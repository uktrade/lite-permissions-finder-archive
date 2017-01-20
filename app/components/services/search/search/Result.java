package components.services.search.search;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;
import java.util.Objects;

public class Result {

  public final String controlCode;
  public final String displayText;
  public final List<String> additionalControlCodeMatches;

  public Result(@JsonProperty("controlCode") String controlCode,
                @JsonProperty("displayText") String displayText,
                @JsonProperty("additionalMatches") List<String> additionalControlCodeMatches) {
    // Default to an empty string if null
    this.controlCode = Objects.toString(controlCode, "");
    // Default to controlCode if null
    this.displayText = Objects.toString(displayText, this.controlCode);
    this.additionalControlCodeMatches = additionalControlCodeMatches;
  }

  public boolean additionalMatchesPresent() {
    return !this.additionalControlCodeMatches.isEmpty();
  }
}
