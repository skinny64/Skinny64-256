package boomerangsearch.step1;

import gurobi.*;

public class Step1Factory {
  private GRBModel model;
  private int regime;

  /**
   * @param model the main gurobi model
   * @param regime whether we are on SK (TK0), TK1, TK2, TK3 or TK4
   */
  public Step1Factory(final GRBModel model, final int regime) {
    this.model = model;
    this.regime = regime;
  }

  /** Adds the constraints of the linear part of the trails : ART, SR and MC */
  public void addLinear(GRBVar[][][] DX, GRBVar[][][] DTK) throws GRBException {
    for (int round = 0; round < DX.length-1; round++)
      for (int j = 0; j < 4; j++) {
        addXor(DX[round+1][1][j], DX[round][0][j], DTK[round][0][j]);
        addXor(DX[round+1][2][j], DX[round][1][(j+3)%4], DTK[round][1][(j+3)%4], DX[round][2][(j+2)%4]);
        addXor(DX[round+1][3][j], DX[round+1][1][j], DX[round][2][(j+2)%4]);
        addXor(DX[round+1][0][j], DX[round+1][3][j], DX[round][3][(j+1)%4]);
      }
  }
  
  /**lowExt linear part: ART, SR and MC */
  public void addlowExtLinear(GRBVar[][][] DXlowExt, GRBVar[][][] DSTKlowExt) throws GRBException {
    for (int r = 0; r < DXlowExt.length-1; r++)
      for (int j = 0; j < 4; j++) {
	addOr(DXlowExt[r+1][0][j], DXlowExt[r][0][j], DSTKlowExt[r][0][j], DXlowExt[r][2][(j+2)%4], DXlowExt[r][3][(j+1)%4]);
	addOr(DXlowExt[r+1][1][j], DXlowExt[r][0][j], DSTKlowExt[r][0][j]);
        addOr(DXlowExt[r+1][2][j], DXlowExt[r][1][(j+3)%4], DSTKlowExt[r][1][(j+3)%4], DXlowExt[r][2][(j+2)%4]);
        addOr(DXlowExt[r+1][3][j], DXlowExt[r][0][j], DSTKlowExt[r][0][j], DXlowExt[r][2][(j+2)%4]);
      }
  }

  public void addlowExtDec(GRBVar[][][] DXlowExt,GRBVar[][][] DknownDeclowExt) throws GRBException {
    for (int i = 0; i < 4; i++)
        for (int j = 0; j < 4; j++) {
        	model.addConstr(DknownDeclowExt[0][i][j], GRB.EQUAL, DXlowExt[0][i][j], "");
        }
    
    for (int r = 0; r < DXlowExt.length-1; r++) {
      for (int j = 0; j < 4; j++) {
        addOr(DknownDeclowExt[r+1][0][j], DknownDeclowExt[r][3][(j+1)%4], DXlowExt[r+1][0][j]);
        addOr(DknownDeclowExt[r+1][1][j], DknownDeclowExt[r][0][j], DknownDeclowExt[r][1][(j+3)%4], DknownDeclowExt[r][2][(j+2)%4], DXlowExt[r+1][1][j]);
        addOr(DknownDeclowExt[r+1][2][j], DknownDeclowExt[r][1][(j+3)%4], DXlowExt[r+1][2][j]);
        addOr(DknownDeclowExt[r+1][3][j], DknownDeclowExt[r][1][(j+3)%4], DknownDeclowExt[r][2][(j+2)%4], DknownDeclowExt[r][3][(j+1)%4], DXlowExt[r+1][3][j]);
     }
    }       
  }
 
  /**UppExt linear part: ART, SR and MC */
  public void adduppExtLinear(GRBVar[][][] DXuppExt, GRBVar[][][] DSTKuppExt) throws GRBException {
    for (int r = 0; r < DXuppExt.length-1; r++)
      for (int j = 0; j < 4; j++) {
	addOr(DXuppExt[r][0][j], DSTKuppExt[r][0][j], DXuppExt[r+1][1][j]);
	addOr(DXuppExt[r][1][j], DSTKuppExt[r][1][j], DXuppExt[r+1][1][(j+1)%4], DXuppExt[r+1][2][(j+1)%4], DXuppExt[r+1][3][(j+1)%4]);
        addOr(DXuppExt[r][2][j], DXuppExt[r+1][1][(j+2)%4], DXuppExt[r+1][3][(j+2)%4]);
        addOr(DXuppExt[r][3][j], DXuppExt[r+1][0][(j+3)%4], DXuppExt[r+1][3][(j+3)%4]);
      }
  }

