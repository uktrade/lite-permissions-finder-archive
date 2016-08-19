package components.services.ogels.ogel;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

public class Summary {

  public final List<String> canList;

  public final List<String> cantList;

  public final List<String> mustList;

  public final List<String> howToUseList;

  public Summary(@JsonProperty("canList") List<String> canList,
                 @JsonProperty("cantList") List<String> cantList,
                 @JsonProperty("mustList") List<String> mustList,
                 @JsonProperty("howToUseList") List<String> howToUseList) {
    this.canList = canList;
    this.cantList = cantList;
    this.mustList = mustList;
    this.howToUseList = howToUseList;
  }

}
