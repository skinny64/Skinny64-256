package boomerangsearch.step1;

import gurobi.*;

/**
 * A class dealing with the tweakey on different regimes.
 *
 * @author Mathieu Vavrille
 */
public class Step1Tweakey {
  private static final int[] invPermutationTKS = new int[]{2, 0, 4, 7, 6, 3, 5, 1};
  private final GRBModel model;
  private final int nbRounds;
  private final int regime;
  private final int upper;
  public  final GRBVar[][][] DTK;
  public  final GRBVar[] lanes;

  /**
   * @param model the gurobi model
   * @param nbRounds the number of rounds of the differential. The key will have nbRounds-1 states
   * @param regime the regime of the analysis. 1, 2, 3 or 4 for TK1, TK2, TK3 or TK4 (no key for SK)
   */
  public Step1Tweakey(final GRBModel model, final int nbRounds, final int regime, final int upper) throws GRBException {
    this.model = model;
    this.nbRounds = nbRounds;
    this.regime = regime;
    this.upper = upper;
    if (regime == 0)
      throw new IllegalStateException("You should not create a tweakey for SK (regime = 0)");
    DTK = new GRBVar[nbRounds-1][2][4];
    if (regime == 1) {
      createTK1();
      lanes = null;
    }
    else {
      if (upper == 1) {
          lanes = createTKnot1();
          //lanes = createTKnot1upper();
      }
      else {
          lanes = createTKnot1();
          //lanes = createTKnot1lower();
      }
    }
  }

  public void createTK1() throws GRBException {
    if (regime != 1)
      throw new IllegalStateException("You should use the createTK1 function only TK1");
    for (int i = 0; i < 2; i++)
      for (int j = 0; j < 4; j++)
        for (int round = 0; round < 2; round++)
          DTK[round][i][j] = model.addVar(0.0, 1.0, 0.0, GRB.BINARY, "DTK"+round+i+j);
    for (int round = 0; round < nbRounds-3; round++)
      for (int cell = 0; cell < 8; cell++) {
        int nextCell = invPermutationTKS[cell];
        DTK[round+2][nextCell/4][nextCell%4] = DTK[round][cell/4][cell%4]; // Same reference because same value in TK1
      }
  }

