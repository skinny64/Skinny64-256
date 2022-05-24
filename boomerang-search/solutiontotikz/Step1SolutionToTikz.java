package boomerangsearch.solutiontotikz;

import boomerangsearch.step1.Step1Solution;

import java.util.stream.Collectors;
import java.util.stream.IntStream;

public class Step1SolutionToTikz {
  private final Step1Solution step1Solution;
  private static final String freeColor  = "lightgray";
   private static final String activeColor  = "lightgray";
  private static final String upperColor = "lime";
  private static final String lowerColor = "pink";

  public Step1SolutionToTikz(final String filename, final int solutionNumber) {
    this(Step1Solution.fromFile(filename).get(solutionNumber));
  }

  public Step1SolutionToTikz(final Step1Solution step1Solution) {
    this.step1Solution = step1Solution;
  }

  private boolean hasOnlyOnes(int[][] mat) {
    boolean onlyOnes = true;
    for (int i = 0; i < 4; i++)
      for (int j = 0; j < 4; j++)
        onlyOnes = onlyOnes && mat[i][j] == 1;
    return onlyOnes;
  }

  public String generate() {
    String output = "";
    // Header
    output += "\\begin{tikzpicture}[scale = 0.45,every node/.style={scale=0.5}]\n";
    output += "\\makeatletter\n";
    //Eb
    for (int round = 0; round < step1Solution.nExtbRounds; round++) {
      for (int i = 0; i < 4; i++)
        for (int j = 0; j < 4; j++)
          if (step1Solution.DXuppExt[round][i][j] != 0)
            output += "\\fill[color="+activeColor+"] ("+(10*round+j)+","+(11-i)+") rectangle ++(1,1);\n";
      if (round == step1Solution.nExtbRounds-1) {
        for (int i = 0; i < 4; i++)
          for (int j = 0; j < 4; j++)
            if (step1Solution.DXuppExt[round][i][j] != 0)
              output += "\\fill[color="+upperColor+"] ("+(10*round+j+5)+","+(11-i)+") rectangle ++(1,1);\n";
       }
       else {
         for (int i = 0; i < 4; i++)
          for (int j = 0; j < 4; j++)
            if (step1Solution.DXuppExt[round][i][j] != 0)
              output += "\\fill[color="+activeColor+"] ("+(10*round+j+5)+","+(11-i)+") rectangle ++(1,1);\n";
       }
       output += "\n";
    }
    
    //Ef
    for (int round = 0; round < step1Solution.nExtfRounds; round++) {
      // Lower X
      if (round == 0) {
        for (int i = 0; i < 4; i++)
          for (int j = 0; j < 4; j++)
            if (step1Solution.DXlowExt[round][i][j] != 0)
            output += "\\fill[color="+lowerColor+"] ("+(10*(round+step1Solution.nExtbRounds+step1Solution.nbRounds)+j)+","+(6-i)+") rectangle ++(1,1);\n";
      }
      else {
        for (int i = 0; i < 4; i++)
          for (int j = 0; j < 4; j++)
            if (step1Solution.DXlowExt[round][i][j] != 0)
            output += "\\fill[color="+activeColor+"] ("+(10*(round+step1Solution.nExtbRounds+step1Solution.nbRounds)+j)+","+(6-i)+") rectangle ++(1,1);\n";
      }
      // Lower SB
      for (int i = 0; i < 4; i++)
        for (int j = 0; j < 4; j++)
          if (step1Solution.DXlowExt[round][i][j] != 0)
            output += "\\fill[color="+activeColor+"] ("+(10*(round+step1Solution.nExtbRounds+step1Solution.nbRounds)+j+5)+","+(6-i)+") rectangle ++(1,1);\n";
 
      output += "\n";
    }

    //E
    for (int round = step1Solution.nExtbRounds; round < step1Solution.nExtbRounds+step1Solution.nbRounds; round++) {
      // Upper X
      if (hasOnlyOnes(step1Solution.freeXupper[round-step1Solution.nExtbRounds]))
        output += "\\fill[color="+freeColor+"] ("+(10*round)+",8) rectangle ++(4,4);\n";
      else {
      for (int i = 0; i < 4; i++)
        for (int j = 0; j < 4; j++)
          if (step1Solution.DXupper[round-step1Solution.nExtbRounds][i][j] != 0)
            output += "\\fill[color="+((step1Solution.freeXupper[round-step1Solution.nExtbRounds][i][j]==0) ? upperColor : freeColor)+"] ("+(10*round+j)+","+(11-i)+") rectangle ++(1,1);\n";
      }
      // Upper SB
      if (hasOnlyOnes(step1Solution.freeSBupper[round-step1Solution.nExtbRounds]))
        output += "\\fill[color="+freeColor+"] ("+(10*round+5)+",8) rectangle ++(4,4);\n";
      else {
      for (int i = 0; i < 4; i++)
        for (int j = 0; j < 4; j++)
          if (step1Solution.DXupper[round-step1Solution.nExtbRounds][i][j] != 0)
            output += "\\fill[color="+((step1Solution.freeSBupper[round-step1Solution.nExtbRounds][i][j]==0) ? upperColor : freeColor)+"] ("+(10*round+j+5)+","+(11-i)+") rectangle ++(1,1);\n";
      }
      // Lower X
      if (hasOnlyOnes(step1Solution.freeXlower[round-step1Solution.nExtbRounds]))
        output += "\\fill[color="+freeColor+"] ("+(10*round)+",3) rectangle ++(4,4);\n";
      else {
      for (int i = 0; i < 4; i++)
        for (int j = 0; j < 4; j++)
          if (step1Solution.DXlower[round-step1Solution.nExtbRounds][i][j] != 0)
            output += "\\fill[color="+((step1Solution.freeXlower[round-step1Solution.nExtbRounds][i][j]==0) ? lowerColor : freeColor)+"] ("+(10*round+j)+","+(6-i)+") rectangle ++(1,1);\n";
      }
      // Lower SB
      if (hasOnlyOnes(step1Solution.freeSBlower[round-step1Solution.nExtbRounds]))
        output += "\\fill[color="+freeColor+"] ("+(10*round+5)+",3) rectangle ++(4,4);\n";
      else {
      for (int i = 0; i < 4; i++)
        for (int j = 0; j < 4; j++)
          if (step1Solution.DXlower[round-step1Solution.nExtbRounds][i][j] != 0)
            output += "\\fill[color="+((step1Solution.freeSBlower[round-step1Solution.nExtbRounds][i][j]==0) ? lowerColor : freeColor)+"] ("+(10*round+j+5)+","+(6-i)+") rectangle ++(1,1);\n";
      }
      output += "\n";
    }

    //TK
    if (step1Solution.regime != 0)
      for (int round = step1Solution.nExtbRounds; round < step1Solution.nExtbRounds+step1Solution.nbRounds; round++)
        for (int i = 0; i < 2; i++)
          for (int j = 0; j < 4; j++) {
            // Upper TK
            if (step1Solution.DTKupper.DTK[round-step1Solution.nExtbRounds][i][j] == 1)
              output += "\\fill[color="+upperColor+"] ("+(7.5+10*round+j)+","+(13.5-i)+") rectangle ++(1,1);\n";
            if (step1Solution.DTKlower.DTK[round-step1Solution.nExtbRounds][i][j] == 1)
              output += "\\fill[color="+lowerColor+"] ("+(7.5+10*round+j)+","+(1.5-i)+") rectangle ++(1,1);\n";
          }
    if (step1Solution.regime != 0)
      for (int round = 0; round < step1Solution.nExtbRounds; round++)
        for (int i = 0; i < 2; i++)
          for (int j = 0; j < 4; j++) {
            // Upper TK
            if (step1Solution.DSTKuppExt[round][i][j] == 1)
              output += "\\fill[color="+upperColor+"] ("+(7.5+10*round+j)+","+(13.5-i)+") rectangle ++(1,1);\n";
          }
    if (step1Solution.regime != 0)
      for (int round = 0; round < step1Solution.nExtfRounds-1; round++)
        for (int i = 0; i < 2; i++)
          for (int j = 0; j < 4; j++) {
            // Lower TK
            if (step1Solution.DSTKlowExt[round][i][j] == 1)
              output += "\\fill[color="+lowerColor+"] ("+(7.5+10*(round+step1Solution.nExtbRounds+step1Solution.nbRounds)+j)+","+(1.5-i)+") rectangle ++(1,1);\n";
          }
    
    
    //state
    output += "\n\n\n%grid\n";
    output += "\\@for\\x:={";
    output += String.join(",",IntStream.range(0, step1Solution.nExtbRounds+step1Solution.nbRounds).boxed().map(i->String.valueOf(i)).collect(Collectors.toList()));
    output += "}\\do{\n";
    output += " \\@for\\y:={8}\\do{\n";
    output += "  \\@for\\i:={0,1,2,3,4}\\do{\n";
    output += "   \\draw (10*\\x+\\i, \\y)   -- ++(0,4);\n";
    output += "   \\draw (10*\\x, \\y+\\i)   -- ++(4,0);\n";
    output += "   \\draw (10*\\x+5+\\i, \\y) -- ++(0,4);\n";
    output += "   \\draw (10*\\x+5, \\y+\\i) -- ++(4,0);}\n";
    output += "  \\node[align=center] at (10*\\x+4.5, \\y+2.5) {SB};\n";
    output += "  \\draw[->] (10*\\x+4, \\y+2) -- ++(1,0);}\n";
    output += " \\node[align=center] at (10*\\x+3, 13) {\\textbf{\\Large R\\x}};}\n";
    //output += " \\node[align=center] at (10*\\x+3, 2)  {\\textbf{\\Large R\\x}};}\n";

    //state
    output += "\n\n\n%grid\n";
    output += "\\@for\\x:={";
    output += String.join(",",IntStream.range(step1Solution.nExtbRounds, step1Solution.nExtbRounds+step1Solution.nbRounds+step1Solution.nExtfRounds).boxed().map(i->String.valueOf(i)).collect(Collectors.toList()));
    output += "}\\do{\n";
    output += " \\@for\\y:={3}\\do{\n";
    output += "  \\@for\\i:={0,1,2,3,4}\\do{\n";
    output += "   \\draw (10*\\x+\\i, \\y)   -- ++(0,4);\n";
    output += "   \\draw (10*\\x, \\y+\\i)   -- ++(4,0);\n";
    output += "   \\draw (10*\\x+5+\\i, \\y) -- ++(0,4);\n";
    output += "   \\draw (10*\\x+5, \\y+\\i) -- ++(4,0);}\n";
    output += "  \\node[align=center] at (10*\\x+4.5, \\y+2.5) {SB};\n";
    output += "  \\draw[->] (10*\\x+4, \\y+2) -- ++(1,0);}\n";
    //output += " \\node[align=center] at (10*\\x+3, 13) {\\textbf{\\Large R\\x}};\n";
    output += " \\node[align=center] at (10*\\x+3, 2)  {\\textbf{\\Large R\\x}};}\n";
    // TK
    output += "\\@for\\x:={";
    output += String.join(",",IntStream.range(0, step1Solution.nExtbRounds+step1Solution.nbRounds).boxed().map(i->String.valueOf(7.5+10*i)).collect(Collectors.toList()));
    output += "}\\do{\n";
    output += " \\@for\\y:={12.5}\\do{\n";
    output += "  \\@for\\i:={0,1,2,3,4}\\do{\n";
    output += "   \\draw (\\x+\\i, \\y) -- ++(0,2);}\n";
    output += "  \\@for\\i:={0,1,2}\\do{\n";
    output += "   \\draw (\\x, \\y+\\i) -- ++(4,0);}}\n";
    output += " \\@for\\y:={12.5}\\do{\n";
    output += "  \\draw[->] (\\x+1.5, 0.5*\\y+3.75) -- ++(1,0);\n";
    output += "  \\draw[->] (\\x+2, \\y) -- (\\x+2, 0.5*\\y+3.75);\n"; // may want a Xor here instead of an arrow
    output += "  \\node[align=center] at (\\x+2, 0.3*\\y+5.25) {ART\\\\SR\\\\MC};}}\n";

    output += "\\@for\\x:={";
    output += String.join(",",IntStream.range(step1Solution.nExtbRounds, step1Solution.nExtbRounds+step1Solution.nbRounds+step1Solution.nExtfRounds-1).boxed().map(i->String.valueOf(7.5+10*i)).collect(Collectors.toList()));
    output += "}\\do{\n";
    output += " \\@for\\y:={0.5}\\do{\n";
    output += "  \\@for\\i:={0,1,2,3,4}\\do{\n";
    output += "   \\draw (\\x+\\i, \\y) -- ++(0,2);}\n";
    output += "  \\@for\\i:={0,1,2}\\do{\n";
    output += "   \\draw (\\x, \\y+\\i) -- ++(4,0);}}\n";
    output += " \\@for\\y:={2.5}\\do{\n";
    output += "  \\draw[->] (\\x+1.5, 0.5*\\y+3.75) -- ++(1,0);\n";
    output += "  \\draw[->] (\\x+2, \\y) -- (\\x+2, 0.5*\\y+3.75);\n"; // may want a Xor here instead of an arrow
    output += "  \\node[align=center] at (\\x+2, 0.3*\\y+5.25) {ART\\\\SR\\\\MC};}}\n";


    // Footer
    output += "\\makeatother\n";
    output += "\\end{tikzpicture}\n";

    return output;
  }
}
