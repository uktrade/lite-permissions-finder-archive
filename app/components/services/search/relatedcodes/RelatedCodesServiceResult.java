package components.services.search.relatedcodes;

import com.fasterxml.jackson.databind.JsonNode;
import play.libs.Json;
import uk.gov.bis.lite.searchmanagement.api.view.RelatedCodeView;
import uk.gov.bis.lite.searchmanagement.api.view.RelatedCodesView;

import java.util.List;

public class RelatedCodesServiceResult {

  public final String groupTitle;
  public final List<RelatedCodeView> relatedCodes;

  public RelatedCodesServiceResult(JsonNode responseJson) {
    RelatedCodesView relatedCodesView = Json.fromJson(responseJson, RelatedCodesView.class);
    this.groupTitle = relatedCodesView.getGroupTitle();
    this.relatedCodes = relatedCodesView.getResults();
  }

  public boolean shouldShowRelatedCodes(String controlCode) {
    return relatedCodes.size() > 0 && !(relatedCodes.size() == 1 && controlCode.equals(relatedCodes.get(0).getControlCode()));
  }
}
