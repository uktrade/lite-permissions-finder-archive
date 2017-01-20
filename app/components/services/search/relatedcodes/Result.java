package components.services.search.relatedcodes;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.Objects;

public class Result {

  public final String controlCode;
  public final String displayText;

  public Result(@JsonProperty("controlCode") String controlCode, @JsonProperty("displayText") String displayText) {
    // Default to an empty string if null
    this.controlCode = Objects.toString(controlCode, "");
    // Default to controlCode if null
    this.displayText = Objects.toString(displayText, this.controlCode);
  }
}
