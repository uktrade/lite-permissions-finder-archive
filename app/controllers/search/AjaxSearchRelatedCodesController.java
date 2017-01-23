package controllers.search;

import static java.util.concurrent.CompletableFuture.completedFuture;
import static play.mvc.Results.ok;

import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.google.inject.Inject;
import components.common.transaction.TransactionManager;
import components.persistence.PermissionsFinderDao;
import components.services.search.relatedcodes.RelatedCodesServiceClient;
import models.controlcode.ControlCodeSubJourney;
import org.apache.commons.lang3.StringUtils;
import play.libs.Json;
import play.libs.concurrent.HttpExecutionContext;
import play.mvc.Result;
import uk.gov.bis.lite.searchmanagement.api.view.RelatedCodeView;

import java.util.List;
import java.util.Optional;
import java.util.concurrent.CompletionStage;
import java.util.stream.Collectors;

public class AjaxSearchRelatedCodesController {

  private final TransactionManager transactionManager;
  private final PermissionsFinderDao permissionsFinderDao;
  private final HttpExecutionContext httpExecutionContext;
  private final RelatedCodesServiceClient relatedCodesServiceClient;

  @Inject
  public AjaxSearchRelatedCodesController(TransactionManager transactionManager, PermissionsFinderDao permissionsFinderDao, HttpExecutionContext httpExecutionContext, RelatedCodesServiceClient relatedCodesServiceClient) {
    this.transactionManager = transactionManager;
    this.permissionsFinderDao = permissionsFinderDao;
    this.httpExecutionContext = httpExecutionContext;
    this.relatedCodesServiceClient = relatedCodesServiceClient;
  }

  /**
   * AJAX request handler, creating a JSON object of the additional results to show.
   * <br/><br/>
   * Example OK response:
   * <code>{"status": "ok", "relatedCodes": [{ "controlCode": "ML1a", "displayText": "Some text"}], "moreRelatedCodes": true}</code>
   * <br/><br/>
   * Example ERROR response:
   * <code>{"status": "error", "message": "Some error message"}</code>
   *
   * @param fromIndex the low endpoint (inclusive) of the related codes list
   * @param toIndex the high endpoint (exclusive) of the related codes list
   * @param transactionId the transaction ID
   * @return a Result with JSON content of the additional results to show
   */
  public CompletionStage<Result> getRelatedCodes(String controlCodeSubJourney, int fromIndex, int toIndex, String transactionId) {

    if (transactionId == null || transactionId.isEmpty()) {
      return completedFuture(ok(SearchPaginationUtility.buildErrorJsonAndLog(
          String.format("TransactionId cannot be null or empty %s", transactionId))));
    }
    else {
      transactionManager.setTransaction(transactionId);
    }

    Optional<ControlCodeSubJourney> controlCodeSubJourneyOptional = ControlCodeSubJourney.getMatched(controlCodeSubJourney);

    if (controlCodeSubJourneyOptional.isPresent()) {
      String resultsControlCode = permissionsFinderDao.getSearchResultsLastChosenControlCode(controlCodeSubJourneyOptional.get());
      if (StringUtils.isNotEmpty(resultsControlCode)) {
        return relatedCodesServiceClient.get(resultsControlCode)
            .thenApplyAsync(result -> ok(buildResponseJson(result.relatedCodes, fromIndex, toIndex))
                , httpExecutionContext.current());
      }
      else {
        return completedFuture(ok(SearchPaginationUtility.buildErrorJsonAndLog("Unable to lookup related codes")));
      }
    }
    else {
      return completedFuture(ok(SearchPaginationUtility.buildErrorJsonAndLog(String.format("Unknown value for controlCodeSubJourney %s", controlCodeSubJourney))));
    }
  }

  private ObjectNode buildResponseJson(List<RelatedCodeView> relatedCodes, int fromIndex, int toIndex) {
    ObjectNode json = Json.newObject();
    json.put("status", "ok");
    ArrayNode relatedCodesNode = json.putArray("relatedCodes");

    if (relatedCodes != null && !relatedCodes.isEmpty()) {
      int newFromIndex = Math.max(Math.min(fromIndex, relatedCodes.size()), 0);
      int newToIndex = Math.min(Math.max(toIndex, 0), relatedCodes.size());

      List<ObjectNode> list = buildControlCodeJsonList(relatedCodes.subList(newFromIndex, newToIndex));

      relatedCodesNode.addAll(list);

      json.put("moreRelatedCodes", !(list.isEmpty() || newToIndex == relatedCodes.size()));
    }

    return json;
  }

  private List<ObjectNode> buildControlCodeJsonList(List<RelatedCodeView> relatedCodes) {
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
