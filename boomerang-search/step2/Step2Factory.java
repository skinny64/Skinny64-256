package boomerangsearch.step2;

import boomerangsearch.step1.Step1Solution;

import sandwichproba.sboxtables.SboxTables;

import org.javatuples.Triplet;
import org.javatuples.Quartet;

import org.chocosolver.solver.Model;
import org.chocosolver.solver.variables.IntVar;
import org.chocosolver.solver.expression.discrete.arithmetic.ArExpression;
import org.chocosolver.solver.constraints.extension.Tuples;

public class Step2Factory {
  public static int nbXors = 0;
  private static final int[] invPermutationTKS = new int[]{2, 0, 4, 7, 6, 3, 5, 1};
  public  static final int probaFactor = 10;
  private final int[] probaExponents;
  private final Model model;
  private final int tkVersion;
  private final SboxTables sboxTables;
  private static final Tuples tableXor = createRelationXor();
  private final Tuples tableDDT;
  private final Tuples tableDDT2;
  private final Tuples tableBCT;
  private final Tuples tableUBCT;
  private final Tuples tableLBCT;
  private final Tuples tableFBCT;


  public Step2Factory(final Model model, final int tkVersion, final SboxTables sboxTables) {
    this.model = model;
    this.tkVersion = tkVersion;
    this.sboxTables = sboxTables;
    probaExponents = new int[sboxTables.getSboxSize()+1];
    for (int i = 0; i < sboxTables.getSboxSize()+1; i++) {
      probaExponents[i] = (int) Math.round(-probaFactor*Math.log((double) i/sboxTables.getSboxSize())/Math.log(2.0));
    }
    tableDDT = createRelationDDT(1);
    tableDDT2 = createRelationDDT(2);
    tableBCT = createRelationBCT();
    tableUBCT = createRelationUBCT();
    tableLBCT = createRelationLBCT();
    tableFBCT = createRelationFBCT();
  }
  
