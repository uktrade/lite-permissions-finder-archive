package controllers.search;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

public class ControlCodeSearchResults {

  @JsonProperty("results")
  public List<ControlCodeSearchResult> results;

  public List<ControlCodeSearchResult> getResults() {
    return results;
  }
}