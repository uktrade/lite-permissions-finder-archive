package components.services.ogels.applicable;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

public class Result {

  public final String name;

  public final String id;

  public final List<String> usageSummary;

  public Result(@JsonProperty("name") String name,
                @JsonProperty("id") String id,
                @JsonProperty("usageSummary") List<String> usageSummary) {
    this.name = name;
    this.id = id;
    this.usageSummary = usageSummary;
  }

}
