package components.services.controlcode.category;

import com.fasterxml.jackson.databind.JsonNode;
import components.services.controlcode.ControlCodeFullViewResult;
import play.libs.Json;
import uk.gov.bis.lite.controlcode.api.view.ControlCodeFullView;

import java.util.Arrays;

public class CategoryControlsServiceResult extends ControlCodeFullViewResult {
  public CategoryControlsServiceResult(JsonNode responseJson) {
    super(Arrays.asList(Json.fromJson(responseJson, ControlCodeFullView[].class)));
  }
}
