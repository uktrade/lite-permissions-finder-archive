package controllers.services.controlcode.frontend;

import com.fasterxml.jackson.annotation.JsonProperty;

public class Decontrol {

  @JsonProperty("originControlCode")
  public String originControlCode;

  @JsonProperty("text")
  public String text;

  public Decontrol(){}

}
