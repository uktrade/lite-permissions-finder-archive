package components.services.ogels.conditions;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.JsonNode;
import play.libs.Json;

import java.util.Optional;

public class OgelConditionsServiceResult {

  public final String conditionDescription;
  public final String ogelID;
  public final Optional<ConditionDescriptionControlCodes> conditionDescriptionControlCodes;
  public final String itemsAllowed ;
  public final String controlCode;
  public final boolean isEmpty;
  public final boolean isMissingControlCodes;

  public OgelConditionsServiceResult() {
    this.conditionDescription = null;
    this.ogelID = null;
    this.conditionDescriptionControlCodes = null;
    this.itemsAllowed = null;
    this.controlCode = null;
    this.isEmpty = true;
    this.isMissingControlCodes = true;
  }

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
    this.isEmpty = false;
    this.isMissingControlCodes = conditionDescriptionControlCodes != null
        && conditionDescriptionControlCodes.missingControlCodes != null
        && !conditionDescriptionControlCodes.missingControlCodes.isEmpty();
  }

  public static OgelConditionsServiceResult buildEmpty() {
    return new OgelConditionsServiceResult();
  }

  public static OgelConditionsServiceResult buildFromJson(JsonNode reponseJson) {
    return Json.fromJson(reponseJson, OgelConditionsServiceResult.class);
  }


}
