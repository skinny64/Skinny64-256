package boomerangsearch.step2;

import boomerangsearch.step1.Step1Solution;

import sandwichproba.sboxtables.SboxTables;

import org.chocosolver.cutoffseq.LubyCutoffStrategy;
import org.chocosolver.solver.Model;
import org.chocosolver.solver.Solver;
import org.chocosolver.solver.search.strategy.Search;
import org.chocosolver.solver.search.strategy.selectors.values.IntDomainMin;
import org.chocosolver.solver.search.strategy.selectors.values.IntDomainMax;
import org.chocosolver.solver.search.strategy.selectors.values.IntDomainBest;
import org.chocosolver.solver.search.strategy.selectors.values.IntDomainImpact;
import org.chocosolver.solver.search.strategy.selectors.variables.AntiFirstFail;
import org.chocosolver.solver.search.strategy.selectors.variables.FirstFail;
import org.chocosolver.solver.search.strategy.selectors.variables.DomOverWDeg;
import org.chocosolver.solver.search.strategy.selectors.variables.ActivityBased;
import org.chocosolver.solver.search.loop.monitors.NogoodFromSolutions;
import org.chocosolver.solver.expression.discrete.arithmetic.ArExpression;
import org.chocosolver.solver.variables.IntVar;
import org.chocosolver.util.tools.ArrayUtils;

import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.File;
import java.util.List;
import java.util.Arrays;
import java.util.ArrayList;
import java.util.Map;
import java.util.HashMap;

public class Step2 {
  private final Model model;
  private final int regime;
  private final Step2Factory factory;
  private final Step1Solution step1;
  private final int nbRounds;
  private final int nExtbRounds;
  private final int nExtfRounds;
  private final IntVar[][][] dXupper;
  private final IntVar[][][] dSBupper;
  private final IntVar[][][] dYupper;
  private final Step2Tweakey dTKupper;
  private final IntVar[][][] dXlower;
  private final IntVar[][][] dSBlower;
  private final IntVar[][][] dYlower;
  private final Step2Tweakey dTKlower;
  private final IntVar[][][] proba;
  private final IntVar objective;

