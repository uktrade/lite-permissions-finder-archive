package components.services.controlcode.search;

import com.fasterxml.jackson.annotation.JsonProperty;

public class SearchServiceResult {

  public final String highlightedText;

  public final String code;

  public SearchServiceResult(@JsonProperty("highlightedText") String highlightedText,
                             @JsonProperty("code") String code) {
    this.highlightedText = highlightedText;
    this.code = code;
  }

}