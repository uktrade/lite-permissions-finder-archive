package components.services.search.relatedcodes;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.Arrays;
import java.util.List;

public class RelatedCodesServiceResult {

  public final List<RelatedCode> relatedCodes;

  public RelatedCodesServiceResult(@JsonProperty("results") RelatedCode[] relatedCodes) {
    this.relatedCodes = Arrays.asList(relatedCodes);
  }
}
