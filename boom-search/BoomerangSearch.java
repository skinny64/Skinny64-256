package boomerangsearch;

import boomerangsearch.step1.Step1;
import boomerangsearch.step1.Step1Solution;
import boomerangsearch.step2.Step2;
import boomerangsearch.step2.Step2Solution;

import sandwichproba.sboxtables.Sbox4Skinny;
import sandwichproba.sboxtables.Sbox8Skinny;
import sandwichproba.sboxtables.SboxTables;

import gurobi.*;

import picocli.CommandLine;
import picocli.CommandLine.Command;
import picocli.CommandLine.Option;
import picocli.CommandLine.Parameters;
import picocli.CommandLine.ArgGroup;

import java.util.Arrays;
import java.util.List;
import java.util.ArrayList;
import java.util.stream.Collectors;
import java.util.concurrent.Callable;
import java.util.concurrent.ForkJoinPool;
import java.util.concurrent.ExecutionException;
import java.io.File;

@Command(name = "boomerangsearch", mixinStandardHelpOptions = true, version = "1.0", description = "Find boomerangs on SKINNY")
public class BoomerangSearch implements Callable<Integer> {
  
  @Option(names = {"-verbose","-v"}, description = "Verbose mode. Default is false")
  private boolean verbose;
  @Option(names = {"-nbRounds","-r"}, defaultValue = "12", description = "Number of rounds for the boomerang distinguisher. Default is ${DEFAULT-VALUE}")
  private int nbRounds;
  @Option(names = {"-nExtbRounds","-rb"}, defaultValue = "2", description = "Number of rounds extended before the boomerang distinguisher. Default is ${DEFAULT-VALUE}")
  private int nExtbRounds;
  @Option(names = {"-nExtfRounds","-rf"}, defaultValue = "4", description = "Number of rounds extended after the boomerang distinguisher. Default is ${DEFAULT-VALUE}")
  private int nExtfRounds;
  @Option(names = {"-regime","-tk"}, defaultValue = "2", description = "Regime of the boomerang. Can be 0, 1, 2, 3 or 4 for sk, tk1, tk2, tk3 or tk4. Default is ${DEFAULT-VALUE}")
  private int regime;
  @Option(names = {"-nbThreads","-t"}, defaultValue = "0", description = "Number of threads allowed to use. Default takes as many as possible")
  private int nbThreads;
  

  // -------- Step 1 options --------
  @Option(names = {"-nonOptimalStep1Sols","-nonOpts1"}, description = "Search for non optimal solutions in Step1 (up to obj = 2*bestObj) . Default is false")
  private boolean nonOptimalStep1Sols;
  @Option(names = {"-step1MinObjectiveValue","-s1obj"}, defaultValue = "-1", description = "Value of objective (may not be optimal). Default search for optimal solutions. Seems to greatly increase the running time, preferably you should also generate the optimal solutions")
  private int step1MinObjectiveValue;
  @Option(names = {"-nbStep1Sols","-sols1"}, defaultValue = "1", description = "Maximum number of truncated differentials to find in step1. Default is ${DEFAULT-VALUE}")
  private int nbStep1Sols;
  @Option(names = {"-step1output","-s1o"}, defaultValue = "output/step1.json", description = "Output file of step1 solutions. Default is ${DEFAULT-VALUE}")
  private File step1output;
  
  // -------- Step 2 options --------
  @Option(names = {"-blockSize","-bs"}, defaultValue = "8", description = "Block size. Can be 4 or 8 for 64 or 128 bits. Default is ${DEFAULT-VALUE}")
  private int blockSize;
  @Option(names = {"-nbStep2Sols","-sols2"}, defaultValue = "0", description = "Maximum number of optimal step2 solutions to consider for each step1 solution. Default is ${DEFAULT-VALUE}, 0 means search for all the solutions")
  private int nbStep2Sols;
  @Option(names = {"-noStep2","-nos2"}, description = "If specified, do not run step2")
  private boolean noStep2;
  @Option(names = {"-step2input","-s1i"}, description = "Input file for the step2 solutions. If specified do not run step1")
  private File step2input;
  @Option(names = {"-step2output","-s2o"}, defaultValue = "output/step2.json", description = "Output file of step2 solutions. Default is ${DEFAULT-VALUE}")
  private File step2output;
  @Option(names = {"-clusterGap","-cg"}, defaultValue = "1", description = "Gap allowed in the solutions for the computation of the cluster probability. Default is ${DEFAULT-VALUE}")
  private int clusterGap;
  
