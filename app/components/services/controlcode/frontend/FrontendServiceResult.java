package components.services.controlcode.frontend;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

public class FrontendServiceResult {

  @JsonProperty("controlCodeData")
  public ControlCodeData controlCodeData;

  @JsonProperty("lineage")
  public List<Ancestor> ancestors;

  public FrontendServiceResult(){}


}