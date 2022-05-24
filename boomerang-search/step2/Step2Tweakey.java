package boomerangsearch.step2;

import boomerangsearch.step1.Step1SolutionTweakey;

import org.chocosolver.solver.Model;
import org.chocosolver.solver.constraints.extension.Tuples;
import org.chocosolver.solver.variables.IntVar;
import org.chocosolver.util.tools.ArrayUtils;

import java.util.List;
import java.util.ArrayList;

public class Step2Tweakey {
  private static final int[] invPermutationTKS = new int[]{2, 0, 4, 7, 6, 3, 5, 1};
  private final Tuples tableLFSRTK2;
  private final Tuples tableLFSRTK3;
  private final Tuples tableLFSRTK4;
  private final Model model;
  private final int nbRounds;
  private final int regime;
  private final int cellSize;
  private final IntVar[][][] dTK1;
  private final IntVar[][][] dTK2;
  private final IntVar[][][] dTK3;
  private final IntVar[][][] dTK4;
  public  final IntVar[][][] dTK;

  public Step2Tweakey(final Model model, final int nbRounds, final int regime, final int cellSize, final Step1SolutionTweakey step1TK) {
    if (regime == 0)
      throw new IllegalStateException("You should not define any tweakey in SK");
    this.model = model;
    this.nbRounds = nbRounds;
    this.regime = regime;
    this.cellSize = cellSize;
    tableLFSRTK2 = createRelationLFSRTK2();
    tableLFSRTK3 = createRelationLFSRTK3();
    tableLFSRTK4 = createRelationLFSRTK4();
    dTK1 = new IntVar[nbRounds][2][4];
    dTK2 = new IntVar[nbRounds][2][4];
    dTK3 = new IntVar[nbRounds][2][4];
    dTK4 = new IntVar[nbRounds][2][4];
    dTK  = new IntVar[nbRounds][2][4];
    switch (regime) { 
    case 4: initializedTKLFSR(step1TK.lanes, dTK4, tableLFSRTK4, "4");
    case 3: initializedTKLFSR(step1TK.lanes, dTK3, tableLFSRTK3, "3");
    case 2: initializedTKLFSR(step1TK.lanes, dTK2, tableLFSRTK2, "2");
    case 1: initializedTK1(step1TK.lanes);
    }
    switch (regime) {
    case 4: initializedTKfull1234(step1TK.DTK);
      break;
    case 3: initializedTKfull123(step1TK.DTK);
      break;
    case 2: initializedTKfull12(step1TK.DTK);
      break;
    case 1: initializedTKfull1();
      break;
    }
  }

  // -------- Initialize the keys 1,2,3 and 4 --------
  private void initializeFirstTwoRounds(final int[] lanes, final IntVar[][][] dTK, final String name) {
    for (int i = 0; i < 2; i++)
      for (int j = 0; j < 4; j++) {
        if (lanes[i*4+j]   == 1)
          dTK[0][i][j] = model.intVar(name+"-"+0+i+j, 0, cellSize);
        if (lanes[8+i*4+j] == 1)
          dTK[1][i][j] = model.intVar(name+"-"+1+i+j, 0, cellSize);
      }
  }

  private void initializedTKLFSR(final int[] lanes, IntVar[][][] dTK234, Tuples tableLFSR, String regimestr) {
    initializeFirstTwoRounds(lanes, dTK234, "dTK"+regimestr);
    for (int round = 2; round < nbRounds; round++)
      for (int cell = 0; cell < 8; cell++)
        if (dTK234[round-2][cell/4][cell%4] != null) {
          int nextCell = invPermutationTKS[cell];
          dTK234[round][nextCell/4][nextCell%4] = model.intVar("dTK"+regimestr+"-"+round+(nextCell/4)+(nextCell%4), 0, cellSize);
          model.table(new IntVar[]{dTK234[round-2][cell/4][cell%4], dTK234[round][nextCell/4][nextCell%4]}, tableLFSR).post();
      }
  }

  private void initializedTK1(final int[] lanes) {
    initializeFirstTwoRounds(lanes, dTK1, "dTK1");
    for (int round = 2; round < nbRounds; round++)
      for (int cell = 0; cell < 8; cell++) {
        int nextCell = invPermutationTKS[cell];
        dTK1[round][nextCell/4][nextCell%4] = dTK1[round-2][cell/4][cell%4];
      }
  }

  // -------- Initialize the xor of the keys --------
  private void initializedTKfull1234(final int[][][] DTK) {
    for (int round = 0; round < nbRounds; round++)
      for (int i = 0; i < 2; i++)
        for (int j = 0; j < 4; j++)
          if (DTK[round][i][j] == 1) {
            dTK[round][i][j] = model.intVar("dTK"+round+i+j, 0, cellSize);
            Step2Factory.postXOR(model, dTK1[round][i][j], dTK2[round][i][j], dTK3[round][i][j], dTK4[round][i][j], dTK[round][i][j], cellSize);
          }
          else
            if (dTK1[round][i][j] != null) // lane is not zero
              Step2Factory.postXOR(model, dTK1[round][i][j], dTK2[round][i][j], dTK3[round][i][j], dTK4[round][i][j]);

  }


  private void initializedTKfull123(final int[][][] DTK) {
    for (int round = 0; round < nbRounds; round++)
      for (int i = 0; i < 2; i++)
        for (int j = 0; j < 4; j++)
          if (DTK[round][i][j] == 1) {
            dTK[round][i][j] = model.intVar("dTK"+round+i+j, 0, cellSize);
            Step2Factory.postXOR(model, dTK1[round][i][j], dTK2[round][i][j], dTK3[round][i][j], dTK[round][i][j], cellSize);
          }
          else
            if (dTK1[round][i][j] != null) // lane is not zero
              Step2Factory.postXOR(model, dTK1[round][i][j], dTK2[round][i][j], dTK3[round][i][j]);

  }

