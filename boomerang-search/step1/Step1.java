package boomerangsearch.step1;

import gurobi.*;

import java.util.List;
import java.util.ArrayList;
import java.util.stream.IntStream;
import java.util.stream.Collectors;
import java.io.IOException;
import java.io.File;

public class Step1 {
  private final int nbRounds;
  private final int nExtbRounds;
  private final int nExtfRounds;
  private final int regime;
  private final int blocksize;
  private final GRBModel model;
  private final Step1Factory factory;
  private final GRBVar[][][] DXupper;
  private final GRBVar[][][] freeXupper;
  private final GRBVar[][][] freeSBupper;
  private final Step1Tweakey DTKupper;
  private final GRBVar[][][] DXlower;
  private final GRBVar[][][] freeXlower;
  private final GRBVar[][][] freeSBlower;
  private final Step1Tweakey DTKlower;
  private final GRBVar[][][] isDDT2;
  private final GRBVar[][][] isTable;
  private final GRBLinExpr   objective;
  
  /*Ef*/
  private static final int[] invPermutationTKS = new int[]{2, 0, 4, 7, 6, 3, 5, 1};
  private final GRBVar[][][] DXlowExt;
  private final GRBVar[][][] DSTKlowExt;
  private final GRBVar[][][] DKnownDeclowExt;

  /*Eb*/
  private final GRBVar[][][] DXuppExt;
  private final GRBVar[][][] DSTKuppExt;
  private final GRBVar[][][] DKnownuppExt;
  private final GRBVar[][][] DKnownEncuppExt;

  /*fastfilter*/
  private final GRBVar[][][] DXFixedlowExt;
  private final GRBVar[][][] DWFixedlowExt;
  private final GRBVar[][][] DXFilterlowExt;
  private final GRBVar[][][] DWFilterlowExt;
  private final GRBVar[][][] DXisFilterlowExt;
  private final GRBVar[][][] DWisFilterlowExt;
  private final GRBVar[][][] DXGuesslowExt;
  private final GRBVar[][][] DWGuesslowExt;
  
  private final GRBVar[] obj;
  private final GRBVar[] advantage;
  private final GRBVar[] xmemory;

