package controllers.services.controlcode.frontend;

import com.fasterxml.jackson.annotation.JsonProperty;

public class SpecificationText {

  @JsonProperty("text")
  public String text;

  @JsonProperty("linkedControlCode")
  public String linkedControlCode;

  public SpecificationText(){}

}
