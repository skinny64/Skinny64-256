package boomerangsearch.solutiontotikz;

import boomerangsearch.step1.Step1Solution;

import picocli.CommandLine;
import picocli.CommandLine.Command;
import picocli.CommandLine.Option;
import picocli.CommandLine.Parameters;
import picocli.CommandLine.ArgGroup;

import java.util.Arrays;
import java.util.concurrent.Callable;
import java.io.File;

@Command(name = "boomerangsearch", mixinStandardHelpOptions = true, version = "1.0", description = "Find boomerangs on SKINNY")
public class SolutionToTikz implements Callable<Integer> {
  
  @Option(names = {"-solNumber","-sol"}, defaultValue = "0", description = "The solution number in the array. Default is ${DEFAULT-VALUE}")
  private int solNumber;

  // -------- Step 1 options --------
  @Option(names = {"-step1input","-s1i"}, description = "Output file of step1 solutions to convert to tikz.")
  private String step1input;

  // -------- Step 2 options --------
  @Option(names = {"-step2input","-s2i"}, description = "Output file of step2 solutions to convert to tikz.}")
  private String step2input; // Ajouter un exclusive pour la step2
  @Option(names = {"-step2best","-s2b"}, description = "The solution number in the array. Default is ${DEFAULT-VALUE}")
  private boolean step2best;

  /** The main function instanciates the CLI, get the parameters and then execute the runner (function call()) */
  public static void main(final String... args) throws Exception {
    System.exit(new CommandLine(new SolutionToTikz()).execute(args));
  }

  /** Main function that runs the models */
  @Override
  public Integer call() {
    String tikz = "";
    if (step1input != null)
      tikz = new Step1SolutionToTikz(step1input, solNumber).generate();
    if (step2input != null)
      tikz = new Step2SolutionToTikz(step2input, solNumber, step2best).generate();
    System.out.println(tikz);
    return 0;
  }

}
