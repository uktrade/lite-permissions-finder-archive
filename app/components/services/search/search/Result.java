package components.services.search.search;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

public class Result {

  public final String controlCode;
  public final String displayText;
  public final List<String> additionalControlCodeMatches;

  public Result(@JsonProperty("controlCode") String controlCode,
                @JsonProperty("displayText") String displayText,
                @JsonProperty("additionalMatches") List<String> additionalControlCodeMatches) {
    this.controlCode = controlCode != null ? controlCode : "";
    // Default to controlCode if null
    this.displayText = displayText != null ? displayText : this.controlCode;
    this.additionalControlCodeMatches = additionalControlCodeMatches;
  }

  public boolean additionalMatchesPresent() {
    return !this.additionalControlCodeMatches.isEmpty();
  }
}
