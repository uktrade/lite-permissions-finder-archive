package components.services.controlcode.related;

import com.fasterxml.jackson.databind.JsonNode;
import components.services.controlcode.ControlCodeFullViewResult;
import play.libs.Json;
import uk.gov.bis.lite.controlcode.api.view.ControlCodeFullView;

import java.util.Arrays;

public class RelatedControlsServiceResult extends ControlCodeFullViewResult {
  public RelatedControlsServiceResult(JsonNode responseJson) {
    super(Arrays.asList(Json.fromJson(responseJson, ControlCodeFullView[].class)));
  }
}
