package components.services.controlcode.controls.related;

import com.fasterxml.jackson.databind.JsonNode;
import components.services.controlcode.controls.ControlCode;
import play.libs.Json;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public class RelatedControlsServiceResult {
  public final List<ControlCode> controlCodes;

  public RelatedControlsServiceResult(JsonNode responseJson) {
    this.controlCodes = Arrays.asList(Json.fromJson(responseJson, ControlCode[].class)).stream()
        .sorted((c1, c2) -> Integer.compare(c1.displayOrder, c2.displayOrder))
        .collect(Collectors.toList());
  }
}
