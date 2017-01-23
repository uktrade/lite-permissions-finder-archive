package components.services.search.relatedcodes;

import com.fasterxml.jackson.databind.JsonNode;
import play.libs.Json;
import uk.gov.bis.lite.searchmanagement.api.view.RelatedCodeView;
import uk.gov.bis.lite.searchmanagement.api.view.RelatedCodesView;

import java.util.List;

public class RelatedCodesServiceResult {

  public final List<RelatedCodeView> relatedCodes;

  public RelatedCodesServiceResult(JsonNode responseJson) {
    this.relatedCodes = Json.fromJson(responseJson, RelatedCodesView.class).getResults();
  }
}