  public Step2(Step1Solution step1, SboxTables sboxTables, final int regime) {
    model = new Model();
    this.regime = regime;
    factory = new Step2Factory(model, 2, sboxTables);
    this.step1 = step1;
    nbRounds = step1.nbRounds;
    nExtbRounds = step1.nExtbRounds;
    nExtfRounds = step1.nExtfRounds;
    proba = new IntVar[nbRounds][4][4];
    // Upper
    dXupper  = new IntVar[nbRounds][4][4];
    dSBupper = new IntVar[nbRounds][4][4];
    dYupper  = (regime == 0) ? null : new IntVar[nbRounds-1][2][4];
    dTKupper = (regime == 0) ? null : new Step2Tweakey(model, nbRounds, regime, sboxTables.getSboxSize(), step1.DTKupper);
    // Lower
    dXlower  = new IntVar[nbRounds][4][4];
    dSBlower = new IntVar[nbRounds][4][4];
    dYlower  = (regime == 0) ? null : new IntVar[nbRounds-1][2][4];
    dTKlower = (regime == 0) ? null : new Step2Tweakey(model, nbRounds, regime, sboxTables.getSboxSize(), step1.DTKlower);

    
    // -------- Initialization --------
    if (regime != 0) {
      factory.initializeY(dYupper, step1.freeSBupper);
      factory.initializeY(dYlower, step1.freeSBlower);
    }
    objective = factory.initializeAndPostSboxes(dXupper, dSBupper, dXlower, dSBlower, proba, step1);

    // -------- Constraints --------
    if (regime == 0) {
      factory.shiftrowsMCSK(dXupper, dSBupper, step1.DXupper, step1.freeXupper, step1.freeSBupper);
      factory.shiftrowsMCSK(dXlower, dSBlower, step1.DXlower, step1.freeXlower, step1.freeSBlower);
    }
    else {
      // Upper 
      factory.addRoundTweakey(dSBupper, step1.DXupper, dTKupper.dTK, step1.DTKupper.DTK, dYupper, step1.freeSBupper);
      factory.shiftrowsMC(dXupper, dSBupper, step1.DXupper, dYupper, step1.freeXupper, step1.freeSBupper);
      // Lower
      factory.addRoundTweakey(dSBlower, step1.DXlower, dTKlower.dTK, step1.DTKlower.DTK, dYlower, step1.freeSBlower);
      factory.shiftrowsMC(dXlower, dSBlower, step1.DXlower, dYlower, step1.freeXlower, step1.freeSBlower);
    }

    List<IntVar> middleVariables = new ArrayList<IntVar>();
    List<IntVar> ddtInVariables  = new ArrayList<IntVar>();
    List<IntVar> ddtOutVariables = new ArrayList<IntVar>();
    List<IntVar> ddtVariables = new ArrayList<IntVar>();
    List<IntVar> probaVariables  = new ArrayList<IntVar>();
    for (int round = 0; round < nbRounds; round++)
      for (int i = 0; i < 4; i++)
        for (int j = 0; j < 4; j++) {
          if (proba[round][i][j] != null)
            probaVariables.add(proba[round][i][j]);
          if (dXupper[round][i][j] != null && dSBlower[round][i][j] != null) {
            middleVariables.add(dXupper[round][i][j]);
            middleVariables.add(dSBlower[round][i][j]);
            if (dSBupper[round][i][j] != null)
              middleVariables.add(dSBupper[round][i][j]);
            if (dXlower[round][i][j] != null)
              middleVariables.add(dXlower[round][i][j]);
          }
          else {
            if (dXupper[round][i][j] != null)
              ddtInVariables.add(dXupper[round][i][j]);
            if (dSBupper[round][i][j] != null)
              ddtOutVariables.add(dSBupper[round][i][j]);
            if (dXlower[round][i][j] != null)
              ddtInVariables.add(dXlower[round][i][j]);
            if (dSBlower[round][i][j] != null)
              ddtOutVariables.add(dSBlower[round][i][j]);
            // DDTvariables
            if (dXupper[round][i][j] != null)
              ddtVariables.add(dXupper[round][i][j]);
            if (dSBupper[round][i][j] != null)
              ddtVariables.add(dSBupper[round][i][j]);
            if (dXlower[round][i][j] != null)
              ddtVariables.add(dXlower[round][i][j]);
            if (dSBlower[round][i][j] != null)
              ddtVariables.add(dSBlower[round][i][j]);
          }
        }

    StrategyBuilder strategyBuilder = new StrategyBuilder(sboxTables);
    
    // -------- Solver configuration --------
    Solver solver = model.getSolver();
    if (regime == 0) {
      if (middleVariables.size() == 0)
        solver.setSearch(Search.lastConflict(new DomOverWDeg(ArrayUtils.append((middleVariables.size() == 0) ? new IntVar[]{} : ArrayUtils.toArray(middleVariables),(ddtVariables.size() == 0) ? new IntVar[]{} : ArrayUtils.toArray(ddtVariables)), 0, new IntDomainMin())));
    }
    else {
      switch (0) {// 0 is default
      case 0:
        solver.setSearch(Search.lastConflict(new DomOverWDeg(ArrayUtils.append(dTKupper.getVariables(), dTKlower.getVariables()), 0, new IntDomainMin())),
                         Search.lastConflict(new DomOverWDeg(ArrayUtils.append((middleVariables.size() == 0) ? new IntVar[]{} : ArrayUtils.toArray(middleVariables),ArrayUtils.toArray(ddtVariables)), 0, new IntDomainMin())));
        break;
      case 1:
        solver.setSearch(Search.intVarSearch(new AntiFirstFail(model), strategyBuilder.getDDTInSelector(10, true), ArrayUtils.append(dTKupper.getVariables(), dTKlower.getVariables())),
                         Search.intVarSearch(new AntiFirstFail(model), strategyBuilder.getDDTInSelector(10, true), ArrayUtils.append((middleVariables.size() == 0) ? new IntVar[]{} : ArrayUtils.toArray(middleVariables),ArrayUtils.toArray(ddtVariables))));
        break;
      case 2:
        solver.setSearch(Search.intVarSearch(new FirstFail(model), strategyBuilder.getDDTInSelector(10, false), ArrayUtils.append(dTKupper.getVariables(), dTKlower.getVariables())),
                         Search.intVarSearch(new FirstFail(model), strategyBuilder.getDDTInSelector(10, false), ArrayUtils.append((middleVariables.size() == 0) ? new IntVar[]{} : ArrayUtils.toArray(middleVariables),ArrayUtils.toArray(ddtVariables))));
        break;
      }
    }
    solver.setRestarts(l -> solver.getFailCount() >= l, new LubyCutoffStrategy(2), 256);
    solver.setNoGoodRecordingFromRestarts();
  }

