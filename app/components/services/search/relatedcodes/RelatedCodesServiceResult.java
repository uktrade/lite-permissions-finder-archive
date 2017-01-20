package components.services.search.relatedcodes;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.Arrays;
import java.util.List;

public class RelatedCodesServiceResult {

  public final List<Result> results;

  public RelatedCodesServiceResult(@JsonProperty("results") Result[] results) {
    this.results = Arrays.asList(results);
  }
}
