package components.services.ogels.conditions;

import com.fasterxml.jackson.databind.JsonNode;
import play.libs.Json;
import uk.gov.bis.lite.ogel.api.view.ControlCodeConditionFullView;
import uk.gov.bis.lite.ogel.api.view.ControlCodeConditionFullView.ConditionDescriptionControlCodes;

import java.util.Optional;

public class OgelConditionsServiceResult {

  public final String conditionDescription;
  public final String ogelID;
  public final Optional<ConditionDescriptionControlCodes> conditionDescriptionControlCodes;
  public final boolean itemsAllowed;
  public final String controlCode;
  public final boolean isEmpty;
  public final boolean isMissingControlCodes;

  private OgelConditionsServiceResult() {
    this.conditionDescription = null;
    this.ogelID = null;
    this.conditionDescriptionControlCodes = Optional.empty();
    this.itemsAllowed = false;
    this.controlCode = null;
    this.isEmpty = true;
    this.isMissingControlCodes = true;
  }

  private OgelConditionsServiceResult(String conditionDescription, String ogelID,
                                      ConditionDescriptionControlCodes conditionDescriptionControlCodes,
                                     boolean itemsAllowed, String controlCode) {
    this.conditionDescription = conditionDescription;
    this.ogelID = ogelID;
    this.conditionDescriptionControlCodes = conditionDescriptionControlCodes != null
        ? Optional.of(conditionDescriptionControlCodes)
        : Optional.empty();
    this.itemsAllowed = itemsAllowed;
    this.controlCode = controlCode;
    this.isEmpty = false;
    this.isMissingControlCodes = conditionDescriptionControlCodes != null
        && conditionDescriptionControlCodes.getMissingControlCodes() != null
        && !conditionDescriptionControlCodes.getControlCodes().isEmpty();
  }

  public static OgelConditionsServiceResult buildEmpty() {
    return new OgelConditionsServiceResult();
  }

  public static OgelConditionsServiceResult buildFrom(JsonNode jsonNode) {
    ControlCodeConditionFullView controlCodeCondition = Json.fromJson(jsonNode, ControlCodeConditionFullView.class);
    return new OgelConditionsServiceResult(controlCodeCondition.getConditionDescription(),
      controlCodeCondition.getOgelId(), controlCodeCondition.getConditionDescriptionControlCodes(),
      controlCodeCondition.isItemsAllowed(), controlCodeCondition.getControlCode());
  }


}