  public static List<Step2Solution> step2SK(Step1Solution step1Solution, SboxTables sboxTables, int regime, int maxNbSols, int clusterGap, boolean verbose) {
    if (verbose)
      System.out.println("Step2 with step1 solution with objective " + step1Solution.objective);
    List<Step2Solution> step2Solutions = new Step2(step1Solution, sboxTables, regime).getOptimalSK(verbose);
    //step2Solutions.get(0).probaClusters = new Step2(step1Solution, sboxTables, regime).getProbaCluster(step2Solutions.get(0), (int) Math.round(-10*step2Solutions.get(0).probaExponent), clusterGap, verbose);
    if (verbose)
      System.out.println("From a step1 solution with objective " + step1Solution.objective + ", step2 found "+ step2Solutions.size() + " solutions" + ((step2Solutions.size() == 0) ? "." : " with objective 2^" + step2Solutions.get(0).probaExponent + "."));
    return step2Solutions;
  }

  public List<Step2Solution> getOptimalSK(boolean verbose) {
    model.setObjective(Model.MINIMIZE, objective);
    Solver solver = model.getSolver();
    //int timeLimitInSeconds = 60;
    //solver.limitTime(timeLimitInSeconds*1000);
    Step2Solution bestSolution = null;
    while (solver.solve()) {
      int optimalObjective = objective.getValue();
      //System.out.print("  " + optimalObjective);
      bestSolution = getSolution();
    }
    System.out.println("");
    return (bestSolution == null) ? new ArrayList<Step2Solution>() : Arrays.asList(bestSolution);
  }

  public static List<Step2Solution> optimizeEnumerateCluster(Step1Solution step1Solution, SboxTables sboxTables, int regime, int maxNbSols, int clusterGap, boolean verbose) {
    int i=0;
    if (verbose)
        System.out.println("Step2 with step1 solution with objective " + step1Solution.objective);
    int optimalObjective = new Step2(step1Solution, sboxTables, regime).getOptimalObjective(verbose);
    List<Step2Solution> step2Solutions = new Step2(step1Solution, sboxTables, regime).enumerateBestSolutions(optimalObjective, maxNbSols, verbose);
    System.out.println("Found "+ step2Solutions.size() + " solutions.");
    for (Step2Solution step2Solution : step2Solutions) {
      //i ++;
      //System.out.println("Step2 solution" + i);
      //if (i >2)
           //break;
      step2Solution.probaClusters = new Step2(step1Solution, sboxTables, regime).getProbaCluster(step2Solution, optimalObjective, clusterGap, verbose); // Cluster analysis
      }
    if (verbose)
      System.out.println("From a step1 solution with objective " + step1Solution.objective + ", step2 found "+ step2Solutions.size() + " solutions" + ((step2Solutions.size() == 0) ? "." : " with objective 2^-" + (double) optimalObjective/Step2Factory.probaFactor + ".\nThe cluster analysis found a solution with probability 2^"+ step2Solutions.stream().mapToDouble(sol -> sol.probaClusters).max().getAsDouble()));
    return step2Solutions;
  }
  
  public int getOptimalObjective(boolean verbose) {
    model.setObjective(Model.MINIMIZE, objective);
    Solver solver = model.getSolver();
    //int timeLimitInSeconds = 20;
    //solver.limitTime(timeLimitInSeconds*1000);
    int optimalObjective = -1;
    while (solver.solve()) {
      optimalObjective = objective.getValue();
      //System.out.print("  " + optimalObjective);
    }
    //System.out.println("");
    return optimalObjective;
  }

  private List<Step2Solution> enumerateBestSolutions(final int optimum, final int maxNbSols, final boolean verbose) {
    objective.eq(optimum).post();
    Solver solver = model.getSolver();
    solver.setNoGoodRecordingFromSolutions(getInterestingStep2Vars()); // Consider only different solutions
    List<Step2Solution> bestSolutions = new ArrayList<Step2Solution>();
    int count = 0;
    while ((maxNbSols == 0 || count < maxNbSols) && solver.solve()) {
      bestSolutions.add(getSolution());
      count++;
    }
    return bestSolutions;
  }