  private void initializedTKfull12(final int[][][] DTK) {
    for (int round = 0; round < nbRounds; round++)
      for (int i = 0; i < 2; i++)
        for (int j = 0; j < 4; j++)
          if (DTK[round][i][j] == 1) {
            dTK[round][i][j] = model.intVar("dTK"+round+i+j, 0, cellSize);
            Step2Factory.postXOR(model, dTK1[round][i][j], dTK2[round][i][j], dTK[round][i][j]);
          }
          else
            if (dTK1[round][i][j] != null) {// lane is not zero
              dTK1[round][i][j].eq(dTK2[round][i][j]).post();
            } 
  }

  private void initializedTKfull1() {
    for (int round = 0; round < nbRounds; round++)
      for (int i = 0; i < 2; i++)
        for (int j = 0; j < 4; j++)
          dTK[round][i][j] = dTK1[round][i][j];
  }

  public void fixSolution(Step2SolutionTweakey step2TK) {
    for (int i = 0; i < 2; i++)
      for (int j = 0; j < 4; j++)
        for (int round = 0; round < nbRounds-1; round++) {
          if (dTK1[round][i][j] != null)
            dTK1[round][i][j].eq(step2TK.dTK1[round][i][j]).post();
          if (dTK2[round][i][j] != null)
            dTK2[round][i][j].eq(step2TK.dTK2[round][i][j]).post();
          if (dTK3[round][i][j] != null)
            dTK3[round][i][j].eq(step2TK.dTK3[round][i][j]).post();
          if (dTK4[round][i][j] != null)
            dTK4[round][i][j].eq(step2TK.dTK4[round][i][j]).post();
          if (dTK[round][i][j] != null)
            dTK[round][i][j].eq(step2TK.dTK[round][i][j]).post();
        }
  }

  public IntVar[] getVariables() {
    List<IntVar> keyVariables = new ArrayList<IntVar>();
    for (int round = 0; round < 2; round++)
      for (int i = 0; i < 2; i++)
        for (int j = 0; j < 4; j++)
          switch (regime) {
          case 4: if (dTK4[round][i][j] != null) keyVariables.add(dTK4[round][i][j]);
          case 3: if (dTK3[round][i][j] != null) keyVariables.add(dTK3[round][i][j]);
          case 2: if (dTK2[round][i][j] != null) keyVariables.add(dTK2[round][i][j]);
          case 1: if (dTK1[round][i][j] != null) keyVariables.add(dTK1[round][i][j]);
          }
    return ArrayUtils.toArray(keyVariables);
  }

  public Step2SolutionTweakey getValue() {
    int[][][] dTK1Value = new int[nbRounds][2][4];
    int[][][] dTK2Value = new int[nbRounds][2][4];
    int[][][] dTK3Value = new int[nbRounds][2][4];
    int[][][] dTK4Value = new int[nbRounds][2][4];
    int[][][] dTKValue  = new int[nbRounds][2][4];
    for (int i = 0; i < 2; i++)
      for (int j = 0; j < 4; j++)
        for (int round = 0; round < nbRounds; round++) {
          if (dTK1[round][i][j] != null)
            dTK1Value[round][i][j] = dTK1[round][i][j].getValue();
          if (dTK2[round][i][j] != null)
            dTK2Value[round][i][j] = dTK2[round][i][j].getValue();
          if (dTK3[round][i][j] != null)
            dTK3Value[round][i][j] = dTK3[round][i][j].getValue();
          if (dTK4[round][i][j] != null)
            dTK4Value[round][i][j] = dTK4[round][i][j].getValue();
          if (dTK[round][i][j] != null)
            dTKValue[round][i][j]  = dTK[round][i][j].getValue();
        }
    return new Step2SolutionTweakey(dTK1Value, dTK2Value, dTK3Value, dTK4Value, dTKValue);
  }

  // -------- Creation of LFSR tables --------
  private Tuples createRelationLFSRTK2() {
    Tuples tuples = new Tuples(true);
    if (cellSize == 16)
      for (int i = 0; i < 16; i++)
        tuples.add(i, ((i << 1) % 16) + new int[]{0,1,1,0}[i/4]);
    else
      for (int i = 0; i < 256; i++)
        tuples.add(i, (((i << 1) % 256) + ((i >> 5) + (i >> 7)) % 2));
    return tuples;
  }
  
  private Tuples createRelationLFSRTK3() {
    Tuples tuples = new Tuples(true);
    if (cellSize == 16)
      for (int i = 0; i < 16; i++)
        tuples.add(i, ((i/8)^(i%2))*8 + i/2);
    else
      for (int i = 0; i < 256; i++)
        tuples.add(i, (((i/64)^i)%2)*128 + i/2);
    return tuples;
  }
  //v1
  private Tuples createRelationLFSRTK4() {
    Tuples tuples = new Tuples(true);
    if (cellSize == 16)
      for (int i = 0; i < 16; i++)
        tuples.add(i, ((i<<1)&12) + (((i>>2)%2)^(i%2))*2 + ((i/8)^((i>>2)%2)^((i>>1)%2)));
    return tuples;
  }
  //v2
  /*private Tuples createRelationLFSRTK4() {
    Tuples tuples = new Tuples(true);
    if (cellSize == 16)
      for (int i = 0; i < 16; i++)
        tuples.add(i, ((i<<2)&12) + (((i>>2)%2)^((i>>3)%2))*2 + (((i>>2)%2)^((i>>1)%2)));
    return tuples;
  }*/
}
