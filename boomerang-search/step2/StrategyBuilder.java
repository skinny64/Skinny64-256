package boomerangsearch.step2;

import sandwichproba.sboxtables.SboxTables;

import org.javatuples.Pair;

import org.chocosolver.solver.search.strategy.selectors.values.IntValueSelector;
import org.chocosolver.solver.search.strategy.strategy.AbstractStrategy;
import org.chocosolver.solver.variables.IntVar;

import java.util.List;
import java.util.Set;
import java.util.stream.IntStream;
import java.util.stream.Collectors;

public class StrategyBuilder {
  private SboxTables sboxTables;
  private List<Integer> bestDDTIn;
  private List<Integer> bestDDTOut;
  
  public StrategyBuilder(final SboxTables sboxTables) {
    this.sboxTables = sboxTables;
    bestDDTIn = IntStream.range(1, sboxTables.getSboxSize()-1).boxed()
      .map(alpha -> new Pair<Double, Integer>(-getMeanProbaIn(alpha)
                                              ,alpha))
      .sorted()
      .map(p -> p.getValue1())
      .collect(Collectors.toList());
    bestDDTOut = IntStream.range(1, sboxTables.getSboxSize()-1).boxed()
      .map(beta  -> new Pair<Double, Integer>(-getMeanProbaOut(beta)
                                              ,beta))
      .sorted()
      .map(p -> p.getValue1())
      .collect(Collectors.toList());
  }

  private double getMeanProbaIn(final int alpha) {
    Set<Integer> possibleOutputs = sboxTables.getPossibleOutputDiffsDDT(alpha);
    return (double) possibleOutputs.stream()
      .mapToInt(beta -> sboxTables.ddt(alpha, beta))
      .sum() / possibleOutputs.size();
  }

  private double getMeanProbaOut(final int beta) {
    Set<Integer> possibleInputs = sboxTables.getPossibleInputDiffsDDT(beta);
    return (double) possibleInputs.stream()
      .mapToInt(alpha -> sboxTables.ddt(alpha, beta))
      .sum() / possibleInputs.size();
  }

  public IntValueSelector getDDTInSelector(final int bound, final boolean defaultUpper) {
    return (IntValueSelector) var -> {
      for (int i = 0; i < Math.min(bound,sboxTables.getSboxSize()); i++)
        if (var.contains(bestDDTIn.get(i)))
          return bestDDTIn.get(i);
      return (defaultUpper) ? var.getUB() : var.getLB();
    };
  }

  public IntValueSelector getDDTOutSelector(final int bound, final boolean defaultUpper) {
    return (IntValueSelector) var -> {
      for (int i = 0; i < Math.min(bound,sboxTables.getSboxSize()); i++)
        if (var.contains(bestDDTOut.get(i)))
          return bestDDTOut.get(i);
      return (defaultUpper) ? var.getUB() : var.getLB();
    };
  }
}