  // -------- Initialization --------
  public IntVar initializeAndPostSboxes(IntVar[][][] dXupper, IntVar[][][] dSBupper, IntVar[][][] dXlower, IntVar[][][] dSBlower, IntVar[][][] proba, Step1Solution step1) {
    ArExpression objective = model.intVar(0);
    int lowerBoundObjective = 0;
    int upperBoundObjective = 0;
    for (int round = 0; round < step1.nbRounds; round++)
      for (int i = 0; i < 4; i++)
        for (int j = 0; j < 4; j++) {
          // Initialization of useful variables
          if (step1.DXupper[round][i][j] == 1 && step1.freeXupper[round][i][j] == 0)
            dXupper[round][i][j] = model.intVar("dXupper"+round+i+j, 1, sboxTables.getSboxSize()-1, false);
          if (step1.DXupper[round][i][j] == 1 && step1.freeSBupper[round][i][j] == 0)
            dSBupper[round][i][j] = model.intVar("dSBupper"+round+i+j, 1, sboxTables.getSboxSize()-1, false);
          if (step1.DXlower[round][i][j] == 1 && step1.freeXlower[round][i][j] == 0)
            dXlower[round][i][j] = model.intVar("dXlower"+round+i+j, 1, sboxTables.getSboxSize()-1, false);
          if (step1.DXlower[round][i][j] == 1 && step1.freeSBlower[round][i][j] == 0)
            dSBlower[round][i][j] = model.intVar("dSBlower"+round+i+j, 1, sboxTables.getSboxSize()-1, false);
          // Sbox constraints
          if (step1.DXupper[round][i][j] == 0) {
            if (step1.DXlower[round][i][j] == 1 && step1.freeXlower[round][i][j] == 0) { // DDT
              proba[round][i][j] = model.intVar("proba"+round+i+j, 2*probaFactor, 7*probaFactor, false);
              model.table(new IntVar[]{dXlower[round][i][j], dSBlower[round][i][j], proba[round][i][j]}, tableDDT).post();
              objective = objective.add(proba[round][i][j]);
              lowerBoundObjective = lowerBoundObjective + 2*probaFactor;
              upperBoundObjective = upperBoundObjective + 7*probaFactor;
            }
          }
          else if (step1.DXlower[round][i][j] == 0) {
            if (step1.DXupper[round][i][j] == 1 && step1.freeSBupper[round][i][j] == 0) { // DDT
              proba[round][i][j] = model.intVar("proba"+round+i+j, 2*probaFactor, 7*probaFactor, false);
              model.table(new IntVar[]{dXupper[round][i][j], dSBupper[round][i][j], proba[round][i][j]}, tableDDT).post();
              objective = objective.add(proba[round][i][j]);
              lowerBoundObjective = lowerBoundObjective + 2*probaFactor;
              upperBoundObjective = upperBoundObjective + 7*probaFactor;
            }
          }
          else {
            if (step1.freeSBupper[round][i][j] == 0 && step1.freeXlower[round][i][j] == 0) { // FBCT
              proba[round][i][j] = model.intVar("proba"+round+i+j, 2*probaFactor, 7*probaFactor, false);
              model.table(new IntVar[]{dXupper[round][i][j], dSBupper[round][i][j], dXlower[round][i][j], dSBlower[round][i][j], proba[round][i][j]}, tableFBCT).post();
              objective = objective.add(proba[round][i][j]);
              lowerBoundObjective = lowerBoundObjective + 2*probaFactor;
              upperBoundObjective = upperBoundObjective + 7*probaFactor;
            }
            else if (step1.freeSBupper[round][i][j] == 0 && step1.freeSBlower[round][i][j] == 0) { // UBCT
              proba[round][i][j] = model.intVar("proba"+round+i+j, 2*probaFactor, 7*probaFactor, false);
              model.table(new IntVar[]{dXupper[round][i][j], dSBupper[round][i][j], dSBlower[round][i][j], proba[round][i][j]}, tableUBCT).post();
              objective = objective.add(proba[round][i][j]);
              lowerBoundObjective = lowerBoundObjective + 2*probaFactor;
              upperBoundObjective = upperBoundObjective + 7*probaFactor;
            }
            else if (step1.freeXlower[round][i][j] == 0 && step1.freeXupper[round][i][j] == 0) { // LBCT
              proba[round][i][j] = model.intVar("proba"+round+i+j, 2*probaFactor, 7*probaFactor, false);
              model.table(new IntVar[]{dXupper[round][i][j], dXlower[round][i][j], dSBlower[round][i][j], proba[round][i][j]}, tableLBCT).post();
              objective = objective.add(proba[round][i][j]);
              lowerBoundObjective = lowerBoundObjective + 2*probaFactor;
              upperBoundObjective = upperBoundObjective + 7*probaFactor;
            }
            else if (step1.freeSBupper[round][i][j] == 0) { // DDT2 upper
              proba[round][i][j] = model.intVar("proba"+round+i+j, 4*probaFactor, 14*probaFactor, false);
              model.table(new IntVar[]{dXupper[round][i][j], dSBupper[round][i][j], proba[round][i][j]}, tableDDT2).post();
              objective = objective.add(proba[round][i][j]);
              lowerBoundObjective = lowerBoundObjective + 4*probaFactor;
              upperBoundObjective = upperBoundObjective + 14*probaFactor;
            }
            else if (step1.freeXlower[round][i][j] == 0) { // DDT2 lower
              proba[round][i][j] = model.intVar("proba"+round+i+j, 4*probaFactor, 14*probaFactor, false);
              model.table(new IntVar[]{dXlower[round][i][j], dSBlower[round][i][j], proba[round][i][j]}, tableDDT2).post();
              objective = objective.add(proba[round][i][j]);
              lowerBoundObjective = lowerBoundObjective + 4*probaFactor;
              upperBoundObjective = upperBoundObjective + 14*probaFactor;
            }
            else if (step1.freeXupper[round][i][j] == 0 && step1.freeSBlower[round][i][j] == 0) { // BCT
              proba[round][i][j] = model.intVar("proba"+round+i+j, 0, 7*probaFactor, false);
              model.table(new IntVar[]{dXupper[round][i][j], dSBlower[round][i][j], proba[round][i][j]}, tableBCT).post();
              objective = objective.add(proba[round][i][j]);
              upperBoundObjective = upperBoundObjective + 7*probaFactor;
            }
          }
        }
    IntVar objectiveVar = model.intVar("objectiveVar", lowerBoundObjective, upperBoundObjective);
    objective.eq(objectiveVar).post();
    return objectiveVar;
  }
  
  public void initializeY(IntVar[][][] dY, int[][][] freeSB) {
    for (int round = 0; round < dY.length; round++)
      for (int i = 0; i < 2; i++)
        for (int j = 0; j < 4; j++)
          if (freeSB[round][i][j] == 0)
            dY[round][i][j] = model.intVar("dY", 0, 255);
  }

  // -------- Linear part of the cipher --------
  public void addRoundTweakey(IntVar[][][] dSB, int[][][] DX, IntVar[][][] dTK12, int[][][] DTK12,IntVar[][][] dY,int[][][] freeSB) {
    for (int round = 0; round < dY.length; round++)
      for (int i = 0; i < 2; i++)
        for (int j = 0; j < 4; j++)
          if (freeSB[round][i][j] == 0)
            postXOR(dSB[round][i][j],DX[round][i][j]==0,dTK12[round][i][j],DTK12[round][i][j]==0,dY[round][i][j]);
  }

