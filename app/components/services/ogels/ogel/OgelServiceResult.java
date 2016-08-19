package components.services.ogels.ogel;

import com.fasterxml.jackson.annotation.JsonProperty;

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

}
