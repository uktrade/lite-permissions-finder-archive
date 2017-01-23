package components.services.search.search;

import com.fasterxml.jackson.databind.JsonNode;
import play.libs.Json;
import uk.gov.bis.lite.searchmanagement.api.view.SearchResultView;
import uk.gov.bis.lite.searchmanagement.api.view.SearchResultsView;

import java.util.List;

public class SearchServiceResult {

  public final List<SearchResultView> results;

  public SearchServiceResult(JsonNode responseJson) {
    this.results = Json.fromJson(responseJson, SearchResultsView.class).getResults();
  }

}