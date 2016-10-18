package components.services.registration;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.JsonNode;
import play.libs.Json;

public class OgelRegistrationServiceResult {

  public final String status;

  public final String redirectUrl;

  public OgelRegistrationServiceResult(@JsonProperty("status") String status,
                                       @JsonProperty("redirectUrl") String redirectUrl) {
    this.status = status;
    this.redirectUrl = redirectUrl;
  }

  public static OgelRegistrationServiceResult buildFromJson(JsonNode jsonNode){
    return Json.fromJson(jsonNode, OgelRegistrationServiceResult.class);
  }

}
