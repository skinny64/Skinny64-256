package boomerangsearch.step2;

public class Step2SolutionTweakey {
  public int[][][] dTK1;
  public int[][][] dTK2;
  public int[][][] dTK3;
  public int[][][] dTK4;
  public int[][][] dTK;

  public Step2SolutionTweakey() {} // Needed for Jackson

  public Step2SolutionTweakey(int[][][] dTK1, int[][][] dTK2, int[][][] dTK3, int[][][] dTK4, int[][][] dTK) {
    this.dTK1 = dTK1;
    this.dTK2 = dTK2;
    this.dTK3 = dTK3;
    this.dTK4 = dTK4;
    this.dTK  = dTK;
  }
}
