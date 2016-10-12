package components.services.ogels.virtualeu;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.JsonNode;
import play.libs.Json;

public class VirtualEUOgelResult {

  public final boolean virtualEu;

  public final String ogelId;

  public VirtualEUOgelResult(@JsonProperty("virtualEu") String virtualEu, @JsonProperty("ogelId") String ogelId) {
    this.virtualEu = Boolean.parseBoolean(virtualEu);
    this.ogelId = ogelId;
  }

  public static VirtualEUOgelResult build(JsonNode responseJson) {
    return Json.fromJson(responseJson, VirtualEUOgelResult.class);
  }

}
