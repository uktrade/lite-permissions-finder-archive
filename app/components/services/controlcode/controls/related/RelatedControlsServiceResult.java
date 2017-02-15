package components.services.controlcode.controls.related;

import com.fasterxml.jackson.databind.JsonNode;
import play.libs.Json;
import uk.gov.bis.lite.controlcode.api.view.ControlCodeFullView;

import java.util.Arrays;
import java.util.List;

public class RelatedControlsServiceResult {
  public final List<ControlCodeFullView> controlCodes;

  public RelatedControlsServiceResult(JsonNode responseJson) {
    this.controlCodes = Arrays.asList(Json.fromJson(responseJson, ControlCodeFullView[].class));
  }
}