  public void adduppExtEnc(GRBVar[][][] DKnownuppExt, GRBVar[][][] DKnownEncuppExt, GRBVar[][][] DXuppExt) throws GRBException {
    for (int r = 0; r < DKnownuppExt.length-1; r++)
      for (int j = 0; j < 4; j++) {
	addOr(DKnownEncuppExt[r][0][j], DKnownuppExt[r+1][0][j], DKnownuppExt[r+1][1][j], DKnownuppExt[r+1][3][j]);
	addOr(DKnownEncuppExt[r][1][j], DKnownuppExt[r+1][2][(j+1)%4]);
        addOr(DKnownEncuppExt[r][2][j], DKnownuppExt[r+1][0][(j+2)%4], DKnownuppExt[r+1][2][(j+2)%4], DKnownuppExt[r+1][3][(j+2)%4]);
        addOr(DKnownEncuppExt[r][3][j], DKnownuppExt[r+1][0][(j+3)%4]);
      }
      for (int r = 0; r < DKnownuppExt.length-1; r++)
          for (int i = 0; i < 4; i++) 
              for (int j = 0; j < 4; j++) {
		      addOr(DKnownuppExt[r][i][j], DKnownEncuppExt[r][i][j], DXuppExt[r][i][j]);
      }
  }

  /**fastfilter*/
  public void addDFixedlowExtLinear(GRBVar[][][] DXFixedlowExt,GRBVar[][][] DWFixedlowExt,GRBVar[][][] DXlowExt) throws GRBException {
    for (int i = 0; i < 4; i++)
        for (int j = 0; j < 4; j++) {
        	model.addConstr(DXFixedlowExt[0][i][j], GRB.EQUAL, 1, "");
        }
    double[] t1 = {1.0, 0.0, -1.0};
    double[] t2 = {-1.0, 1.0, 1.0};
    double[] t3 = {0.0, -1.0, -1.0};
    int[] shift = {0, 3, 2, 1};
    for (int r = 0; r < DWFixedlowExt.length; r++) {
      for (int j = 0; j < 4; j++) {
        for (int i = 0; i < 4; i++) {
          model.addConstr(linExprOf(t1, DXFixedlowExt[r][i][(j+shift[i])%4], DXlowExt[r][i][(j+shift[i])%4], DWFixedlowExt[r][i][j]), GRB.GREATER_EQUAL, 0.0, "");
          model.addConstr(linExprOf(t2, DXFixedlowExt[r][i][(j+shift[i])%4], DXlowExt[r][i][(j+shift[i])%4], DWFixedlowExt[r][i][j]), GRB.GREATER_EQUAL, 0.0, "");
          model.addConstr(linExprOf(t3, DXFixedlowExt[r][i][(j+shift[i])%4], DXlowExt[r][i][(j+shift[i])%4], DWFixedlowExt[r][i][j]), GRB.GREATER_EQUAL, -1.0, "");
        }
      }
    }
    for (int r = 0; r < DWFixedlowExt.length-1; r++) {
      for (int j = 0; j < 4; j++) {
        addAnd(DXFixedlowExt[r+1][0][j], DWFixedlowExt[r][0][j], DWFixedlowExt[r][2][j], DWFixedlowExt[r][3][j]);
        model.addConstr(DXFixedlowExt[r+1][1][j], GRB.EQUAL, DWFixedlowExt[r][0][j], "");
        addAnd(DXFixedlowExt[r+1][2][j], DWFixedlowExt[r][1][j], DWFixedlowExt[r][2][j]);
        addAnd(DXFixedlowExt[r+1][3][j], DWFixedlowExt[r][0][j], DWFixedlowExt[r][2][j]);
     }
    }
  }