  /** The main function instanciates the CLI, get the parameters and then execute the runner (function call()) */
  public static void main(final String... args) throws Exception {
    System.exit(new CommandLine(new BoomerangSearch()).execute(args));
  }

  /** Main function that runs the models */
  @Override
  public Integer call() {
    
    List<Step1Solution> step1Solutions;
    if (step2input == null) {
      step1Solutions = getStep1Solutions();
      Step1Solution.toFile(step1output, step1Solutions);
    }
    else {
      step1Solutions = Step1Solution.fromFile(step2input);
      System.out.println("Loaded " + step1Solutions.size() + " solutions from step1 output file");
    }
    if (!noStep2) {
      System.out.println("");
      List<Step2Solution> step2Solutions = getStep2Solutions(step1Solutions);
      Step2Solution.toFile(step2output, step2Solutions.toArray(new Step2Solution[0]));
    }
    return 0;
  }

  /** Run the Gurobi model to solve the step 1 */
  private List<Step1Solution> getStep1Solutions() {
    try {
      GRBEnv env = new GRBEnv(true);
      env.set(GRB.IntParam.OutputFlag, (verbose) ? 1 : 0);
      env.start();
      Step1 step1 = new Step1(env, nbRounds, nExtbRounds, nExtfRounds, regime, blockSize);
      if (verbose)
        System.out.println("Starting step 1");
      List<Step1Solution> step1Solutions = step1.solve(nbStep1Sols, nonOptimalStep1Sols, step1MinObjectiveValue, nbThreads);
      if (verbose) {
        System.out.println("The best solution has objective " + step1Solutions.get(0).objective);
        System.out.println("Found "+ step1Solutions.size() +" solution(s)");
      }
      step1.dispose();
      env.dispose();
      return step1Solutions;
    } catch (GRBException e) {
      System.out.println("Error code: " + e.getErrorCode() + ". " + e.getMessage());
      e.printStackTrace();
      System.exit(1);
      return null; // Can't access
    }
  }

  /** Run the Choco model to solve the step2, and call it again to do a cluster analysis on the solution found */
  private List<Step2Solution> getStep2Solutions(List<Step1Solution> step1Solutions) {
    SboxTables sboxTables = new SboxTables((blockSize == 4) ? new Sbox4Skinny() : new Sbox8Skinny()); // Only use of blockSize
    ForkJoinPool myPool = (nbThreads == 0) ? new ForkJoinPool() : new ForkJoinPool(nbThreads);
    List<Step2Solution> step2Solutions = new ArrayList<Step2Solution>();
    try {
      step2Solutions =
        myPool.submit(() -> step1Solutions.stream().parallel()
                      .flatMap(solution -> (regime == 0) ? Step2.step2SK(solution, sboxTables, regime, nbStep2Sols, clusterGap, verbose).stream() : Step2.optimizeEnumerateCluster(solution, sboxTables, regime, nbStep2Sols, clusterGap, verbose).stream())
                      .collect(Collectors.toList()))
        .get();
    }
    catch (InterruptedException e) { e.printStackTrace(); System.exit(1);}
    catch (ExecutionException e) { e.printStackTrace(); System.exit(1);}
    if (verbose) {
      if (step2Solutions.size() == 0)
        System.out.println("No solution found");
      else {
        double bestProbaExponent = step2Solutions.stream().mapToDouble(sol -> sol.probaExponent).max().getAsDouble();
        double bestProbaCluster  = step2Solutions.stream().mapToDouble(sol -> sol.probaClusters).max().getAsDouble();
        System.out.println("\nThe best probability step2 found is 2^"+bestProbaExponent);
        System.out.println("The best probability the cluster found is 2^"+bestProbaCluster);
      }
    }
    return step2Solutions;
  }
}
