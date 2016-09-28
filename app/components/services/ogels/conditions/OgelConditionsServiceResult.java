package components.services.ogels.conditions;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.Optional;

public class OgelConditionsServiceResult {

  public final String conditionDescription;
  public final String ogelID;
  public final Optional<ConditionDescriptionControlCodes> conditionDescriptionControlCodes;
  public final String itemsAllowed ;
  public final String controlCode;

  public OgelConditionsServiceResult(
      @JsonProperty("conditionDescription") String conditionDescription,
      @JsonProperty("ogelID") String ogelID,
      @JsonProperty("conditionDescriptionControlCodes") ConditionDescriptionControlCodes conditionDescriptionControlCodes,
      @JsonProperty("itemsAllowed") String itemsAllowed,
      @JsonProperty("controlCode") String controlCode) {
    this.conditionDescription = conditionDescription;
    this.ogelID = ogelID;
    this.conditionDescriptionControlCodes = conditionDescriptionControlCodes != null
        ? Optional.of(conditionDescriptionControlCodes)
        : Optional.empty();
    this.itemsAllowed = itemsAllowed;
    this.controlCode = controlCode;
  }

}