  /**
   * @param env the Gurobi environment
   * @param nbRounds the number of rounds of the boomerang
   * @param regime the regime of the analysis. 0, 1, 2, 3 or 4 for SK, TK1, TK2, TK3 or TK4
   */
  public Step1(final GRBEnv env, final int nbRounds, final int nExtbRounds, final int nExtfRounds, final int regime, final int blocksize) throws GRBException {
    model = new GRBModel(env);
    this.nbRounds = nbRounds;
    this.nExtbRounds = nExtbRounds;
    this.nExtfRounds = nExtfRounds;
    this.regime = regime;
    this.blocksize = blocksize;

    factory = new Step1Factory(model, regime);
    DXupper = new GRBVar[nbRounds+1][4][4];
    freeXupper = new GRBVar[nbRounds][4][4];
    freeSBupper = new GRBVar[nbRounds][4][4];
    DTKupper = (regime == 0) ? null : new Step1Tweakey(model, nbRounds+1, regime, 1);
    DXlower = new GRBVar[nbRounds+1][4][4];
    freeXlower = new GRBVar[nbRounds][4][4];
    freeSBlower = new GRBVar[nbRounds][4][4];
    DTKlower = (regime == 0) ? null : new Step1Tweakey(model, nbRounds+1, regime, 0);
    isDDT2 = new GRBVar[nbRounds][4][4];
    isTable = new GRBVar[nbRounds][4][4];
    
    double weightmk = 1;
    double weighthf = 1;
    double consT1 = 0;
    double consT2 = 0;
    if (blocksize == 4) {
        weightmk = 4.0;
        weighthf = -8.0;
        consT1 = 33;
        consT2 = 64;
    } 
    else if (blocksize == 8) {
        weightmk = 8.0;
        weighthf = -16.0;
        consT1 = 65;
        consT2 = 128;
    }

    /*Ef*/
    DXlowExt = new GRBVar[nExtfRounds][4][4];
    DSTKlowExt = new GRBVar[nExtfRounds][2][4];
    DKnownDeclowExt = new GRBVar[nExtfRounds][4][4];
    /*Eb*/
    DXuppExt = new GRBVar[nExtbRounds+1][4][4];
    DSTKuppExt = new GRBVar[nExtbRounds][2][4];
    DKnownuppExt = new GRBVar[nExtbRounds][4][4];
    DKnownEncuppExt = new GRBVar[nExtbRounds][4][4];
    /*ADD fastfilter*/
    DXFixedlowExt = new GRBVar[nExtfRounds][4][4];
    DWFixedlowExt = new GRBVar[nExtfRounds][4][4];
    DXFilterlowExt = new GRBVar[nExtfRounds][4][4];
    DWFilterlowExt = new GRBVar[nExtfRounds][4][4];
    DXisFilterlowExt = new GRBVar[nExtfRounds][4][4];
    DWisFilterlowExt = new GRBVar[nExtfRounds][4][4];
    DXGuesslowExt = new GRBVar[nExtfRounds][4][4];
    DWGuesslowExt = new GRBVar[nExtfRounds][4][4];

    obj = new GRBVar[1];
    advantage = new GRBVar[1];
    xmemory = new GRBVar[1];

    // Initialization
    for (int round = 0; round < nbRounds; round++)
      for (int i = 0; i < 4; i++)
        for (int j = 0; j < 4; j++) {
          DXupper[round][i][j]     = model.addVar(0.0, 1.0, 0.0, GRB.BINARY, "DXupper"+round+i+j);
          freeXupper[round][i][j]  = model.addVar(0.0, 1.0, (round<=nbRounds/2)? 0.0 : 1.0, GRB.BINARY, "freeXupper"+round+i+j);
          freeSBupper[round][i][j] = model.addVar(0.0, 1.0, (round<=nbRounds/2)? 0.0 : 1.0, GRB.BINARY, "freeSBupper"+round+i+j);
          DXlower[round][i][j]     = model.addVar(0.0, 1.0, 0.0, GRB.BINARY, "DXlower"+round+i+j);
          freeXlower[round][i][j]  = model.addVar(0.0, 1.0, (round>=nbRounds/2)? 0.0 : 1.0, GRB.BINARY, "freeXlower"+round+i+j);
          freeSBlower[round][i][j] = model.addVar(0.0, 1.0, (round>=nbRounds/2)? 0.0 : 1.0, GRB.BINARY, "freeSBlower"+round+i+j);
          isDDT2[round][i][j] = model.addVar(0.0, 1.0, 0.0, GRB.BINARY, "isDDT2"+round+i+j);
          isTable[round][i][j] = model.addVar(0.0, 1.0, 0.0, GRB.BINARY, "isTable"+round+i+j);          
          }
    for (int i = 0; i < 4; i++)
        for (int j = 0; j < 4; j++) {
          DXupper[nbRounds][i][j]     = model.addVar(0.0, 1.0, 0.0, GRB.BINARY, "DXupper"+nbRounds+i+j);
          DXlower[nbRounds][i][j]     = model.addVar(0.0, 1.0, 0.0, GRB.BINARY, "DXlower"+nbRounds+i+j);        
          }

    obj[0] = model.addVar(0.0, GRB.INFINITY, 0.0, GRB.INTEGER, "obj0");
    advantage[0] = model.addVar(0.0, GRB.INFINITY, 0.0, GRB.INTEGER, "advantage0");
    xmemory[0] = model.addVar(0.0, GRB.INFINITY, 0.0, GRB.INTEGER, "xmemory0");

    /*Ef*/
    for (int round = 0; round < nExtfRounds; round++)
        for (int i = 0; i < 4; i++)
          for (int j = 0; j < 4; j++) {
        	  DXlowExt[round][i][j] = model.addVar(0.0, 1.0, 0.0, GRB.BINARY, "DXlowExt"+round+i+j);
                  DKnownDeclowExt[round][i][j] = model.addVar(0.0, 1.0, 0.0, GRB.BINARY, "DKnownDeclowExt"+round+i+j);
                  /*fastfilter*/
                  DXFixedlowExt[round][i][j] = model.addVar(0.0, 1.0, 0.0, GRB.BINARY, "DXFixedlowExt"+round+i+j);
                  DWFixedlowExt[round][i][j] = model.addVar(0.0, 1.0, 0.0, GRB.BINARY, "DWFixedlowExt"+round+i+j);
                  DXFilterlowExt[round][i][j] = model.addVar(0.0, 1.0, 0.0, GRB.BINARY, "DXFilterlowExt"+round+i+j);
                  DWFilterlowExt[round][i][j] = model.addVar(0.0, 1.0, 0.0, GRB.BINARY, "DWFilterlowExt"+round+i+j);
                  DXisFilterlowExt[round][i][j] = model.addVar(0.0, 1.0, 0.0, GRB.BINARY, "DXisFilterlowExt"+round+i+j);
                  DWisFilterlowExt[round][i][j] = model.addVar(0.0, 1.0, 0.0, GRB.BINARY, "DWisFilterlowExt"+round+i+j);
                  DXGuesslowExt[round][i][j] = model.addVar(0.0, 1.0, 0.0, GRB.BINARY, "DXGuesslowExt"+round+i+j);
                  DWGuesslowExt[round][i][j] = model.addVar(0.0, 1.0, 0.0, GRB.BINARY, "DWGuesslowExt"+round+i+j);
        	  if (i<2)
        		  DSTKlowExt[round][i][j] = model.addVar(0.0, 1.0, 0.0, GRB.BINARY, "DSTKlowExt"+round+i+j);
          }
    
    /*Eb*/
    for (int round = 0; round < nExtbRounds+1; round++)
        for (int i = 0; i < 4; i++)
          for (int j = 0; j < 4; j++) {
        	  DXuppExt[round][i][j] = model.addVar(0.0, 1.0, 0.0, GRB.BINARY, "DXuppExt"+round+i+j);
        	  if (round<nExtbRounds)
		  {
                      DKnownuppExt[round][i][j] = model.addVar(0.0, 1.0, 0.0, GRB.BINARY, "DKnownuppExt"+round+i+j);
		      DKnownEncuppExt[round][i][j] = model.addVar(0.0, 1.0, 0.0, GRB.BINARY, "DKnownEncuppExt"+round+i+j);
		      if(i<2)
        		  DSTKuppExt[round][i][j] = model.addVar(0.0, 1.0, 0.0, GRB.BINARY, "DSTKuppExt"+round+i+j);
		  }
          }

    // Constraints
    if (regime == 0) {
      System.out.println("TODO, can replace the equality a=f in MC by a reference");
      factory.addLinearSK(DXupper);
      factory.addLinearSK(DXlower);
      factory.removeSymmetriesSK(DXupper[0]);
      model.addConstr(factory.sumState(DXupper[0]), GRB.GREATER_EQUAL, 1, "");
      model.addConstr(factory.sumState(DXlower[nbRounds-1]), GRB.GREATER_EQUAL, 1, "");
    }
    else {
      factory.addLinear(DXupper, DTKupper.DTK);
      factory.addLinear(DXlower, DTKlower.DTK);
    }

    factory.freePropagationUpper(freeXupper, freeSBupper, DXupper);
    factory.freePropagationLower(freeXlower, freeSBlower, DXlower);

    factory.objectiveConstraints(DXupper, freeXupper, freeSBupper, DXlower, freeXlower, freeSBlower, isTable, isDDT2);

    factory.addKnownDiffBounds(DXupper);
    factory.addKnownDiffBounds(DXlower);
    
    /*Ef*/
    factory.addlowExtLinear(DXlowExt,DSTKlowExt);
    factory.addlowExtDec(DXlowExt,DKnownDeclowExt);

    for (int i = 0; i < 4; i++)
        for (int j = 0; j < 4; j++) {
        	model.addConstr(DXlowExt[0][i][j], GRB.EQUAL, DXlower[nbRounds][i][j],"");
        }
    for (int i = 0; i < 8; i++) {
        int currentPos = i;
        for (int round = 0; round < nbRounds+nExtfRounds; round = round+2) {
             if (round >= nbRounds) {
                   model.addConstr(DSTKlowExt[round-nbRounds][currentPos/4][currentPos%4], GRB.EQUAL, DTKlower.lanes[i], "");
             }
          currentPos = invPermutationTKS[currentPos];
        }
    }
    for (int i = 0; i < 8; i++) {
        int currentPos = i;
        for (int round = 1; round < nbRounds+nExtfRounds; round = round+2) {
        	if (round >= nbRounds) {
                        model.addConstr(DSTKlowExt[round-nbRounds][currentPos/4][currentPos%4], GRB.EQUAL, DTKlower.lanes[i+8], "");
                }
            currentPos = invPermutationTKS[currentPos];
        }
      }


    /*Eb*/
    factory.adduppExtLinear(DXuppExt,DSTKuppExt);
    for (int i = 0; i < 4; i++)
        for (int j = 0; j < 4; j++) {
        	model.addConstr(DXuppExt[nExtbRounds][i][j], GRB.EQUAL, DXupper[0][i][j],"");
        }
    for (int i = 0; i < 8; i++) {
        int currentPos = i;
        for (int round = nExtbRounds-1; round > -1; round = round-2) {
          currentPos = invPermutationTKS[currentPos];
          model.addConstr(DSTKuppExt[round][i/4][i%4], GRB.EQUAL, DTKupper.lanes[currentPos+8], "");        
        }
    }
    for (int i = 0; i < 8; i++) {
        int currentPos = i;
        for (int round = nExtbRounds-2; round > -1; round = round-2) {
          currentPos = invPermutationTKS[currentPos];
          model.addConstr(DSTKuppExt[round][i/4][i%4], GRB.EQUAL, DTKupper.lanes[currentPos], "");        
        }
    }
    for (int i = 0; i < 4; i++)
        for (int j = 0; j < 4; j++) {
        	model.addConstr(DKnownuppExt[nExtbRounds-1][i][j], GRB.EQUAL, DXuppExt[nExtbRounds-1][i][j],"");
        }
    factory.adduppExtEnc(DKnownuppExt,DKnownEncuppExt,DXuppExt);
    
    /*fastfilter*/
    factory.addDFixedlowExtLinear(DXFixedlowExt, DWFixedlowExt, DXlowExt);
    factory.addWfilterlowExt(DWFixedlowExt,DWFilterlowExt);
    factory.addXfilterlowExt(DXFixedlowExt,DXlowExt,DXFilterlowExt);
    factory.addXguesslowExt(DXGuesslowExt,DWGuesslowExt,DXisFilterlowExt,DWisFilterlowExt);
    for (int round = 0; round < nExtfRounds; round++)
        for (int i = 0; i < 4; i++)
            for (int j = 0; j < 4; j++) {
              model.addConstr(DXisFilterlowExt[round][i][j], GRB.LESS_EQUAL, DXFilterlowExt[round][i][j],"");
              model.addConstr(DWisFilterlowExt[round][i][j], GRB.LESS_EQUAL, DWFilterlowExt[round][i][j],"");
        }

    /*active<15*/
    GRBLinExpr sumActive = new GRBLinExpr();
    for (int i = 0; i < 4; i++)
        for (int j = 0; j < 4; j++) {
          sumActive.addTerm(1.0, DXuppExt[1][i][j]);
        }
    model.addConstr(sumActive, GRB.LESS_EQUAL, 15.0, "");
    
    /*Upper bound of distinguisher*/
    factory.addUpperBound(isTable,isDDT2,blocksize,regime,nbRounds);

    
    /*bound of mb, mf, m'f and hf*/
    GRBLinExpr mb = new GRBLinExpr();
    GRBLinExpr mfg = new GRBLinExpr();
    GRBLinExpr hf = new GRBLinExpr();
    GRBLinExpr mb_mf = new GRBLinExpr();
    GRBLinExpr mb_mfg = new GRBLinExpr();
    GRBLinExpr mb_mfg_hf = new GRBLinExpr();
    for (int round = 0; round < nExtbRounds-1; round++)
        for (int i = 0; i < 2; i++)
            for (int j = 0; j < 4; j++) {
              mb_mf.addTerm(weightmk, DKnownEncuppExt[round][i][j]);
              mb.addTerm(1, DKnownEncuppExt[round][i][j]);
              mb_mfg.addTerm(weightmk, DKnownEncuppExt[round][i][j]);
              mb_mfg_hf.addTerm(weightmk, DKnownEncuppExt[round][i][j]);
        }
    for (int round = 0; round < nExtfRounds; round++)
        for (int i = 0; i < 2; i++)
            for (int j = 0; j < 4; j++) {
              mb_mf.addTerm(weightmk, DKnownDeclowExt[round][i][j]);
              mb_mfg.addTerm(weightmk, DXGuesslowExt[round][i][j]);
              mfg.addTerm(1, DXGuesslowExt[round][i][j]);
              mb_mfg_hf.addTerm(weightmk, DXGuesslowExt[round][i][j]);
        }
    for (int round = 0; round < nExtfRounds; round++)
        for (int i = 0; i < 4; i++)
            for (int j = 0; j < 4; j++) {
              hf.addTerm(1, DXisFilterlowExt[round][i][j]);
              hf.addTerm(1, DWisFilterlowExt[round][i][j]);
              mb_mfg_hf.addTerm(weighthf, DXisFilterlowExt[round][i][j]);
              mb_mfg_hf.addTerm(weighthf, DWisFilterlowExt[round][i][j]);
        }
    //model.addConstr(hf, GRB.GREATER_EQUAL, 3, "");
    //model.addConstr(mb_mfg, GRB.LESS_EQUAL, (regime-1)*blocksize*16, "");
    //model.addConstr(mb_mfg_hf, GRB.LESS_EQUAL, (regime-2)*blocksize*16, "");
    //model.addConstr(mb_mfg, GRB.LESS_EQUAL, (regime-2)*blocksize*16, "");
    //model.addConstr(mb_mfg_hf, GRB.LESS_EQUAL, (regime-3)*blocksize*16, "");

   
    
    mb_mf.addTerm(-1.0, xmemory[0]);
    //h<mb+mf-x
    model.addConstr(advantage[0], GRB.LESS_EQUAL, mb_mf, "");
    //x<mb+m'f
    model.addConstr(xmemory[0], GRB.LESS_EQUAL, mb_mfg, "");
    /*upperbound of memory*/
    //model.addConstr(mb_mf, GRB.LESS_EQUAL, blocksize*16+20, "");    
    
    /*articleTrails*/
    articleTrails();   

    objective = new GRBLinExpr();
    GRBLinExpr objective1 = new GRBLinExpr();
    GRBLinExpr objective2 = new GRBLinExpr();
    GRBLinExpr objective3 = new GRBLinExpr();
    for (int round = 0; round < nbRounds; round++)
      for (int i = 0; i < 4; i++)
        for (int j = 0; j < 4; j++) {
          objective1.addTerm(1.0, isTable[round][i][j]);
          objective1.addTerm(1.0, isDDT2[round][i][j]);
          objective2.addTerm(2.0, isTable[round][i][j]);
          objective2.addTerm(2.0, isDDT2[round][i][j]);
        }
    for (int round = 0; round < nExtbRounds-1; round++)
        for (int i = 0; i < 2; i++)
            for (int j = 0; j < 4; j++) {
              objective1.addTerm(weightmk, DKnownEncuppExt[round][i][j]);
              objective2.addTerm(weightmk, DKnownEncuppExt[round][i][j]);
        }
    for (int round = 0; round < nExtfRounds; round++)
        for (int i = 0; i < 2; i++)
            for (int j = 0; j < 4; j++) {
              objective1.addTerm(weightmk, DXGuesslowExt[round][i][j]);
              objective2.addTerm(weightmk, DXGuesslowExt[round][i][j]);
        }
    for (int round = 0; round < nExtfRounds; round++)
        for (int i = 0; i < 4; i++)
            for (int j = 0; j < 4; j++) {
              objective2.addTerm(weighthf, DXisFilterlowExt[round][i][j]);
              objective2.addTerm(weighthf, DWisFilterlowExt[round][i][j]);
        } 
    objective1.addConstant(consT1);
    objective2.addConstant(consT2);
    objective3.addConstant(regime*blocksize*16);
    objective3.addTerm(-1.0, advantage[0]);
    objective.addTerm(1.0, obj[0]);
    model.addConstr(objective, GRB.GREATER_EQUAL, objective1, "");
    model.addConstr(objective, GRB.GREATER_EQUAL, objective2, "");
    model.addConstr(objective, GRB.GREATER_EQUAL, objective3, "");
 
    model.setObjective(objective, GRB.MINIMIZE);
  }

