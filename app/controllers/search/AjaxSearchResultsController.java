package controllers.search;

import static java.util.concurrent.CompletableFuture.completedFuture;
import static play.mvc.Results.ok;

import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;

import com.google.inject.Inject;
import components.common.transaction.TransactionManager;
import components.persistence.PermissionsFinderDao;
import components.services.search.SearchServiceClient;
import models.GoodsType;
import play.Logger;
import play.libs.Json;
import play.libs.concurrent.HttpExecutionContext;
import play.mvc.Result;

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
   * <code>{"status": "ok", "results": [{ "code": "ML1a", "highlightedText": "Some text"}], "moreResults": true}</code>
   * <br/><br/>
   * Example ERROR response:
   * <code>{"status": "error", "message": "Some error message"}</code>
   *
   * @param goodsType the type of goods to search over, should be the {@link GoodsType#value()} of a {@link models.GoodsType} entry
   * @param fromIndex the low endpoint (inclusive) of the results list
   * @param toIndex the high endpoint (exclusive) of the results list
   * @param transactionId the transaction ID
   * @return a Result with JSON content of the additional results to show
   */
  public CompletionStage<Result> getResults(String goodsType, int fromIndex, int toIndex, String transactionId) {

    if (transactionId == null || transactionId.isEmpty()) {
      return completedFuture(ok(buildErrorJsonAndLog(
          String.format("TransactionId cannot be null or empty %s", transactionId))));
    }
    else {
      transactionManager.setTransaction(transactionId);
    }

    Optional<GoodsType> goodsTypeOptional = GoodsType.getMatched(goodsType);

    if (goodsTypeOptional.isPresent()) {
      if (goodsTypeOptional.get() == GoodsType.PHYSICAL) {
        Optional<SearchController.ControlCodeSearchForm> optionalForm = permissionsFinderDao.getPhysicalGoodsSearchForm();
        if (optionalForm.isPresent()) {
          return searchServiceClient.get(SearchController.getSearchTerms(optionalForm.get()))
              .thenApplyAsync(searchResult -> ok(buildResponseJson(searchResult.results, fromIndex, toIndex))
                  , httpExecutionContext.current());
        }
        else {
          return completedFuture(ok(buildErrorJsonAndLog("Unable to lookup search terms")));
        }
      }
      else {
        return completedFuture(ok(buildErrorJsonAndLog(String.format("Unhandled goodsType %s", goodsType))));
      }
    }
    else {
      return completedFuture(ok(buildErrorJsonAndLog(String.format("Unknown value for goodsType %s", goodsType))));
    }

  }

  private ObjectNode buildResponseJson(List<components.services.search.Result> results, int fromIndex, int toIndex) {
    ObjectNode json = Json.newObject();
    json.put("status", "ok");
    ArrayNode resultsNode = json.putArray("results");

    if (results != null && !results.isEmpty()) {
      int newFromIndex = Math.max(Math.min(fromIndex, results.size()), 0);
      int newToIndex = Math.min(Math.max(toIndex, 0), results.size());

      List<ObjectNode> list = results.subList(newFromIndex, newToIndex)
          .stream()
          .map(result -> {
            ObjectNode resultJson = Json.newObject();
            resultJson.put("code", result.code);
            resultJson.put("highlightedText", result.highlightedText);
            return resultJson;
          }).collect(Collectors.toList());

      resultsNode.addAll(list);

      json.put("moreResults", !(list.isEmpty() || newToIndex == results.size()));
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
