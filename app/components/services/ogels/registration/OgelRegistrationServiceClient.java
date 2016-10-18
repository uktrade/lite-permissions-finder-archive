package components.services.ogels.registration;

import static play.mvc.Results.redirect;

import com.google.inject.Inject;
import com.google.inject.name.Named;
import components.common.client.CountryServiceClient;
import components.common.logging.CorrelationId;
import components.common.state.ContextParamManager;
import components.persistence.PermissionsFinderDao;
import components.services.controlcode.frontend.FrontendServiceClient;
import components.services.ogels.applicable.ApplicableOgelServiceClient;
import components.services.ogels.ogel.OgelServiceClient;
import exceptions.ServiceException;
import models.summary.Summary;
import org.apache.commons.lang3.StringUtils;
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
  private final String ogelRegistrationRootUrl;
  private final ContextParamManager contextParamManager;
  private final PermissionsFinderDao permissionsFinderDao;
  private final HttpExecutionContext httpExecutionContext;
  private final FrontendServiceClient frontendServiceClient;
  private final CountryServiceClient countryServiceClient;
  private final OgelServiceClient ogelServiceClient;
  private final ApplicableOgelServiceClient applicableOgelServiceClient;

  @Inject
  public OgelRegistrationServiceClient(WSClient wsClient,
                                       @Named("ogelRegistrationServiceHost") String webServiceHost,
                                       @Named("ogelRegistrationServicePort") int webServicePort,
                                       @Named("ogelRegistrationServiceTimeout") int webServiceTimeout,
                                       @Named("ogelRegistrationServiceSharedSecret") String webServiceSharedSecret,
                                       ContextParamManager contextParamManager,
                                       PermissionsFinderDao permissionsFinderDao,
                                       HttpExecutionContext httpExecutionContext,
                                       FrontendServiceClient frontendServiceClient,
                                       CountryServiceClient countryServiceClient,
                                       OgelServiceClient ogelServiceClient,
                                       ApplicableOgelServiceClient applicableOgelServiceClient) {
    this.wsClient = wsClient;
    this.webServiceTimeout = webServiceTimeout;
    this.webServiceSharedSecret = webServiceSharedSecret;
    this.ogelRegistrationRootUrl = "http://" + webServiceHost + ":" + webServicePort;
    this.webServiceUrl = ogelRegistrationRootUrl + "/update-transaction";
    this.contextParamManager = contextParamManager;
    this.permissionsFinderDao = permissionsFinderDao;
    this.httpExecutionContext = httpExecutionContext;
    this.frontendServiceClient = frontendServiceClient;
    this.countryServiceClient = countryServiceClient;
    this.ogelServiceClient = ogelServiceClient;
    this.applicableOgelServiceClient = applicableOgelServiceClient;
  }

  public CompletionStage<Result> updateTransactionAndRedirect(String transactionId) {
    WSRequest wsRequest = wsClient.url(webServiceUrl)
        .withRequestFilter(CorrelationId.requestFilter)
        .setRequestTimeout(webServiceTimeout)
        .setQueryParameter("securityToken", webServiceSharedSecret);

    CompletionStage<Summary> summaryStage = Summary.composeSummary(contextParamManager, permissionsFinderDao,
        httpExecutionContext, frontendServiceClient, countryServiceClient, ogelServiceClient, applicableOgelServiceClient);

    CompletionStage<OgelRegistrationServiceRequest> requestStage =
        summaryStage.thenApplyAsync(summary -> new OgelRegistrationServiceRequest(transactionId, summary),
            httpExecutionContext.current());

    return requestStage.thenApplyAsync(request -> wsRequest.post(Json.toJson(request)), httpExecutionContext.current())
        .thenCompose(Function.identity())
        .thenApplyAsync(response -> {
          if (response.getStatus() != 200) {
            throw new ServiceException(String.format("Unexpected HTTP status code from OgelRegistrationService: %s",
                response.getStatus()));
          }
          else {
            OgelRegistrationServiceResult result = OgelRegistrationServiceResult.buildFromJson(response.asJson());
            if (!StringUtils.isNotBlank(result.redirectUrl)) {
              throw new ServiceException("Unexpected redirect URL supplied from OgelRegistrationService");
            }
            else if (!StringUtils.equals(result.status, STATUS_CODE_OK)) {
              // This is not the HTTP status code
              throw new ServiceException(String.format("Bad status code returned from OgelRegistrationService: %s",
                  result.status));
            }
            else {
              permissionsFinderDao.saveOgelRegistrationServiceTransactionExists(true);
              return redirect(ogelRegistrationRootUrl + result.redirectUrl);
            }
          }
        }, httpExecutionContext.current());
  }

}