  public void addWfilterlowExt(GRBVar[][][] DWFixedlowExt,GRBVar[][][] DWFilterlowExt) throws GRBException {
    double[][] t = {{0.0, 0.0, -1.0, 0.0, 0.0, 0.0, 1.0, -1.0},
     {0.0, 0.0, -1.0, 0.0, 0.0, -1.0, 0.0, 0.0},
     {-1.0, 0.0, 0.0, 0.0, 0.0, 0.0, -1.0, 0.0},
     {1.0, 0.0, -1.0, 0.0, 0.0, 0.0, 1.0, 0.0},
     {0.0, 0.0, 1.0, -1.0, 0.0, 0.0, -1.0, 1.0},
     {0.0, 0.0, 0.0, 1.0, 0.0, 0.0, 0.0, -1.0},
     {0.0, 1.0, 0.0, 0.0, 0.0, -1.0, 0.0, 0.0},
     {0.0, -1.0, 1.0, 0.0, 0.0, 1.0, 0.0, 0.0}};
    double[] con = {-1.0, -1.0, -1.0, 0.0, 0.0, 0.0, 0.0, 0.0};
    for (int r = 0; r < DWFixedlowExt.length; r++) {
      for (int j = 0; j < 4; j++) {
        if ( r==  DWFixedlowExt.length-1) {        
            for (int i = 0; i < 4; i++) {
              model.addConstr(DWFilterlowExt[r][i][j], GRB.EQUAL, DWFixedlowExt[r][i][j], "");
            }     
        }
       else {
         model.addConstr(DWFilterlowExt[r][0][j], GRB.EQUAL, 0.0, "");
         for (int k = 0; k < 8; k++) {
            model.addConstr(linExprOf(t[k], DWFixedlowExt[r][0][j], DWFixedlowExt[r][1][j], DWFixedlowExt[r][2][j], DWFixedlowExt[r][3][j], DWFilterlowExt[r][0][j], DWFilterlowExt[r][1][j], DWFilterlowExt[r][2][j], DWFilterlowExt[r][3][j]), GRB.GREATER_EQUAL, con[k], "");
          }
        }
      }
    }
    
  }

  public void addXfilterlowExt(GRBVar[][][] DXFixedlowExt,GRBVar[][][] DXlowExt,GRBVar[][][] DXFilterlowExt) throws GRBException {
    for (int r = 0; r < DXFixedlowExt.length; r++) {
      for (int j = 0; j < 4; j++) {
        for (int i = 0; i < 4; i++) {
          addAnd(DXFilterlowExt[r][i][j], DXFixedlowExt[r][i][j], DXlowExt[r][i][j]);
        }
      }
    }
  }

  public void addXguesslowExt(GRBVar[][][] DXGuesslowExt,GRBVar[][][] DWGuesslowExt,GRBVar[][][] DXisFilterlowExt,GRBVar[][][] DWisFilterlowExt) throws GRBException {
    for (int i = 0; i < 4; i++)
        for (int j = 0; j < 4; j++) {
        	model.addConstr(DXGuesslowExt[0][i][j], GRB.EQUAL, DXisFilterlowExt[0][i][j], "");
        }
    
    int[] shift = {0, 3, 2, 1};
    for (int r = 0; r < DWisFilterlowExt.length; r++) {
      for (int j = 0; j < 4; j++) {
        for (int i = 0; i < 4; i++) {
          addOr(DWGuesslowExt[r][i][j], DWisFilterlowExt[r][i][j], DXGuesslowExt[r][i][(j+shift[i])%4]);
        }
      }
    }

    for (int r = 0; r < DWisFilterlowExt.length-1; r++) {
      for (int j = 0; j < 4; j++) {
        addOr(DXGuesslowExt[r+1][0][j], DWGuesslowExt[r][3][j], DXisFilterlowExt[r+1][0][j]);
	addOr(DXGuesslowExt[r+1][1][j], DWGuesslowExt[r][0][j], DWGuesslowExt[r][1][j], DWGuesslowExt[r][2][j], DXisFilterlowExt[r+1][1][j]);
        addOr(DXGuesslowExt[r+1][2][j], DWGuesslowExt[r][1][j], DXisFilterlowExt[r+1][2][j]);
        addOr(DXGuesslowExt[r+1][3][j], DWGuesslowExt[r][1][j], DWGuesslowExt[r][2][j], DWGuesslowExt[r][3][j], DXisFilterlowExt[r+1][3][j]);
     }
    }       
  }
  
