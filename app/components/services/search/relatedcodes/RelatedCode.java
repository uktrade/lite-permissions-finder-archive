package components.services.search.relatedcodes;

import com.fasterxml.jackson.annotation.JsonProperty;
import components.services.search.ControlCode;

public class RelatedCode extends ControlCode{

  public RelatedCode(@JsonProperty("controlCode") String controlCode, @JsonProperty("displayText") String displayText) {
    super(controlCode, displayText);
  }

}