  public GRBVar[] createTKnot1() throws GRBException {
    if (regime == 1)
      throw new IllegalStateException("You should use the createTKnot1 function only TK2,TK3,TK4");
    for (int i = 0; i < 2; i++)
      for (int j = 0; j < 4; j++)
        for (int round = 0; round < nbRounds-1; round++)
          DTK[round][i][j] = model.addVar(0.0, 1.0, 0.0, GRB.BINARY, "DTK"+round+i+j);
    final GRBVar[] lanes = new GRBVar[16];
    final int firstRoundLanesSize = nbRounds/2;
    for (int i = 0; i < 8; i++) {
      int currentPos = i;
      GRBVar isUsedLane = model.addVar(0.0, 1.0, 0.0, GRB.BINARY, "lane"+i);
      lanes[i] = isUsedLane;
      GRBLinExpr sumLane = new GRBLinExpr();
      for (int round = 0; round < nbRounds-1; round = round+2) {
        sumLane.addTerm(1.0, DTK[round][currentPos/4][currentPos%4]);
        // Bound each value
        model.addConstr(DTK[round][currentPos/4][currentPos%4], GRB.LESS_EQUAL, isUsedLane, "lane"+i+round);
        currentPos = invPermutationTKS[currentPos];
      }
      //sumLane.addTerm((double) (regime-1-firstRoundLanesSize), isUsedLane); // TK2,TK3,TK4 v2
      sumLane.addTerm((double) (regime-firstRoundLanesSize), isUsedLane); // TK4 v1
      model.addConstr(sumLane, GRB.GREATER_EQUAL, 0.0, "sumLane"+i);
    }
    final int secondRoundLanesSize = (nbRounds-1)/2;
    for (int i = 0; i < 8; i++) {
      int currentPos = i;
      GRBVar isUsedLane = model.addVar(0.0, 1.0, 0.0, GRB.BINARY, "lane"+(i+8));
      lanes[i+8] = isUsedLane;
      GRBLinExpr sumLane = new GRBLinExpr();
      for (int round = 1; round < nbRounds-1; round = round+2) {
        sumLane.addTerm(1.0, DTK[round][currentPos/4][currentPos%4]);
        // Bound each value
        model.addConstr(DTK[round][currentPos/4][currentPos%4], GRB.LESS_EQUAL, isUsedLane, "lane"+(i+8)+round);
        currentPos = invPermutationTKS[currentPos];
      }
      //sumLane.addTerm((double) (regime-1-secondRoundLanesSize), isUsedLane); // TK2,TK3,TK4 v2
      sumLane.addTerm((double) (regime-secondRoundLanesSize), isUsedLane); // TK4 v1
      model.addConstr(sumLane, GRB.GREATER_EQUAL, 0.0, "sumLane"+(i+8));
    }
    return lanes;
  }
  //64-256-v1
  public GRBVar[] createTKnot1upper() throws GRBException {
    for (int i = 0; i < 2; i++)
      for (int j = 0; j < 4; j++)
        for (int round = 0; round < nbRounds-1; round++)
          DTK[round][i][j] = model.addVar(0.0, 1.0, 0.0, GRB.BINARY, "DTKupp"+round+i+j);
    final GRBVar[] lanes = new GRBVar[16];
    final int firstRoundLanesSize = nbRounds/2;
    for (int i = 0; i < 8; i++) {
      int currentPos = i;
      GRBVar isUsedLane = model.addVar(0.0, 1.0, 0.0, GRB.BINARY, "laneupp"+i);
      lanes[i] = isUsedLane;
      GRBLinExpr sumLane = new GRBLinExpr();
      int times = 0;
      int start = 1;
      for (int round = 0; round < nbRounds-1; round = round+2) {
        sumLane.addTerm(1.0, DTK[round][currentPos/4][currentPos%4]);
        // Bound each value
        //if (times == (0+start)%15 | times ==(1+start)%15 | times ==(2+start)%15 | times == (4+start)%15 | times == (5+start)%15 | times == (8+start)%15 | times == (10+start)%15) {
        if (times == (0+start)%15 | times ==(1+start)%15 | times ==(2+start)%15 | times == (3+start)%15 | times == (7+start)%15 | times == (10+start)%15 | times == (11+start)%15 | times == (13+start)%15) {
            model.addConstr(DTK[round][currentPos/4][currentPos%4], GRB.EQUAL, 0, "laneupp"+i+round); 
        }
        else {
            model.addConstr(DTK[round][currentPos/4][currentPos%4], GRB.EQUAL, isUsedLane, "laneupp"+i+round);
        }
        times++;
        currentPos = invPermutationTKS[currentPos];
      }
    }
    final int secondRoundLanesSize = (nbRounds-1)/2;
    for (int i = 0; i < 8; i++) {
      int currentPos = i;
      GRBVar isUsedLane = model.addVar(0.0, 1.0, 0.0, GRB.BINARY, "laneupp"+(i+8));
      lanes[i+8] = isUsedLane;
      GRBLinExpr sumLane = new GRBLinExpr();
      int times = 0;
      int start = 1;
      for (int round = 1; round < nbRounds-1; round = round+2) {
        sumLane.addTerm(1.0, DTK[round][currentPos/4][currentPos%4]);
        // Bound each value
        //if (times == (0+start)%15 | times ==(1+start)%15 | times ==(2+start)%15 | times == (4+start)%15 | times == (5+start)%15 | times == (8+start)%15 | times == (10+start)%15) {
        //if (times == (0+start)%15 | times ==(1+start)%15 | times ==(2+start)%15 | times == (3+start)%15 | times == (7+start)%15 | times == (10+start)%15 | times == (11+start)%15 | times == (13+start)%15) {
             //model.addConstr(DTK[round][currentPos/4][currentPos%4], GRB.EQUAL, 0, "laneupp"+i+round); 
         //}
        //else {
            model.addConstr(DTK[round][currentPos/4][currentPos%4], GRB.EQUAL, isUsedLane, "laneupp"+(i+8)+round);
        //}
        times++;
        currentPos = invPermutationTKS[currentPos];
      }
    }
    return lanes;
  }