  public double getProbaCluster(Step2Solution step2, int optimum, int gap, boolean verbose) {
    for (int i = 0; i < 4; i++)
      for (int j = 0; j < 4; j++) {
        if (dXupper[0][i][j] != null)
          dXupper[0][i][j].eq(step2.dXupper[0][i][j]).post();
        if (dSBlower[nbRounds-1][i][j] != null)
          dSBlower[nbRounds-1][i][j].eq(step2.dSBlower[nbRounds-1][i][j]).post();
      }
    if (regime != 0) {
      dTKupper.fixSolution(step2.dTKupper);
      dTKlower.fixSolution(step2.dTKlower);
    }
    objective.le(Step2Factory.probaFactor*gap + optimum).post();
    double proba = 0;
    Solver solver = model.getSolver();
    int count = 0;
    Map<Integer, Integer> solsPerProba = new HashMap<Integer,Integer>();
    while(solver.solve()) {
      count++;
      proba = proba + Math.pow(2, ((double) optimum - objective.getValue())/Step2Factory.probaFactor);
      int currentProba = (solsPerProba.containsKey(objective.getValue())) ? solsPerProba.get(objective.getValue()) : 0;
      solsPerProba.put(objective.getValue(), currentProba+1);
    }
    //System.out.println(solsPerProba);
    return Math.log(proba)/Math.log(2) - (double) optimum/Step2Factory.probaFactor;
  }

  private Step2Solution getSolution() {
    int[][][] dXupperValue = new int[nbRounds][4][4];
    int[][][] dSBupperValue = new int[nbRounds][4][4];
    int[][][] dYupperValue = new int[nbRounds-1][2][4];
    int[][][] dXlowerValue = new int[nbRounds][4][4];
    int[][][] dSBlowerValue = new int[nbRounds][4][4];
    int[][][] dYlowerValue = new int[nbRounds-1][4][4];
    int[][][] probaValue = new int[nbRounds][4][4];
    for (int i = 0; i < 4; i++)
      for (int j = 0; j < 4; j++) {
        for (int round = 0; round < nbRounds; round++) {
          if (dXupper[round][i][j] != null)
            dXupperValue[round][i][j] = dXupper[round][i][j].getValue();
          if (dSBupper[round][i][j] != null)
            dSBupperValue[round][i][j] = dSBupper[round][i][j].getValue();
          if (dXlower[round][i][j] != null)
            dXlowerValue[round][i][j] = dXlower[round][i][j].getValue();
          if (dSBlower[round][i][j] != null)
            dSBlowerValue[round][i][j] = dSBlower[round][i][j].getValue();
          if (proba[round][i][j] != null)
            probaValue[round][i][j] = proba[round][i][j].getValue();
          if (regime != 0)
            if (round < nbRounds-1 && i < 2) {
              if (dYupper[round][i][j] != null)
                dYupperValue[round][i][j] = dYupper[round][i][j].getValue();
              if (dYlower[round][i][j] != null)
                dYlowerValue[round][i][j] = dYlower[round][i][j].getValue();
            }
        }
      }
    return new Step2Solution(nbRounds, nExtbRounds, nExtfRounds, -(double) objective.getValue()/Step2Factory.probaFactor,  dXupperValue, dSBupperValue, dYupperValue, (regime == 0) ? null : dTKupper.getValue(), dXlowerValue, dSBlowerValue, dYlowerValue, (regime == 0) ? null : dTKlower.getValue(), probaValue);
  }

  private IntVar[] getInterestingStep2Vars() {
    List<IntVar> vars = new ArrayList<IntVar>();
    boolean fullAnalysis = true;
    if (fullAnalysis)
      for (int i = 0; i < 4; i++)
        for (int j = 0; j < 4; j++) {
          if (dXupper[0][i][j] != null)
            vars.add(dXupper[0][i][j]);
          if (dSBlower[nbRounds-1][i][j] != null)
            vars.add(dSBlower[nbRounds-1][i][j]);
        }
    else
      for (int i = 0; i < 4; i++)
        for (int j = 0; j < 4; j++) {
          if (dSBupper[0][i][j] != null)
            vars.add(dSBupper[0][i][j]);
          if (dXlower[nbRounds-1][i][j] != null)
            vars.add(dXlower[nbRounds-1][i][j]);
        }
    if (regime == 0)
      return vars.toArray(new IntVar[0]);
    else
      return ArrayUtils.append(dTKupper.getVariables(),
                               dTKlower.getVariables(),
                               vars.toArray(new IntVar[0]));
  }
  
}
