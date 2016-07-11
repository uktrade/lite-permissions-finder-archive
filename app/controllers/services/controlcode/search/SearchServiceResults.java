package controllers.services.controlcode.search;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

public class SearchServiceResults {

  @JsonProperty("results")
  public List<SearchServiceResult> results;

  public List<SearchServiceResult> getResults() {
    return results;
  }
}