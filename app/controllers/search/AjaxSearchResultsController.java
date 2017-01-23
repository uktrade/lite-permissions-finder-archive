package controllers.search;

import static java.util.concurrent.CompletableFuture.completedFuture;
import static play.mvc.Results.ok;

import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.google.inject.Inject;
import components.common.transaction.TransactionManager;
import components.persistence.PermissionsFinderDao;
import components.services.search.search.SearchServiceClient;
import models.controlcode.ControlCodeSubJourney;
import play.libs.Json;
import play.libs.concurrent.HttpExecutionContext;
import play.mvc.Result;
import uk.gov.bis.lite.searchmanagement.api.view.SearchResultView;

import java.util.List;
import java.util.Optional;
import java.util.concurrent.CompletionStage;
import java.util.stream.Collectors;

public class AjaxSearchResultsController {

  private final TransactionManager transactionManager;
  private final PermissionsFinderDao permissionsFinderDao;
  private final HttpExecutionContext httpExecutionContext;

  @Inject
  public AjaxSearchResultsController(TransactionManager transactionManager, PermissionsFinderDao permissionsFinderDao, HttpExecutionContext httpExecutionContext, SearchServiceClient searchServiceClient) {
    this.transactionManager = transactionManager;
    this.permissionsFinderDao = permissionsFinderDao;
    this.httpExecutionContext = httpExecutionContext;
    this.searchServiceClient = searchServiceClient;
  }

  private final SearchServiceClient searchServiceClient;

  /**
   * AJAX request handler, creating a JSON object of the additional results to show.
   * <br/><br/>
   * Example OK response:
   * <code>{"status": "ok", "results": [{ "controlCode": "ML1a", "displayText": "Some text"}], "moreResults": true}</code>
   * <br/><br/>
   * Example ERROR response:
   * <code>{"status": "error", "message": "Some error message"}</code>
   *
   * @param fromIndex the low endpoint (inclusive) of the results list
   * @param toIndex the high endpoint (exclusive) of the results list
   * @param transactionId the transaction ID
   * @return a Result with JSON content of the additional results to show
   */
  public CompletionStage<Result> getResults(String controlCodeSubJourney, int fromIndex, int toIndex, String transactionId) {

    if (transactionId == null || transactionId.isEmpty()) {
      return completedFuture(ok(SearchPaginationUtility.buildErrorJsonAndLog(
          String.format("TransactionId cannot be null or empty %s", transactionId))));
    }
    else {
      transactionManager.setTransaction(transactionId);
    }

    Optional<ControlCodeSubJourney> controlCodeSubJourneyOptional = ControlCodeSubJourney.getMatched(controlCodeSubJourney);

    if (controlCodeSubJourneyOptional.isPresent()) {
      Optional<SearchController.SearchForm> optionalForm =
          permissionsFinderDao.getPhysicalGoodsSearchForm(controlCodeSubJourneyOptional.get());
      if (optionalForm.isPresent()) {
        return searchServiceClient.get(SearchController.getSearchTerms(optionalForm.get()))
            .thenApplyAsync(searchResult -> ok(buildResponseJson(searchResult.results, fromIndex, toIndex))
                , httpExecutionContext.current());
      }
      else {
        return completedFuture(ok(SearchPaginationUtility.buildErrorJsonAndLog("Unable to lookup search terms")));
      }
    }
    else {
      return completedFuture(ok(SearchPaginationUtility.buildErrorJsonAndLog(String.format("Unknown value for controlCodeSubJourney %s", controlCodeSubJourney))));
    }

  }

  private ObjectNode buildResponseJson(List<SearchResultView> results, int fromIndex, int toIndex) {
    ObjectNode json = Json.newObject();
    json.put("status", "ok");
    ArrayNode resultsNode = json.putArray("results");

    if (results != null && !results.isEmpty()) {
      int newFromIndex = Math.max(Math.min(fromIndex, results.size()), 0);
      int newToIndex = Math.min(Math.max(toIndex, 0), results.size());

      List<ObjectNode> list = buildControlCodeJsonList(results.subList(newFromIndex, newToIndex));

      resultsNode.addAll(list);

      json.put("moreResults", !(list.isEmpty() || newToIndex == results.size()));
    }

    return json;
  }

  private List<ObjectNode> buildControlCodeJsonList(List<SearchResultView> relatedCodes) {
    return relatedCodes
        .stream()
        .map(code -> {
          ObjectNode codeJson = Json.newObject();
          codeJson.put("controlCode", code.getControlCode());
          codeJson.put("displayText", code.getDisplayText());
          return codeJson;
        }).collect(Collectors.toList());
  }
}
