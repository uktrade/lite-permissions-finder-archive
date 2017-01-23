package components.services.search.search;

import com.fasterxml.jackson.annotation.JsonProperty;
import components.services.search.ControlCode;

import java.util.List;
import java.util.Objects;

public class Result extends ControlCode {

  public final List<String> additionalControlCodeMatches;

  public Result(@JsonProperty("controlCode") String controlCode,
                @JsonProperty("displayText") String displayText,
                @JsonProperty("additionalMatches") List<String> additionalControlCodeMatches) {
    super(controlCode, displayText);
    this.additionalControlCodeMatches = additionalControlCodeMatches;
  }

  public boolean additionalMatchesPresent() {
    return !this.additionalControlCodeMatches.isEmpty();
  }
}