   public void addUpperBound(GRBVar[][][] isTable,GRBVar[][][] isDDT2, int blocksize, int regime, int nbRounds) throws GRBException {
    int maxprob = 60;
    if (blocksize==4 && regime == 2 && nbRounds == 18) 
        maxprob=42;
    else if (blocksize==8 && regime == 2 && nbRounds == 19) 
        maxprob=55;
    else if (blocksize==8 && regime == 2 && nbRounds == 18) 
        maxprob=50;
    else if (blocksize==4 && regime == 3 && nbRounds == 22) 
        maxprob=42;
    else if (blocksize==8 && regime == 3 && nbRounds == 23) 
        maxprob=55;
    else if (blocksize==4 && regime == 4) 
        maxprob=42;

    GRBLinExpr sumTables = new GRBLinExpr();
    for (int round = 0; round < isTable.length; round++)
      for (int i = 0; i < 4; i++)
        for (int j = 0; j < 4; j++) {
          sumTables.addTerm(1.0, isTable[round][i][j]);
          sumTables.addTerm(1.0, isDDT2[round][i][j]);
        }
    model.addConstr(sumTables, GRB.LESS_EQUAL, maxprob, "");
  }

  /** Adds the constraints of the linear part of the trails for SK : ART, SR and MC */
  public void addLinearSK(GRBVar[][][] DX) throws GRBException {
    for (int round = 0; round < DX.length-1; round++)
      for (int j = 0; j < 4; j++) {
        model.addConstr(DX[round+1][1][j], GRB.EQUAL, DX[round][0][j], "");
        addXor(DX[round+1][2][j], DX[round][1][(j+3)%4], DX[round][2][(j+2)%4]);
        addXor(DX[round+1][3][j], DX[round+1][1][j], DX[round][2][(j+2)%4]);
        addXor(DX[round+1][0][j], DX[round+1][3][j], DX[round][3][(j+1)%4]);
      }
  }

  /** Adds constraints on the free variables between upper and lower trail */
  public void freePropagation(GRBVar[][][] DXup, GRBVar[][][] freeSBup, GRBVar[][][] DXlo, GRBVar[][][] freeXlo) throws GRBException {
    double[] t1 = {1.0, -1.0, -1.0, 1.0};
    double[] t2 = {2.0, -2.0, -1.0, 1.0, -1.0, 1.0};
    double[] t3 = {3.0, -3.0, -1.0, 1.0, -1.0, 1.0, -1.0, 1.0};
    for (int r = 0; r < freeXlo.length-1; r++) {
      for (int j = 0; j < 4; j++) {
        model.addConstr(linExprOf(t3, freeSBup[r][0][j], DXup[r][0][j], freeSBup[r+1][0][j], freeXlo[r+1][0][j], freeSBup[r+1][1][j], freeXlo[r+1][1][j], freeSBup[r+1][3][j], freeXlo[r+1][3][j]), GRB.GREATER_EQUAL, -5.0, "");
        model.addConstr(linExprOf(t1, freeSBup[r][1][j], DXup[r][1][j], freeSBup[r+1][2][(j+1)%4], freeXlo[r+1][2][(j+1)%4]), GRB.GREATER_EQUAL, -1.0, "");
        model.addConstr(linExprOf(t3, freeSBup[r][2][j], DXup[r][2][j], freeSBup[r+1][2][(j+2)%4], freeXlo[r+1][2][(j+2)%4], freeSBup[r+1][2][(j+2)%4], freeXlo[r+1][2][(j+2)%4], freeSBup[r+1][3][(j+2)%4], freeXlo[r+1][3][(j+2)%4]), GRB.GREATER_EQUAL, -5.0, "");
        model.addConstr(linExprOf(t1, freeSBup[r][3][j], DXup[r][3][j], freeSBup[r+1][0][(j+3)%4], freeXlo[r+1][0][(j+3)%4]), GRB.GREATER_EQUAL, -1.0, "");

        model.addConstr(linExprOf(t1, freeXlo[r+1][0][j], DXlo[r+1][0][j], freeXlo[r][3][(j+1)%4], freeSBup[r][3][(j+1)%4]), GRB.GREATER_EQUAL, -1.0, "");
        model.addConstr(linExprOf(t3, freeXlo[r+1][1][j], DXlo[r+1][1][j], freeXlo[r][0][j], freeSBup[r][0][j], freeXlo[r][1][(j+3)%4], freeSBup[r][1][(j+3)%4], freeXlo[r][2][(j+2)%4], freeSBup[r][2][(j+2)%4]), GRB.GREATER_EQUAL, -5.0, "");
        model.addConstr(linExprOf(t1, freeXlo[r+1][2][j], DXlo[r+1][2][j], freeXlo[r][1][(j+3)%4], freeSBup[r][1][(j+3)%4]), GRB.GREATER_EQUAL, -1.0, "");
        model.addConstr(linExprOf(t3, freeXlo[r+1][3][j], DXlo[r+1][3][j], freeXlo[r][1][(j+3)%4], freeSBup[r][1][(j+3)%4], freeXlo[r][2][(j+2)%4], freeSBup[r][2][(j+2)%4], freeXlo[r][3][(j+1)%4], freeSBup[r][3][(j+1)%4]), GRB.GREATER_EQUAL, -5.0, "");
      }
    }
  }