  public void shiftrowsMC(IntVar[][][] dX, IntVar[][][] dSB, int[][][] DX, IntVar[][][] dY, int[][][] freeX, int[][][] freeSB) {
    for (int round = 0; round < dY.length; round++)
      for (int j = 0; j < 4; j++) {
        if (freeX[round+1][0][j] == 0 && freeSB[round][3][(j+1)%4] == 0 && freeX[round+1][3][j] == 0)
          postXOR(dSB[round][3][(j+1)%4], DX[round][3][(j+1)%4]==0, dX[round+1][0][j], DX[round+1][0][j]==0, dX[round+1][3][j], DX[round+1][3][j]==0);
        if (freeX[round+1][1][j] == 0 && freeSB[round][0][j] == 0) {
          if (DX[round+1][1][j] == 0)
            dY[round][0][j].eq(0).post();
          else
            dY[round][0][j].eq(dX[round+1][1][j]).post();
        }
        if (freeX[round+1][2][j] == 0 && freeSB[round][2][(j+2)%4] == 0 && freeSB[round][1][(j+3)%4] == 0)
          postXOR(dX[round+1][2][j], DX[round+1][2][j]==0, dSB[round][2][(j+2)%4], DX[round][2][(j+2)%4]==0, dY[round][1][(j+3)%4]);
        if (freeX[round+1][3][j] == 0 && freeSB[round][0][j] == 0 && freeSB[round][2][(j+2)%4] == 0)
          postXOR(dSB[round][2][(j+2)%4], DX[round][2][(j+2)%4]==0, dX[round+1][3][j], DX[round+1][3][j]==0, dY[round][0][j]);
      }
  }

  public void shiftrowsMCSK(IntVar[][][] dX, IntVar[][][] dSB, int[][][] DX, int[][][] freeX, int[][][] freeSB) {
    for (int round = 0; round < dX.length-1; round++)
      for (int j = 0; j < 4; j++) {
        if (freeX[round+1][0][j] == 0 && freeSB[round][3][(j+1)%4] == 0 && freeX[round+1][3][j] == 0)
          postXOR(dSB[round][3][(j+1)%4], DX[round][3][(j+1)%4]==0, dX[round+1][0][j], DX[round+1][0][j]==0, dX[round+1][3][j], DX[round+1][3][j]==0);
        if (freeX[round+1][1][j] == 0 && freeSB[round][0][j] == 0) {
          if (DX[round+1][1][j] != 0)
            dSB[round][0][j].eq(dX[round+1][1][j]).post();
        }
        if (freeX[round+1][2][j] == 0 && freeSB[round][2][(j+2)%4] == 0 && freeSB[round][1][(j+3)%4] == 0)
          postXOR(dX[round+1][2][j], DX[round+1][2][j]==0, dSB[round][2][(j+2)%4], DX[round][2][(j+2)%4]==0, dSB[round][1][(j+3)%4], DX[round][1][(j+3)%4]==0);
        if (freeX[round+1][3][j] == 0 && freeSB[round][0][j] == 0 && freeSB[round][2][(j+2)%4] == 0)
          postXOR(dSB[round][2][(j+2)%4], DX[round][2][(j+2)%4]==0, dX[round+1][3][j], DX[round+1][3][j]==0, dSB[round][0][j], DX[round][0][j]==0);
      }
  }

  // -------- Many versions of XOR with and without the knowledge of a zero, general one, and general one with 4 parameters --------
  public static void postXOR(Model model, IntVar a, IntVar b, IntVar c) {
    model.table(new IntVar[]{a, b, c}, tableXor).post();
    nbXors++;
  }

  public static void postXOR(Model model, IntVar a, IntVar b, IntVar c, IntVar d) {
    IntVar tempVar = model.intVar("temp5Xor3", 0, 15); // cellsize=16
    model.table(new IntVar[]{a, b, tempVar}, tableXor).post();
    nbXors++;
    model.table(new IntVar[]{tempVar, c, d}, tableXor).post();
    nbXors++;
  }
  
  public static void postXOR(Model model, IntVar a, IntVar b, IntVar c, IntVar d, int maxVal) { // a+b+c+d = 0
    IntVar tempVar = model.intVar("temp4Xor", 0, maxVal); // Need to create a temp variable
    model.table(new IntVar[]{a, b, tempVar}, tableXor).post();
    nbXors++;
    model.table(new IntVar[]{tempVar, c, d}, tableXor).post();
    nbXors++;
  }

  public static void postXOR(Model model, IntVar a, IntVar b, IntVar c, IntVar d, IntVar e, int maxVal) { // a+b+c+d+e = 0
    IntVar tempVar1 = model.intVar("temp5Xor1", 0, maxVal); // Need to create a temp variable
    IntVar tempVar2 = model.intVar("temp5Xor2", 0, maxVal); // Need to create a temp variable
    model.table(new IntVar[]{a, b, tempVar1}, tableXor).post();
    nbXors++;
    model.table(new IntVar[]{tempVar1, c, tempVar2}, tableXor).post();
    nbXors++;
    model.table(new IntVar[]{tempVar2, d, e}, tableXor).post();
    nbXors++;
  }
  
