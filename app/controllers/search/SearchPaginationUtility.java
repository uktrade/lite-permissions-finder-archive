package controllers.search;

import com.fasterxml.jackson.databind.node.ObjectNode;
import play.Logger;
import play.libs.Json;

public class SearchPaginationUtility {
  public static ObjectNode buildErrorJsonAndLog(String message){
    Logger.error("Error handling Ajax request: {}", message);
    ObjectNode json = Json.newObject();
    json.put("status", "error");
    json.put("message", message);
    return json;
  }
}
