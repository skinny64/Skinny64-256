package boomerangsearch.step1;

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.core.type.TypeReference;

import java.util.List;
import java.io.File;
import java.io.IOException;

public class Step1Solution {
  public int nbRounds;
  public int nExtbRounds;
  public int nExtfRounds;
  public int regime;
  public int objective;
  public int[][][] DXupper;
  public int[][][] freeXupper;
  public int[][][] freeSBupper;
  public Step1SolutionTweakey DTKupper;
  public int[][][] DXlower;
  public int[][][] freeXlower;
  public int[][][] freeSBlower;
  public Step1SolutionTweakey DTKlower;
  public int[][][] isTable;
  public int[][][] isDDT2;
  /*Ef*/
  public int[][][] DXlowExt;
  public int[][][] DSTKlowExt;
  public int[][][] DKnownDeclowExt;

  /*Eb*/
  public int[][][] DXuppExt;
  public int[][][] DSTKuppExt;
  public int[][][] DKnownuppExt;
  public int[][][] DKnownEncuppExt;

  /*fastfilter*/
  public int[][][] DXFixedlowExt;
  public int[][][] DWFixedlowExt;
  public int[][][] DXFilterlowExt;
  public int[][][] DWFilterlowExt;
  public int[][][] DXisFilterlowExt;
  public int[][][] DWisFilterlowExt;
  public int[][][] DXGuesslowExt;
  public int[][][] DWGuesslowExt;


  public Step1Solution() {}

  public Step1Solution(int nbRounds, int nExtbRounds, int nExtfRounds, int regime, int objective, int[][][] DXupper, int[][][] freeXupper, int[][][] freeSBupper, Step1SolutionTweakey DTKupper, int[][][] DXlower, int[][][] freeXlower, int[][][] freeSBlower, Step1SolutionTweakey DTKlower, int[][][] isTable, int[][][] isDDT2, int[][][] DXlowExt, int[][][] DSTKlowExt, int[][][] DKnownDeclowExt, int[][][] DXuppExt, int[][][] DSTKuppExt, int[][][] DKnownuppExt, int[][][] DKnownEncuppExt, int[][][] DXFixedlowExt, int[][][] DWFixedlowExt, int[][][] DXFilterlowExt, int[][][] DWFilterlowExt, int[][][] DXisFilterlowExt, int[][][] DWisFilterlowExt, int[][][] DXGuesslowExt, int[][][] DWGuesslowExt) {
    this.nbRounds = nbRounds;
    this.nExtbRounds = nExtbRounds;
    this.nExtfRounds = nExtfRounds;
    this.regime = regime;
    this.objective = objective;
    this.DXupper = DXupper;
    this.freeXupper = freeXupper;
    this.freeSBupper = freeSBupper;
    this.DTKupper = DTKupper;
    this.DXlower = DXlower;
    this.freeXlower = freeXlower;
    this.freeSBlower = freeSBlower;
    this.DTKlower = DTKlower;
    this.isTable = isTable;
    this.isDDT2 = isDDT2;
    this.DXlowExt = DXlowExt;
    this.DSTKlowExt = DSTKlowExt;
    this.DKnownDeclowExt = DKnownDeclowExt;
    this.DXuppExt = DXuppExt;
    this.DSTKuppExt = DSTKuppExt;
    this.DKnownuppExt = DKnownuppExt;
    this.DKnownEncuppExt = DKnownEncuppExt;
    this.DXFixedlowExt = DXFixedlowExt;
    this.DWFixedlowExt=DWFixedlowExt;
    this.DXFilterlowExt=DXFilterlowExt;
    this.DWFilterlowExt=DWFilterlowExt;
    this.DXisFilterlowExt=DXisFilterlowExt;
    this.DWisFilterlowExt=DWisFilterlowExt;
    this.DXGuesslowExt=DXGuesslowExt;
    this.DWGuesslowExt=DWGuesslowExt;

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

  public static void toFile(String fileName, List<Step1Solution> solutions) {
    ObjectMapper mapper = new ObjectMapper();
    try {
      mapper.writeValue(new File(fileName), solutions);
    }
    catch (JsonParseException e) { e.printStackTrace(); System.exit(1); }
    catch (JsonMappingException e) { e.printStackTrace(); System.exit(1); }
    catch (IOException e) { e.printStackTrace(); System.exit(1); }
  }

  public static void toFile(File file, List<Step1Solution> solutions) {
    ObjectMapper mapper = new ObjectMapper();
    try {
      mapper.writeValue(file, solutions);
    }
    catch (JsonParseException e) { e.printStackTrace(); System.exit(1); }
    catch (JsonMappingException e) { e.printStackTrace(); System.exit(1); }
    catch (IOException e) { e.printStackTrace(); System.exit(1); }
  }

  public static List<Step1Solution> fromFile(String fileName) {
    return fromFile(new File(fileName));
  }

  public static List<Step1Solution> fromFile(File file) {
    try {
      return new ObjectMapper().readValue(file, new TypeReference<List<Step1Solution>>(){});
    }
    catch (JsonParseException e) { e.printStackTrace(); System.exit(1); }
    catch (JsonMappingException e) { e.printStackTrace(); System.exit(1); }
    catch (IOException e) { e.printStackTrace(); System.exit(1); }
    return null; // Can't reach
  }
}
