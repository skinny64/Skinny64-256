package boomerangsearch.solutiontotikz;

import boomerangsearch.step2.Step2Solution;

import org.javatuples.Pair;

import java.util.List;
import java.util.stream.IntStream;

public class Step2SolutionToTikz {
  private final Step2Solution step2Solution;

  public Step2SolutionToTikz(final String filename, final int solutionNumber, final boolean best) {
    if (!best)
      step2Solution = Step2Solution.fromFile(filename).get(solutionNumber);
    else {
      List<Step2Solution> allSolutions = Step2Solution.fromFile(filename);
      int bestId = IntStream.range(0, allSolutions.size()).boxed()
        .map(i -> new Pair<Double, Integer>(-allSolutions.get(i).probaExponent,i))//Clusters, i))
        .sorted()
        .findFirst()
        .get().getValue1();
      step2Solution = allSolutions.get(bestId);
    }
  }

  public Step2SolutionToTikz(final Step2Solution step2Solution) {
    this.step2Solution = step2Solution;
  }

  public String generate() {
    System.out.println(step2Solution.probaExponent);
    System.out.println(step2Solution.probaClusters);
    String output = "";

    // dX and dSB
      for (int round = step2Solution.nExtbRounds; round < step2Solution.nbRounds+step2Solution.nExtbRounds; round++) {
        for (int i = 0; i < 4; i++)
          for (int j = 0; j < 4; j++) {
            if (step2Solution.dXupper[round-step2Solution.nExtbRounds][i][j] != 0)
              output += "\\node[align=center] at (" + (10*round+j+0.5) + "," + (11.5-i) + ") {\\Large $" + step2Solution.dXupper[round-step2Solution.nExtbRounds][i][j] + "$};\n";
            if (step2Solution.dSBupper[round-step2Solution.nExtbRounds][i][j] != 0)
              output += "\\node[align=center] at (" + (10*round+j+5.5) + "," + (11.5-i) + ") {\\Large $" + step2Solution.dSBupper[round-step2Solution.nExtbRounds][i][j] + "$};\n";
            if (step2Solution.dXlower[round-step2Solution.nExtbRounds][i][j] != 0)
              output += "\\node[align=center] at (" + (10*round+j+0.5) + "," + (6.5-i) + ") {\\Large $" + step2Solution.dXlower[round-step2Solution.nExtbRounds][i][j] + "$};\n";
            if (step2Solution.dSBlower[round-step2Solution.nExtbRounds][i][j] != 0)
              output += "\\node[align=center] at (" + (10*round+j+5.5) + "," + (6.5-i) + ") {\\Large $" + step2Solution.dSBlower[round-step2Solution.nExtbRounds][i][j] + "$};\n";
          }
        output += "\n";
      }
    // dTK
    if (step2Solution.dTKupper != null)
      for (int round = step2Solution.nExtbRounds; round < step2Solution.nbRounds+step2Solution.nExtbRounds; round++) {
        for (int i = 0; i < 2; i++)
          for (int j = 0; j < 4; j++) {
            if (step2Solution.dTKupper.dTK[round-step2Solution.nExtbRounds][i][j] != 0)
              output += "\\node[align=center] at (" + (10*round+j+8) + "," + (14-i) + ") {\\Large $" + step2Solution.dTKupper.dTK[round-step2Solution.nExtbRounds][i][j] + "$};\n";
            if (step2Solution.dTKlower.dTK[round-step2Solution.nExtbRounds][i][j] != 0)
              output += "\\node[align=center] at (" + (10*round+j+8) + "," + (2-i) + ") {\\Large $" + step2Solution.dTKlower.dTK[round-step2Solution.nExtbRounds][i][j] + "$};\n";
          }
      }

    return output;
  }
}