  public List<Step1Solution> solve(final int nbSolutions, final boolean nonOptimalSolutions, final int minObjValue, final int nbThreads) throws GRBException {
    model.read("tune1.prm");
    model.write("model.lp");
    model.set(GRB.IntParam.Threads, nbThreads);
    if (minObjValue != -1)
      model.addConstr(objective, GRB.GREATER_EQUAL, minObjValue, "objectiveFix");
    //GRBConstr c0 = model.addConstr(freeXlower[nbRounds/2][0][0], GRB.EQUAL, 1.0, "");
    //GRBConstr c1 = model.addConstr(freeSBupper[nbRounds/2][0][0], GRB.EQUAL, 1.0, "");
    //model.optimize();
    //model.remove(c0);
    //model.remove(c1);
    /*model.set(GRB.DoubleParam.PoolGap, (nonOptimalSolutions) ? 1.0 : 0.005);
    model.set(GRB.IntParam.PoolSolutions, nbSolutions);
    model.set(GRB.IntParam.PoolSearchMode, 2);*/
    model.set(GRB.IntParam.DualReductions, 0);
    model.optimize();
    //model.write("output.sol");
    //model.computeIIS();
    //model.write("model1.ilp");
    return getAllFoundSolutions();
  }

  public void dispose() throws GRBException {
    model.dispose();
  }

