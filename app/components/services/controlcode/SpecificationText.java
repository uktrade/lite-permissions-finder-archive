package components.services.controlcode;

import com.fasterxml.jackson.annotation.JsonProperty;

public class SpecificationText {

  public final String text;

  public final String linkedControlCode;

  public SpecificationText(@JsonProperty("text") String text,
                           @JsonProperty("linkedControlCode") String linkedControlCode) {
    this.text = text;
    this.linkedControlCode = linkedControlCode;
  }

}
