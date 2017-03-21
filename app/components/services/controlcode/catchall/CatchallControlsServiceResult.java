package components.services.controlcode.catchall;

import com.fasterxml.jackson.databind.JsonNode;
import components.services.controlcode.ControlCodeFullViewResult;
import play.libs.Json;
import uk.gov.bis.lite.controlcode.api.view.ControlCodeFullView;

import java.util.Arrays;

public class CatchallControlsServiceResult extends ControlCodeFullViewResult {
  public CatchallControlsServiceResult(JsonNode responseJson) {
    super(Arrays.asList(Json.fromJson(responseJson, ControlCodeFullView[].class)));
  }
}
