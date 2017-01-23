package components.services.search;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.Objects;

public class ControlCode {
  public final String controlCode;
  public final String displayText;

  public ControlCode(@JsonProperty("controlCode") String controlCode, @JsonProperty("displayText") String displayText) {
    // Default to an empty string if null
    this.controlCode = Objects.toString(controlCode, "");
    // Default to controlCode if null
    this.displayText = Objects.toString(displayText, this.controlCode);
  }
}