  /** Adds constraints on the free variables in the upper trail */
  public void freePropagationUpper(GRBVar[][][] freeX, GRBVar[][][] freeSB, GRBVar[][][] DX) throws GRBException {
    for (int r = 0; r < freeX.length-1; r++)
      for (int j = 0; j < 4; j++) {
        addOr(freeX[r+1][0][j], freeSB[r][0][j], freeSB[r][2][(j+2)%4], freeSB[r][3][(j+1)%4]);
        model.addConstr(freeX[r+1][1][j], GRB.EQUAL, freeSB[r][0][j], "");
        addOr(freeX[r+1][2][j], freeSB[r][1][(j+3)%4], freeSB[r][2][(j+2)%4]);
        addOr(freeX[r+1][3][j], freeSB[r][0][j], freeSB[r][2][(j+2)%4]);
      }
  }

  /** Adds constraints on the free variables in the upper trail */
  public void freePropagationLower(GRBVar[][][] freeX, GRBVar[][][] freeSB, GRBVar[][][] DX) throws GRBException {
    for (int r = 0; r < freeX.length-1; r++)
      for (int j = 0; j < 4; j++) {
        model.addConstr(freeSB[r][0][j], GRB.EQUAL, freeX[r+1][1][j], "");
        addOr(freeSB[r][1][j], freeX[r+1][1][(j+1)%4],freeX[r+1][2][(j+1)%4],freeX[r+1][3][(j+1)%4]);
        addOr(freeSB[r][2][j], freeX[r+1][1][(j+2)%4], freeX[r+1][3][(j+2)%4]);
        addOr(freeSB[r][3][j], freeX[r+1][0][(j+3)%4], freeX[r+1][3][(j+3)%4]);
      }
  }

  public void objectiveConstraints(GRBVar[][][] DXupper, GRBVar[][][] freeXupper, GRBVar[][][] freeSBupper, GRBVar[][][] DXlower, GRBVar[][][] freeXlower, GRBVar[][][] freeSBlower, GRBVar[][][] isTable, GRBVar[][][] isDDT2) throws GRBException {
    for (int round = 0; round < freeXupper.length; round++)
      for (int i = 0; i < 4; i++)
        for (int j = 0; j < 4; j++) {
          oneObjectiveConstraint(DXupper[round][i][j], freeXupper[round][i][j], freeSBupper[round][i][j], DXlower[round][i][j], freeXlower[round][i][j], freeSBlower[round][i][j], isTable[round][i][j], isDDT2[round][i][j]);
        }
  }