  public void postXOR(IntVar a, IntVar b, IntVar c) {
    model.table(new IntVar[]{a, b, c}, tableXor).post();
    nbXors++;
  }

  private void postXOR(IntVar a, boolean a_zero, IntVar b, boolean b_zero, IntVar c) {
    if (a_zero) {
      if (b_zero)
        c.eq(0).post();
      else
        b.eq(c).post();
    }
    else if (b_zero) {
      if (a_zero)
        c.eq(0).post();
      else
        a.eq(c).post();
    }
    else
      postXOR(a, b, c);
  }

  private void postXOR(IntVar a, boolean a_zero, IntVar b, boolean b_zero, IntVar c, boolean c_zero) {
    if (a_zero) {
      if (!(b_zero || c_zero)) {
        b.eq(c).post();
      }
    }
    else if (b_zero) {
      if (!(a_zero || c_zero)) {
        a.eq(c).post();
      }
    }
    else if (c_zero) {
      if (!(a_zero || b_zero)) {
        a.eq(b).post();
      }
    }
    else
      postXOR(a, b, c);
  }
  
  // -------- Creation of Tables --------
  private static Tuples createRelationXor() {
    Tuples tuplesXor = new Tuples(true);
    for (int i = 0; i < 256; i++) {
      for (int j = 0; j < 256; j++) {
        tuplesXor.add(i, j, i ^ j);
      }
    }
    return tuplesXor;
  }
  
  private Tuples createRelationDDT(int factor) {
    Tuples tuplesSB = new Tuples(true);
    for (int gamma = 1; gamma < sboxTables.getSboxSize(); gamma++)
      for (int delta = 1; delta < sboxTables.getSboxSize(); delta++)
        if (sboxTables.ddt(gamma, delta) != 0)
          tuplesSB.add(gamma, delta, factor*probaExponent(sboxTables.ddt(gamma, delta)));
    return tuplesSB;
  }
  
  private Tuples createRelationBCT() {
    Tuples tuplesBCT = new Tuples(true);
    for (int gamma = 1; gamma < sboxTables.getSboxSize(); gamma++)
      for (int delta = 1; delta < sboxTables.getSboxSize(); delta++)
        if (sboxTables.bct(gamma, delta) != 0)
          tuplesBCT.add(gamma, delta, probaExponent(sboxTables.bct(gamma, delta)));
    return tuplesBCT;
  }
  
  private Tuples createRelationUBCT() {
    Tuples tuplesUBCT = new Tuples(true);
    sboxTables.getUBCTEntrySet().stream()
      .filter(entry -> (entry.getKey().getValue0() != 0 &&
                        entry.getKey().getValue1() != 0 &&
                        entry.getKey().getValue2() != 0))
      .forEach(entry -> addKeyValueInTable(tuplesUBCT, entry.getKey(), entry.getValue()));
    return tuplesUBCT;
  }

  private Tuples createRelationLBCT() {
    Tuples tuplesLBCT = new Tuples(true);
    sboxTables.getLBCTEntrySet().stream()
      .filter(entry -> (entry.getKey().getValue0() != 0 &&
                        entry.getKey().getValue1() != 0 &&
                        entry.getKey().getValue2() != 0))
      .forEach(entry -> addKeyValueInTable(tuplesLBCT, entry.getKey(), entry.getValue()));
    return tuplesLBCT;
  }
  
  private Tuples createRelationFBCT() {
    Tuples tuplesFBCT = new Tuples(true);
    sboxTables.getFBCTEntrySet().stream()
      .filter(entry -> (entry.getKey().getValue0() != 0 &&
                        entry.getKey().getValue1() != 0 &&
                        entry.getKey().getValue2() != 0 &&
                        entry.getKey().getValue3() != 0))
      .forEach(entry -> addKeyValueInTable(tuplesFBCT, entry.getKey(), entry.getValue()));
    return tuplesFBCT;
  }

  private void addKeyValueInTable(Tuples table, Triplet<Integer,Integer,Integer> key, int value) {
    table.add(key.getValue0(), key.getValue1(), key.getValue2(), probaExponent(value));
  }

  private void addKeyValueInTable(Tuples table, Quartet<Integer,Integer,Integer,Integer> key, int value) {
    table.add(key.getValue0(), key.getValue1(), key.getValue2(), key.getValue3(), probaExponent(value));
  }

  private int probaExponent(final int tableValue) {
    if (tableValue == 0 || tableValue > 256)
      throw new IllegalStateException("The value for this probability is not implemented, but it should not appear in the tables");
    return probaExponents[tableValue];
  }
}
