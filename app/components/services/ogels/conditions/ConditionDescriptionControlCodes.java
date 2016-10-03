package components.services.ogels.conditions;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

public class ConditionDescriptionControlCodes {

  public final List<String> missingControlCodes;

  public final List<ControlCode> controlCodes;

  public ConditionDescriptionControlCodes(@JsonProperty("missingControlCodes") List<String> missingControlCodes,
                                          @JsonProperty("controlCodes") List<ControlCode> controlCodes) {
    this.missingControlCodes = missingControlCodes;
    this.controlCodes = controlCodes;
  }
}