  public void oneObjectiveConstraint(GRBVar DXupper, GRBVar freeXupper, GRBVar freeSBupper, GRBVar DXlower, GRBVar freeXlower, GRBVar freeSBlower, GRBVar isTable, GRBVar isDDT2) throws GRBException {
    model.addConstr(DXupper, GRB.GREATER_EQUAL, freeSBupper, ""); // if free then =1 -> cy' exy
    model.addConstr(DXlower, GRB.GREATER_EQUAL, freeXlower,  ""); // Symmetry        -> cz' ezt
    model.addConstr(freeSBupper, GRB.GREATER_EQUAL, freeXupper, ""); // free propagates -> cy cx'
    model.addConstr(freeXlower, GRB.GREATER_EQUAL, freeSBlower, ""); // Symmetry        -> cz ct'
    model.addConstr(isTable, GRB.GREATER_EQUAL, isDDT2, ""); // DDT2 -> isTable -> d1 d2'
    model.addConstr(2.0, GRB.GREATER_EQUAL, linExprOf(freeSBupper, freeXlower, isTable), ""); // BCT and others -> cy' cz' d1'
    model.addConstr(linExprOf(freeSBupper, isTable), GRB.GREATER_EQUAL, DXupper, ""); //          -> cy exy' d1
    model.addConstr(linExprOf(freeXlower,  isTable), GRB.GREATER_EQUAL, DXlower, ""); // Symmetry -> cz ezt' d1
    model.addConstr(DXupper, GRB.GREATER_EQUAL, linExprOf(-1.0, freeXlower,  isTable), ""); //          -> cz' exy d1'
    model.addConstr(DXlower, GRB.GREATER_EQUAL, linExprOf(-1.0, freeSBupper, isTable), ""); // Symmetry -> cy' ezt d1'
    model.addConstr(linExprOf(DXupper, DXlower), GRB.GREATER_EQUAL, isTable, ""); // -> exy ezt d1'
    model.addConstr(isDDT2, GRB.GREATER_EQUAL, linExprOf(-1.0, freeXupper,  DXlower), ""); //          -> cx' ezt' d2
    model.addConstr(isDDT2, GRB.GREATER_EQUAL, linExprOf(-1.0, freeSBlower, DXupper), ""); // Symmetry -> ct' exy' d2
    model.addConstr(linExprOf(freeXupper, freeSBlower), GRB.GREATER_EQUAL, isDDT2, ""); // cx ct d2'
  }

  public void objectiveConstraintsDouble(GRBVar[][][] DXupper, GRBVar[][][] freeXupper, GRBVar[][][] freeSBupper, GRBVar[][][] DXlower, GRBVar[][][] freeXlower, GRBVar[][][] freeSBlower, GRBVar[][][] isTable, GRBVar[][][] isDDT2) throws GRBException {
    for (int round = 0; round < freeXupper.length; round++)
      for (int i = 0; i < 4; i++)
        for (int j = 0; j < 4; j++) {
          oneObjectiveConstraintDouble(DXupper[round][i][j], freeXupper[round][i][j], freeSBupper[round][i][j], DXlower[round][i][j], freeXlower[round][i][j], freeSBlower[round][i][j], isTable[round][i][j], isDDT2[round][i][j]);
        }
  }

