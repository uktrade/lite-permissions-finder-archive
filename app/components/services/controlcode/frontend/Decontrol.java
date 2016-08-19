package components.services.controlcode.frontend;

import com.fasterxml.jackson.annotation.JsonProperty;

public class Decontrol {

  public String originControlCode;

  public String text;

  public Decontrol(@JsonProperty("originControlCode") String originControlCode,
                   @JsonProperty("text") String text) {
    this.originControlCode = originControlCode;
    this.text = text;
  }
}
