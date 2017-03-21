package components.services.controlcode.nonexempt;

import com.fasterxml.jackson.databind.JsonNode;
import components.services.controlcode.ControlCodeFullViewResult;
import play.libs.Json;
import uk.gov.bis.lite.controlcode.api.view.ControlCodeFullView;

import java.util.Arrays;

public class NonExemptControlsServiceResult extends ControlCodeFullViewResult{
  public NonExemptControlsServiceResult(JsonNode responseJson) {
    super(Arrays.asList(Json.fromJson(responseJson, ControlCodeFullView[].class)));
  }
}