  public List<Step1Solution> getAllFoundSolutions() throws GRBException {
    return IntStream.range(0, model.get(GRB.IntAttr.SolCount)).boxed()
      .map(solNb -> getSolution(solNb))
      .collect(Collectors.toList());
  }

  private Step1Solution getSolution(final int solutionNumber) {
    try {
      model.set(GRB.IntParam.SolutionNumber, solutionNumber);
      int[][][] DXupperValue     = new int[nbRounds+1][4][4];
      int[][][] freeXupperValue  = new int[nbRounds][4][4];
      int[][][] freeSBupperValue = new int[nbRounds][4][4];
      int[][][] DXlowerValue     = new int[nbRounds+1][4][4];
      int[][][] freeXlowerValue  = new int[nbRounds][4][4];
      int[][][] freeSBlowerValue = new int[nbRounds][4][4];
      int[][][] isTableValue     = new int[nbRounds][4][4];
      int[][][] isDDT2Value      = new int[nbRounds][4][4];
      /*Ef*/
      int[][][] DXlowExtValue    = new int[nExtfRounds][4][4];
      int[][][] DSTKlowExtValue    = new int[nExtfRounds][2][4];
      int[][][] DKnownDeclowExtValue    = new int[nExtfRounds][4][4];
      /*Eb*/
      int[][][] DXuppExtValue    = new int[nExtbRounds+1][4][4];
      int[][][] DSTKuppExtValue    = new int[nExtbRounds][2][4];
      int[][][] DKnownuppExtValue    = new int[nExtbRounds][4][4];
      int[][][] DKnownEncuppExtValue    = new int[nExtbRounds][4][4];

      /*fastfilter*/
      int[][][] DXFixedlowExtValue    = new int[nExtfRounds][4][4];
      int[][][] DWFixedlowExtValue    = new int[nExtfRounds][4][4];
      int[][][] DXFilterlowExtValue   = new int[nExtfRounds][4][4];
      int[][][] DWFilterlowExtValue   = new int[nExtfRounds][4][4];
      int[][][] DXisFilterlowExtValue = new int[nExtfRounds][4][4];
      int[][][] DWisFilterlowExtValue = new int[nExtfRounds][4][4];
      int[][][] DXGuesslowExtValue    = new int[nExtfRounds][4][4];
      int[][][] DWGuesslowExtValue    = new int[nExtfRounds][4][4];

   

      /*Ef*/
      for (int round = 0; round < nExtfRounds; round++)
          for (int i = 0; i < 4; i++)
            for (int j = 0; j < 4; j++) {
            	DXlowExtValue[round][i][j] = (int) Math.round(DXlowExt[round][i][j].get(GRB.DoubleAttr.Xn));
                DKnownDeclowExtValue[round][i][j] = (int) Math.round(DKnownDeclowExt[round][i][j].get(GRB.DoubleAttr.Xn));
                DXFixedlowExtValue[round][i][j] = (int) Math.round(DXFixedlowExt[round][i][j].get(GRB.DoubleAttr.Xn));
                DWFixedlowExtValue[round][i][j] = (int) Math.round(DWFixedlowExt[round][i][j].get(GRB.DoubleAttr.Xn));
                DXFilterlowExtValue[round][i][j] = (int) Math.round(DXFilterlowExt[round][i][j].get(GRB.DoubleAttr.Xn));
                DWFilterlowExtValue[round][i][j] = (int) Math.round(DWFilterlowExt[round][i][j].get(GRB.DoubleAttr.Xn));  
                DXisFilterlowExtValue[round][i][j] = (int) Math.round(DXisFilterlowExt[round][i][j].get(GRB.DoubleAttr.Xn));
                DWisFilterlowExtValue[round][i][j] = (int) Math.round(DWisFilterlowExt[round][i][j].get(GRB.DoubleAttr.Xn));  
                DXGuesslowExtValue[round][i][j] = (int) Math.round(DXGuesslowExt[round][i][j].get(GRB.DoubleAttr.Xn));
                DWGuesslowExtValue[round][i][j] = (int) Math.round(DWGuesslowExt[round][i][j].get(GRB.DoubleAttr.Xn)); 
            	if (i<2)
            		DSTKlowExtValue[round][i][j] = (int) Math.round(DSTKlowExt[round][i][j].get(GRB.DoubleAttr.Xn));
            }
   
      /*Eb*/
      for (int round = 0; round < nExtbRounds; round++)
          for (int i = 0; i < 4; i++)
            for (int j = 0; j < 4; j++) {
            	DXuppExtValue[round][i][j] = (int) Math.round(DXuppExt[round][i][j].get(GRB.DoubleAttr.Xn));
                DKnownuppExtValue[round][i][j] = (int) Math.round(DKnownuppExt[round][i][j].get(GRB.DoubleAttr.Xn));
                DKnownEncuppExtValue[round][i][j] = (int) Math.round(DKnownEncuppExt[round][i][j].get(GRB.DoubleAttr.Xn));
            	if (i<2)
            		DSTKuppExtValue[round][i][j] = (int) Math.round(DSTKuppExt[round][i][j].get(GRB.DoubleAttr.Xn));
            }
      for (int i = 0; i < 4; i++)
            for (int j = 0; j < 4; j++) {
                DXuppExtValue[nExtbRounds][i][j] = (int) Math.round(DXuppExt[nExtbRounds][i][j].get(GRB.DoubleAttr.Xn));
      }
      for (int round = 0; round < nbRounds; round++)
        for (int i = 0; i < 4; i++)
          for (int j = 0; j < 4; j++) {
            DXupperValue[round][i][j] = (int) Math.round(DXupper[round][i][j].get(GRB.DoubleAttr.Xn));
            freeXupperValue[round][i][j] = (int) Math.round(freeXupper[round][i][j].get(GRB.DoubleAttr.Xn));
            freeSBupperValue[round][i][j] = (int) Math.round(freeSBupper[round][i][j].get(GRB.DoubleAttr.Xn));
            DXlowerValue[round][i][j] = (int) Math.round(DXlower[round][i][j].get(GRB.DoubleAttr.Xn));
            freeXlowerValue[round][i][j] = (int) Math.round(freeXlower[round][i][j].get(GRB.DoubleAttr.Xn));
            freeSBlowerValue[round][i][j] = (int) Math.round(freeSBlower[round][i][j].get(GRB.DoubleAttr.Xn));
            isTableValue[round][i][j] = (int) Math.round(isTable[round][i][j].get(GRB.DoubleAttr.Xn));
            isDDT2Value[round][i][j] = (int) Math.round(isDDT2[round][i][j].get(GRB.DoubleAttr.Xn));
          }
      for (int i = 0; i < 4; i++)
          for (int j = 0; j < 4; j++) {
            DXupperValue[nbRounds][i][j] = (int) Math.round(DXupper[nbRounds][i][j].get(GRB.DoubleAttr.Xn));       
            DXlowerValue[nbRounds][i][j] = (int) Math.round(DXlower[nbRounds][i][j].get(GRB.DoubleAttr.Xn));            
          }
      return new Step1Solution(nbRounds, nExtbRounds, nExtfRounds, regime, (int) Math.round(model.get(GRB.DoubleAttr.PoolObjVal)), DXupperValue, freeXupperValue, freeSBupperValue, (regime == 0) ? null : DTKupper.getValue(), DXlowerValue, freeXlowerValue, freeSBlowerValue, (regime == 0) ? null : DTKlower.getValue(), isTableValue, isDDT2Value, DXlowExtValue, DSTKlowExtValue, DKnownDeclowExtValue, DXuppExtValue, DSTKuppExtValue, DKnownuppExtValue, DKnownEncuppExtValue, DXFixedlowExtValue, DWFixedlowExtValue, DXFilterlowExtValue, DWFilterlowExtValue, DXisFilterlowExtValue, DWisFilterlowExtValue, DXGuesslowExtValue, DWGuesslowExtValue);
    } catch (GRBException e) {
      System.out.println("Error code: " + e.getErrorCode() + ". " + e.getMessage());
      e.printStackTrace();
      System.exit(1);
      return null; // Can't access
    }
  }


