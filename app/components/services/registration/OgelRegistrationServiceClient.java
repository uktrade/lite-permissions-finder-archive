package components.services.registration;

import static play.mvc.Results.redirect;

import com.google.inject.Inject;
import com.google.inject.name.Named;
import components.common.logging.CorrelationId;
import components.common.logging.ServiceClientLogger;
import components.persistence.PermissionsFinderDao;
import exceptions.ServiceException;
import models.summary.Summary;
import models.summary.SummaryService;
import org.apache.commons.lang3.StringUtils;
import play.Logger;
import play.libs.Json;
import play.libs.concurrent.HttpExecutionContext;
import play.libs.ws.WSClient;
import play.libs.ws.WSRequest;
import play.mvc.Result;

import java.util.concurrent.CompletionStage;
import java.util.function.Function;

public class OgelRegistrationServiceClient {

  public static final String STATUS_CODE_OK = "ok";

  private final WSClient wsClient;
  private final int webServiceTimeout;
  private final String webServiceSharedSecret;
  private final String webServiceUrl;
  private final PermissionsFinderDao permissionsFinderDao;
  private final HttpExecutionContext httpExecutionContext;
  private final SummaryService summaryService;

  @Inject
  public OgelRegistrationServiceClient(WSClient wsClient,
                                       @Named("ogelRegistrationServiceAddress") String webServiceAddress,
                                       @Named("ogelRegistrationServiceTimeout") int webServiceTimeout,
                                       @Named("ogelRegistrationServiceSharedSecret") String webServiceSharedSecret,
                                       PermissionsFinderDao permissionsFinderDao,
                                       HttpExecutionContext httpExecutionContext,
                                       SummaryService summaryService) {
    this.wsClient = wsClient;
    this.webServiceTimeout = webServiceTimeout;
    this.webServiceSharedSecret = webServiceSharedSecret;
    this.webServiceUrl = webServiceAddress + "/update-transaction";
    this.permissionsFinderDao = permissionsFinderDao;
    this.httpExecutionContext = httpExecutionContext;
    this.summaryService = summaryService;
  }

  public CompletionStage<Result> updateTransactionAndRedirect(String transactionId) {
    WSRequest wsRequest = wsClient.url(webServiceUrl)
        .withRequestFilter(CorrelationId.requestFilter)
        .withRequestFilter(ServiceClientLogger.requestFilter("OGEL Registration", "POST", httpExecutionContext))
        .setRequestTimeout(webServiceTimeout)
        .setQueryParameter("securityToken", webServiceSharedSecret);

    CompletionStage<Summary> summaryStage = summaryService.composeSummary();

    CompletionStage<OgelRegistrationServiceRequest> requestStage =
        summaryStage.thenApplyAsync(summary -> new OgelRegistrationServiceRequest(transactionId, summary),
            httpExecutionContext.current());

    return requestStage.thenApplyAsync(request -> {
      Logger.debug(Json.toJson(request).toString());
      return wsRequest.post(Json.toJson(request));
    }
    , httpExecutionContext.current())
        .thenCompose(Function.identity())
        .handleAsync((response, error) -> {
          if (error != null) {
            throw new ServiceException("OGEL Registration service request failed", error);
          }
          else if (response.getStatus() != 200) {
            throw new ServiceException(String.format("Unexpected HTTP status code from OGEL Registration service /update-transaction: %s",
                response.getStatus()));
          }
          else {
            Logger.debug(response.asJson().toString());
            OgelRegistrationServiceResult result = OgelRegistrationServiceResult.buildFromJson(response.asJson());
            if (!StringUtils.isNotBlank(result.redirectUrl)) {
              throw new ServiceException("Unexpected redirect URL supplied from OGEL Registration service /update-transaction:");
            }
            else if (!StringUtils.equals(result.status, STATUS_CODE_OK)) {
              // This is not the HTTP status code
              throw new ServiceException(String.format("Bad status code returned from OGEL Registration service /update-transaction: %s",
                  result.status));
            }
            else {
              permissionsFinderDao.saveOgelRegistrationServiceTransactionExists(true);
              return redirect(result.redirectUrl);
            }
          }
        }, httpExecutionContext.current());
  }

}
