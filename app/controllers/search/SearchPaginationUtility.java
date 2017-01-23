package controllers.search;

import com.fasterxml.jackson.databind.node.ObjectNode;
import components.services.search.ControlCode;
import play.Logger;
import play.libs.Json;

import java.util.List;
import java.util.stream.Collectors;

public class SearchPaginationUtility {
  public static ObjectNode buildErrorJsonAndLog(String message){
    Logger.error("Error handling Ajax request: {}", message);
    ObjectNode json = Json.newObject();
    json.put("status", "error");
    json.put("message", message);
    return json;
  }

  public static <T extends ControlCode> List<ObjectNode> buildControlCodeJsonList(List<T> codes) {
    return codes
        .stream()
        .map(code -> {
          ObjectNode codeJson = Json.newObject();
          codeJson.put("controlCode", code.controlCode);
          codeJson.put("displayText", code.displayText);
          return codeJson;
        }).collect(Collectors.toList());
  }
}
