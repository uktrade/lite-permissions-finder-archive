package components.services.controlcode.catchall;

import com.fasterxml.jackson.databind.JsonNode;
import play.libs.Json;
import uk.gov.bis.lite.controlcode.api.view.ControlCodeFullView;

import java.util.Arrays;
import java.util.List;

public class CatchallControlsServiceResult {
  public final List<ControlCodeFullView> controlCodes;

  public CatchallControlsServiceResult(JsonNode responseJson) {
    this.controlCodes = Arrays.asList(Json.fromJson(responseJson, ControlCodeFullView[].class));
  }
}
