package controllers.services.controlcode.frontend;

import com.fasterxml.jackson.annotation.JsonProperty;

public class Ancestor {

  @JsonProperty("controlCode")
  public String controlCode;

  @JsonProperty("friendlyDescription")
  public String friendlyDescription;

  public Ancestor(){}

}
