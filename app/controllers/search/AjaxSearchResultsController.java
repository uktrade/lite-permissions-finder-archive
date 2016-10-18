package controllers.search;

import static java.util.concurrent.CompletableFuture.completedFuture;
import static play.mvc.Results.ok;

import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;

import com.google.inject.Inject;
import components.common.transaction.TransactionManager;
import components.persistence.PermissionsFinderDao;
import components.services.search.SearchServiceClient;
import play.Logger;
import play.libs.Json;
import play.libs.concurrent.HttpExecutionContext;
import play.mvc.Result;

import java.util.List;
import java.util.Optional;
import java.util.concurrent.CompletionStage;
import java.util.stream.Collectors;

public class AjaxSearchResultsController {

  public static String PHYSICAL_GOODS = "PHYSICAL_GOODS";
  public static String SOFTWARE = "SOFTWARE";
  public static String TECHNOLOGY = "TECHNOLOGY";

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

  public CompletionStage<Result> getResults(String searchType, int currentCount, String transactionId) {

    if (transactionId == null || transactionId.isEmpty()) {
      return completedFuture(ok(buildErrorJsonAndLog(String.format("TransactionId cannot be null or empty %s", transactionId))));
    }
    else {
      transactionManager.createTransaction(transactionId);
    }

    if (currentCount < 0 || currentCount > 100) {
      return completedFuture(ok(buildErrorJsonAndLog(String.format("currentCount must be between 0 and 100 (inclusive) %s", currentCount))));
    }

    if (PHYSICAL_GOODS.equals(searchType)) {
      Optional<SearchController.ControlCodeSearchForm> optionalForm = permissionsFinderDao.getPhysicalGoodsSearchForm();
      if (optionalForm.isPresent()) {
        return searchServiceClient.get(SearchController.getSearchTerms(optionalForm.get()))
            .thenApplyAsync(searchResult -> ok(buildResponseJson(searchResult.results, currentCount)), httpExecutionContext.current());
      }
      else {
        return completedFuture(ok(buildErrorJsonAndLog("Unable to lookup search terms")));
      }
    }
    else if (SOFTWARE.equals(searchType)) {
      // Return an error for now
      return completedFuture(ok(buildErrorJsonAndLog(String.format("Invalid searchType %s", searchType))));
    }
    else if (TECHNOLOGY.equals(searchType)) {
      // Return an error for now
      return completedFuture(ok(buildErrorJsonAndLog(String.format("Invalid searchType %s", searchType))));
    }
    else {
      return completedFuture(ok(buildErrorJsonAndLog("Unhandled request state")));
    }

  }

  private ObjectNode buildResponseJson(List<components.services.search.Result> results, int currentCount) {
    ObjectNode json = Json.newObject();
    json.put("status", "ok");
    ArrayNode resultsNode = json.putArray("results");

    if (results != null && !results.isEmpty()) {
      int fromIdx = Math.max(Math.min(currentCount, results.size()), 0);
      int toIdx = Math.min(Math.max(currentCount + 5, 0), results.size());
      List<ObjectNode> list = results.subList(fromIdx, toIdx)
          .stream()
          .map(r -> {
            ObjectNode resultJson = Json.newObject();
            resultJson.put("code", r.code);
            resultJson.put("highlightedText", r.highlightedText);
            return resultJson;
          }).collect(Collectors.toList());
      resultsNode.addAll(list);
    }

    return json;
  }

  private ObjectNode buildErrorJsonAndLog(String message){
    Logger.error("Error handling Ajax request: {}", message);
    ObjectNode json = Json.newObject();
    json.put("status", "error");
    json.put("message", message);
    return json;
  }
}
