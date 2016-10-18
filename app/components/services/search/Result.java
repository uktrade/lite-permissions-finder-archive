package components.services.search;

import com.fasterxml.jackson.annotation.JsonProperty;

public class Result {

  public final String highlightedText;

  public final String code;

  public Result(@JsonProperty("highlightedText") String highlightedText,
                             @JsonProperty("code") String code) {
    this.highlightedText = highlightedText;
    this.code = code;
  }

}
