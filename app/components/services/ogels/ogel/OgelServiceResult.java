package components.services.ogels.ogel;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.JsonNode;
import play.libs.Json;

public class OgelServiceResult {

  public final String id;

  public final String name;

  public final String description;

  public final String link;

  public final Summary summary;

  public OgelServiceResult(
      @JsonProperty("id") String id,
      @JsonProperty("name") String name,
      @JsonProperty("description") String description,
      @JsonProperty("link") String link,
      @JsonProperty("summary") Summary summary) {
    this.id = id;
    this.name = name;
    this.description = description;
    this.link = link;
    this.summary = summary;
  }

  public static OgelServiceResult build(JsonNode responseJson) {
    return Json.fromJson(responseJson, OgelServiceResult.class);
  }

}
