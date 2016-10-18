package components.services.search;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.Arrays;
import java.util.List;

public class SearchServiceResult {

  public final List<Result> results;

  public SearchServiceResult(@JsonProperty("results") Result[] results) {
    this.results = Arrays.asList(results);
  }

}