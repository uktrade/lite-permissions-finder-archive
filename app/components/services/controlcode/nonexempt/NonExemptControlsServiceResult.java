package components.services.controlcode.nonexempt;

import com.fasterxml.jackson.databind.JsonNode;
import play.libs.Json;
import uk.gov.bis.lite.controlcode.api.view.ControlCodeFullView;

import java.util.Arrays;
import java.util.List;

public class NonExemptControlsServiceResult {
  public final List<ControlCodeFullView> controlCodes;

  public NonExemptControlsServiceResult(JsonNode responseJson) {
    this.controlCodes = Arrays.asList(Json.fromJson(responseJson, ControlCodeFullView[].class));
  }

}