  private void articleTrails() throws GRBException {
    // -------- Article trails --------
    int maxlanes = 3;
    // SKINNY-64-256 v2
    if (blocksize==4 && regime == 4 && nbRounds == 26) {
      maxlanes=3;
      for (int round = 0; round < 13; round++)
        for (int i = 0; i < 4; i++)
          for (int j = 0; j < 4; j++) {
            if (round == 0 && i == 0 && j == 1 ||
                round == 0 && i == 0 && j == 3 ||
                round == 8 && i == 0 && j == 2 ||
                round == 8 && i == 1 && j == 2 ||
                round == 8 && i == 3 && j == 2 ||
                round == 9 && i == 0 && j == 1 ||
                round == 9 && i == 2 && j == 3 ||
                round == 10 && i == 1 && j == 1 ||
                round == 11 && i == 2 && j == 1 ||
                round == 12 && i == 0 && j == 3 ||
		round == 12 && i == 3 && j == 3  )
              model.addConstr(DXupper[round][i][j], GRB.EQUAL, 1.0, "debug");
            else
              model.addConstr(DXupper[round][i][j], GRB.EQUAL, 0.0, "debug");
          }
      for (int round = 17; round < 26; round++)
        for (int i = 0; i < 4; i++)
          for (int j = 0; j < 4; j++) {
            if (round == 17 && i == 0 && j == 1 )
              model.addConstr(DXlower[round][i][j], GRB.EQUAL, 1.0, "debug");
            else
              model.addConstr(DXlower[round][i][j], GRB.EQUAL, 0.0, "debug");
          }
    }
    // SKINNY-64-256 v1
    else if (blocksize==4 && regime == 4 && nbRounds == 29) {
      maxlanes=1;
      model.addConstr(DXupper[0][0][2], GRB.EQUAL, 1.0, "debug");
      model.addConstr(DXlower[18][0][3], GRB.EQUAL, 1.0, "debug");
      for (int round = 1; round < 11; round++)
        for (int i = 0; i < 4; i++)
          for (int j = 0; j < 4; j++) {
              model.addConstr(DXupper[round][i][j], GRB.EQUAL, 0.0, "debug");
          }
      for (int round = 19; round < 29; round++)
        for (int i = 0; i < 4; i++)
          for (int j = 0; j < 4; j++) {
              model.addConstr(DXlower[round][i][j], GRB.EQUAL, 0.0, "debug");
          }
    }



    GRBLinExpr sumLanesupp = new GRBLinExpr();
    GRBLinExpr sumLaneslow = new GRBLinExpr();
    for (int i = 0; i < 16; i++) {
        sumLanesupp.addTerm(1.0, DTKupper.lanes[i]);
        sumLaneslow.addTerm(1.0, DTKlower.lanes[i]);
    }
    model.addConstr(sumLanesupp, GRB.LESS_EQUAL, maxlanes, "");
    model.addConstr(sumLaneslow, GRB.LESS_EQUAL, maxlanes, "");
    
  }


}