  public void oneObjectiveConstraintDouble(GRBVar DXupper, GRBVar freeXupper, GRBVar freeSBupper, GRBVar DXlower, GRBVar freeXlower, GRBVar freeSBlower, GRBVar isTable, GRBVar isDDT2) throws GRBException {
    boolean optimized = false;
    model.addConstr(DXupper, GRB.GREATER_EQUAL, freeSBupper, ""); // if free then =1  -> cy' exy
    model.addConstr(DXlower, GRB.GREATER_EQUAL, freeXlower,  ""); // Symmetry         -> cz' ezt
    if (!optimized) {
      model.addConstr(DXupper, GRB.GREATER_EQUAL, isDDT2, ""); // need value for ddt2 -> exy d2'
      model.addConstr(DXlower, GRB.GREATER_EQUAL, isDDT2, ""); // Symmetry            -> ezt d2'
    }
    else {
      model.addConstr(linExprOf(new double[]{1.0,1.0,-2.0}, DXupper, DXlower, isDDT2), GRB.GREATER_EQUAL, 0, "");
    }
    model.addConstr(freeSBupper, GRB.GREATER_EQUAL, freeXupper, ""); // free propagates  -> cy cx'
    model.addConstr(freeXlower, GRB.GREATER_EQUAL, freeSBlower, ""); // Symmetry         -> cz ct'
    model.addConstr(1.0, GRB.GREATER_EQUAL, linExprOf(isTable, isDDT2), ""); // not twice at the same time -> d1' d2'
    if (!optimized) {
      model.addConstr(2.0, GRB.GREATER_EQUAL, linExprOf(freeSBupper, freeXlower, isTable), ""); // BCT and others -> cy' cz' d1'
      model.addConstr(2.0, GRB.GREATER_EQUAL, linExprOf(freeSBupper, freeXlower, isDDT2),  ""); // Same for DDT2  -> cy' cz' d2'
    }
    else {
      model.addConstr(4, GRB.GREATER_EQUAL, linExprOf(new double[]{2.0,2.0,1.0,1.0}, freeSBupper, freeXlower, isTable, isDDT2), "");
    }
    model.addConstr(linExprOf(freeSBupper, freeSBlower, isTable), GRB.GREATER_EQUAL, DXupper, ""); //          -> cy ct exy' d1
    model.addConstr(linExprOf(freeXlower,  freeXupper,  isTable), GRB.GREATER_EQUAL, DXlower, ""); // Symmetry -> cx cz ezt' d1
    model.addConstr(DXupper, GRB.GREATER_EQUAL, linExprOf(-1.0, freeXlower,  isTable), ""); //          -> cz' exy d1'
    model.addConstr(DXlower, GRB.GREATER_EQUAL, linExprOf(-1.0, freeSBupper, isTable), ""); // Symmetry -> cy' ezt d1'
    model.addConstr(linExprOf(DXupper, DXlower), GRB.GREATER_EQUAL, isTable, ""); // need one non zero for table -> exy ezt d1'
    model.addConstr(isDDT2, GRB.GREATER_EQUAL, linExprOf(-1.0, freeXupper,  DXlower), ""); //          -> cx' ezt' d2
    model.addConstr(isDDT2, GRB.GREATER_EQUAL, linExprOf(-1.0, freeSBlower, DXupper), ""); // Symmetry -> ct' exy' d2
  }

  public void addXor(GRBVar ... vars) throws GRBException {
    for (int i = 0; i < vars.length; i++) {
      GRBLinExpr sumOthers = new GRBLinExpr();
      for (int j = 0; j < vars.length; j++)
        if (j != i)
          sumOthers.addTerm(1.0, vars[j]);
      model.addConstr(vars[i], GRB.LESS_EQUAL, sumOthers, "Xor");
    }
  }

  public void addAnd(GRBVar mainVar, GRBVar ... vars) throws GRBException {
    GRBLinExpr sumOthers = new GRBLinExpr();
    for (int i = 0; i < vars.length; i++) {
        sumOthers.addTerm(1.0, vars[i]);
        model.addConstr(mainVar, GRB.LESS_EQUAL, vars[i], "");
    }
    sumOthers.addTerm(-1.0, mainVar);
    model.addConstr(sumOthers, GRB.LESS_EQUAL, vars.length-1, "");
  }
  
  public void addOr(GRBVar mainVar, GRBVar ... vars) throws GRBException {
    if (true)
      model.addGenConstrOr(mainVar, vars, "");
    else {
      GRBLinExpr sumOthers = new GRBLinExpr();
      for (int i = 0; i < vars.length; i++) {
          sumOthers.addTerm(1.0, vars[i]);
          model.addConstr(vars[i], GRB.LESS_EQUAL, mainVar, "");
      }
      model.addConstr(mainVar, GRB.LESS_EQUAL, sumOthers, "");
    }
  }

  /** Ensures that there is no X round of DX and DTK to zero, where X depends on the regime */
  public void ensureNonZeroDifference(GRBVar[][][] DX, GRBVar[][][] DTK) throws GRBException {
    int nbNonZeroRounds = 0;
    switch (regime) {
    //v1 
    case 4: nbNonZeroRounds = 10;
    //v2 
    //case 4: nbNonZeroRounds = 8;
      break;
    case 3: nbNonZeroRounds = 6;
      break;
    case 2: nbNonZeroRounds = 4;
      break;
    case 1: nbNonZeroRounds = 2;
      break;
    case 0: nbNonZeroRounds = 0;
      break;
    }
    if (DX.length >= nbNonZeroRounds+1) {
      GRBLinExpr nonZeroSum = new GRBLinExpr();
      for (int round = 0; round < nbNonZeroRounds; round++)
        for (int i = 0; i < 4; i++)
          for (int j = 0; j < 4; j++) {
            nonZeroSum.addTerm(1.0, DX[round][i][j]);
            if (i < 2)
              nonZeroSum.addTerm(1.0, DTK[round][i][j]);
          }
      model.addConstr(nonZeroSum, GRB.GREATER_EQUAL, 1, "nonZeroDiff");
    }
  }

