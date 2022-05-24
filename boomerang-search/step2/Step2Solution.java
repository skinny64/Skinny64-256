package boomerangsearch.step2;

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.core.type.TypeReference;

import java.util.List;
import java.io.File;
import java.io.IOException;

public class Step2Solution {
  public int nbRounds;
  public int nExtbRounds;
  public int nExtfRounds;
  public double probaExponent;
  public double probaClusters;
  public int[][][] dXupper;
  public int[][][] dSBupper;
  public int[][][] dYupper;
  public Step2SolutionTweakey dTKupper;
  public int[][][] dXlower;
  public int[][][] dSBlower;
  public int[][][] dYlower;
  public Step2SolutionTweakey dTKlower;
  public int[][][] proba;

  public Step2Solution() {} // Needed for Jackson

  public Step2Solution(int nbRounds, int nExtbRounds, int nExtfRounds, double probaExponent, int[][][] dXupper, int[][][] dSBupper, int[][][] dYupper, Step2SolutionTweakey dTKupper, int[][][] dXlower, int[][][] dSBlower, int[][][] dYlower, Step2SolutionTweakey dTKlower, int[][][] proba) {
    this.nbRounds = nbRounds;
    this.nExtbRounds = nExtbRounds;
    this.nExtfRounds = nExtfRounds;
    this.probaExponent = probaExponent;
    this.dXupper  = dXupper;
    this.dSBupper = dSBupper;
    this.dYupper  = dYupper;
    this.dTKupper = dTKupper;
    this.dXlower  = dXlower;
    this.dSBlower = dSBlower;
    this.dYlower  = dYlower;
    this.dTKlower = dTKlower;
    this.proba = proba;
  }

  public void toFile(String fileName) {
    ObjectMapper mapper = new ObjectMapper();
    try {
      mapper.writeValue(new File(fileName), this);
    }
    catch (JsonParseException e) { e.printStackTrace(); System.exit(1); }
    catch (JsonMappingException e) { e.printStackTrace(); System.exit(1); }
    catch (IOException e) { e.printStackTrace(); System.exit(1); }
  }

  public static void toFile(String fileName, Step2Solution[] solutions) {
    ObjectMapper mapper = new ObjectMapper();
    try {
      mapper.writeValue(new File(fileName), solutions);
    }
    catch (JsonParseException e) { e.printStackTrace(); System.exit(1); }
    catch (JsonMappingException e) { e.printStackTrace(); System.exit(1); }
    catch (IOException e) { e.printStackTrace(); System.exit(1); }
  }

  public static void toFile(File file, Step2Solution[] solutions) {
    ObjectMapper mapper = new ObjectMapper();
    try {
      mapper.writeValue(file, solutions);
    }
    catch (JsonParseException e) { e.printStackTrace(); System.exit(1); }
    catch (JsonMappingException e) { e.printStackTrace(); System.exit(1); }
    catch (IOException e) { e.printStackTrace(); System.exit(1); }
  }

  public static List<Step2Solution> fromFile(String fileName) {
    return fromFile(new File(fileName));
  }

  public static List<Step2Solution> fromFile(File file) {
    ObjectMapper mapper = new ObjectMapper();
    try {
      return mapper.readValue(file, new TypeReference<List<Step2Solution>>(){});
    }
    catch (JsonParseException e) { e.printStackTrace(); System.exit(1); }
    catch (JsonMappingException e) { e.printStackTrace(); System.exit(1); }
    catch (IOException e) { e.printStackTrace(); System.exit(1); }
    return null; // Can't reach
  }
}