  public GRBVar[] createTKnot1lower() throws GRBException {
    for (int i = 0; i < 2; i++)
      for (int j = 0; j < 4; j++)
        for (int round = 0; round < nbRounds-1; round++)
          DTK[round][i][j] = model.addVar(0.0, 1.0, 0.0, GRB.BINARY, "DTKulow"+round+i+j);
    final GRBVar[] lanes = new GRBVar[16];
    final int firstRoundLanesSize = nbRounds/2;
    for (int i = 0; i < 8; i++) {
      int currentPos = i;
      GRBVar isUsedLane = model.addVar(0.0, 1.0, 0.0, GRB.BINARY, "lanelow"+i);
      lanes[i] = isUsedLane;
      GRBLinExpr sumLane = new GRBLinExpr();
      int times = 0;
      int start = 10;
      for (int round = 0; round < nbRounds-1; round = round+2) {
        sumLane.addTerm(1.0, DTK[round][currentPos/4][currentPos%4]);
        // Bound each value
        //if (times == (0+start)%15 | times ==(1+start)%15 | times ==(2+start)%15 | times == (4+start)%15 | times == (5+start)%15 | times == (8+start)%15 | times == (10+start)%15) {
        if (times == (0+start)%15 | times ==(1+start)%15 | times ==(2+start)%15 | times == (3+start)%15 | times == (7+start)%15 | times == (10+start)%15 | times == (11+start)%15 | times == (13+start)%15) {
            model.addConstr(DTK[round][currentPos/4][currentPos%4], GRB.EQUAL, 0, "lanelow"+i+round); 
        }
        else {
            model.addConstr(DTK[round][currentPos/4][currentPos%4], GRB.EQUAL, isUsedLane, "lanelow"+i+round);
        }
        times++;
        currentPos = invPermutationTKS[currentPos];
      }
    }
    final int secondRoundLanesSize = (nbRounds-1)/2;
    for (int i = 0; i < 8; i++) {
      int currentPos = i;
      GRBVar isUsedLane = model.addVar(0.0, 1.0, 0.0, GRB.BINARY, "lanelow"+(i+8));
      lanes[i+8] = isUsedLane;
      GRBLinExpr sumLane = new GRBLinExpr();
      int times = 0;
      int start = 1;
      for (int round = 1; round < nbRounds-1; round = round+2) {
        sumLane.addTerm(1.0, DTK[round][currentPos/4][currentPos%4]);
        // Bound each value
        //if (times == (0+start)%15 | times ==(1+start)%15 | times ==(2+start)%15 | times == (4+start)%15 | times == (5+start)%15 | times == (8+start)%15 | times == (10+start)%15) {
        //if (times == (0+start)%15 | times ==(1+start)%15 | times ==(2+start)%15 | times == (3+start)%15 | times == (7+start)%15 | times == (10+start)%15 | times == (11+start)%15 | times == (13+start)%15) {
            //model.addConstr(DTK[round][currentPos/4][currentPos%4], GRB.EQUAL, 0, "lanelow"+i+round); 
         //}
        //else {
            model.addConstr(DTK[round][currentPos/4][currentPos%4], GRB.EQUAL, isUsedLane, "lanelow"+(i+8)+round);
        //}
        times++;
        currentPos = invPermutationTKS[currentPos];
      }
    }
    return lanes;
  } 


  public Step1SolutionTweakey getValue() throws GRBException {
    int[][][] DTKValue = new int[nbRounds-1][2][4];
    int[] lanesValue   = new int[16];
    for (int round = 0; round < nbRounds-1; round++)
      for (int i = 0; i < 2; i++)
        for (int j = 0; j < 4; j++)
            DTKValue[round][i][j] = (int) Math.round(DTK[round][i][j].get(GRB.DoubleAttr.Xn));
    if (regime > 1)
      for (int i = 0; i < 16; i++)
        lanesValue[i] = (int) Math.round(lanes[i].get(GRB.DoubleAttr.Xn));
    if (regime == 1) {
      for (int i = 0; i < 8; i++) {
        lanesValue[i]   = DTKValue[0][i/4][i%4];
        lanesValue[i+8] = DTKValue[1][i/4][i%4];
      }
    }
    return new Step1SolutionTweakey(DTKValue, lanesValue);
  }
}