  /** Remove symmetries in SK */
  public void removeSymmetriesSK(GRBVar[][] DX0) throws GRBException {
    // First row
    GRBLinExpr previousLines = new GRBLinExpr();
    for (int i = 0; i < 4; i++) {
      GRBLinExpr firstZero = new GRBLinExpr();
      firstZero.addTerm(3.0, DX0[i][0]);
      firstZero.multAdd(3.0, previousLines);
      model.addConstr(firstZero, GRB.GREATER_EQUAL, linExprOf(DX0[i][1],DX0[i][2],DX0[i][3]), "");
      model.addConstr(previousLines, GRB.GREATER_EQUAL, linExprOf(new double[]{-1.0,-1.0,1.0}, DX0[i][1], DX0[i][2], DX0[i][3]), "");
      previousLines.add(linExprOf(DX0[i]));
    }
  }

  public GRBLinExpr linExprOf(double[] coeffs, GRBVar ... vars) throws GRBException {
    GRBLinExpr ofVars = new GRBLinExpr();
    ofVars.addTerms(coeffs, vars);
    return ofVars;
  }

  public GRBLinExpr linExprOf(double constant, GRBVar ... vars) {
    GRBLinExpr ofVars = linExprOf(vars);
    ofVars.addConstant(constant);
    return ofVars;
  }

  public GRBLinExpr linExprOf(GRBVar ... vars) {
    GRBLinExpr expr = new GRBLinExpr();
    for (GRBVar var : vars)
      expr.addTerm(1.0, var);
    return expr;
  }

  public GRBLinExpr sumState(GRBVar[][] state) throws GRBException {
    GRBLinExpr sum = new GRBLinExpr();
    for (int i = 0; i < 4; i++)
      for (int j = 0; j < 4; j++)
        sum.addTerm(1.0, state[i][j]);
    return sum;
  }

  public void addKnownDiffBounds(GRBVar[][][] DX) throws GRBException {
    int[] diffBounds = new int[0];
    switch (regime) {
    case 0:
      diffBounds = new int[]{0,1,2,5,8,12,16,26,36,41,46,51,55,58,61,66,75,82,88,92,96,102,108,114,116,124,132,138,136,148,158};
      break;
    case 1:
      diffBounds = new int[]{0,0,0,1,2,3,6,10,13,16,23,32,38,41,45,49,54,59,62,66,70,75,79,83,85,88,95,102,108,112,120};
      break;
    case 2:
      diffBounds = new int[]{0,0,0,0,0,1,2,3,6,9,12,16,21,25,31,35,40,43,46,52,57,59,64,67,72,75,82,85,88,92,96};
      break;
    case 3:
      diffBounds = new int[]{0,0,0,0,0,0,0,1,2,3,6,10,13,16,19,24,27,31,35,43,45,48,51,55,58,60,65,72,77,81,85};
      break;
    case 4:
      //v2
      //diffBounds = new int[]{0,0,0,0,0,0,0,0,0,1,2,3,6,9,12,16,19,21,24,30,35,39,41,43,46,50,54,58,62,66,72};
      //v1
      diffBounds = new int[]{0,0,0,0,0,0,0,0,0,0,0,1,2,3,6,9,13,16,19,21,24,29,32,35,39,43,46,49,53,55,58};
      break;
    }
    for (int start = 0; start < DX.length-1; start++)
      for (int end = start + 4; end < DX.length-1; end++) {
        GRBLinExpr sumRounds = new GRBLinExpr();
        for (int round = start; round <= end; round++)
          sumRounds.add(sumState(DX[round]));
        model.addConstr(sumRounds, GRB.GREATER_EQUAL, diffBounds[end-start+1], "");
      }
  }

}
