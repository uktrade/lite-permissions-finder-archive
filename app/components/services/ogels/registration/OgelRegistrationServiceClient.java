package components.services.ogels.registration;

import static play.mvc.Results.badRequest;
import static play.mvc.Results.redirect;

import com.fasterxml.jackson.databind.JsonNode;
import com.google.inject.Inject;
import com.google.inject.name.Named;
import components.common.client.CountryServiceClient;
import components.common.state.ContextParamManager;
import components.persistence.PermissionsFinderDao;
import components.services.ServiceResponseStatus;
import components.services.controlcode.frontend.FrontendServiceClient;
import components.services.ogels.ogel.OgelServiceClient;
import models.summary.Summary;
import play.Logger;
import play.libs.Json;
import play.libs.concurrent.HttpExecutionContext;
import play.libs.ws.WSClient;
import play.libs.ws.WSRequest;
import play.mvc.Result;

import java.util.concurrent.CompletionStage;
import java.util.function.Function;

public class OgelRegistrationServiceClient {

  public final static String STATUS_CODE_OK = "ok";

  private final WSClient ws;
  private final String webServiceHost;
  private final int webServicePort;
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

  @Inject
  public OgelRegistrationServiceClient(WSClient ws,
                                       @Named("ogelRegistrationServiceHost") String webServiceHost,
                                       @Named("ogelRegistrationServicePort") int webServicePort,
                                       @Named("ogelRegistrationServiceTimeout") int webServiceTimeout,
                                       @Named("ogelRegistrationServiceSharedSecret") String webServiceSharedSecret,
                                       ContextParamManager contextParamManager,
                                       PermissionsFinderDao permissionsFinderDao,
                                       HttpExecutionContext httpExecutionContext,
                                       FrontendServiceClient frontendServiceClient,
                                       CountryServiceClient countryServiceClient,
                                       OgelServiceClient ogelServiceClient) {
    this.ws = ws;
    this.webServiceHost = webServiceHost;
    this.webServicePort = webServicePort;
    this.webServiceTimeout = webServiceTimeout;
    this.webServiceSharedSecret = webServiceSharedSecret;
    this.ogelRegistrationRootUrl = "http://" + webServiceHost + ":" + webServicePort;
    this.webServiceUrl = ogelRegistrationRootUrl + "/create-transaction";
    this.contextParamManager = contextParamManager;
    this.permissionsFinderDao = permissionsFinderDao;
    this.httpExecutionContext = httpExecutionContext;
    this.frontendServiceClient = frontendServiceClient;
    this.countryServiceClient = countryServiceClient;
    this.ogelServiceClient = ogelServiceClient;
  }

  public CompletionStage<Result> handOffToOgelRegistration(String transactionId){

    WSRequest wsRequest = ws.url(webServiceUrl)
        .setRequestTimeout(webServiceTimeout)
        .setQueryParameter("securityToken", webServiceSharedSecret);

    CompletionStage<Summary> summaryStage = Summary.composeSummary(contextParamManager, permissionsFinderDao,
        httpExecutionContext, frontendServiceClient, countryServiceClient, ogelServiceClient);

    CompletionStage<OgelRegistrationServiceRequest> requestStage =
        summaryStage.thenApply(summary -> new OgelRegistrationServiceRequest(transactionId, summary));

    return requestStage.thenApply(request -> wsRequest.post(Json.toJson(request)))
        .thenCompose(Function.identity())
        .handle((response, error) -> {
          if (error != null) {
            Logger.error("Unchecked exception in OGEL registration service");
            Logger.error(error.getMessage(), error);
            return Response.failure(ServiceResponseStatus.UNCHECKED_EXCEPTION);
          }
          else if (response.getStatus() != 200) {
            Logger.error("Unexpected HTTP status code from OGEL registration service: {}", response.getStatus());
            return Response.failure(ServiceResponseStatus.UNEXPECTED_HTTP_STATUS_CODE);
          }
          else {
            return Response.success(response.asJson());
          }
        }).thenApplyAsync(response -> {
          // TODO this entire handler isn't needed
          if (!response.isOk() && STATUS_CODE_OK.equals(response.getResult().status)
              && response.getResult().redirectUrl != null || response.getResult().redirectUrl.isEmpty()) {
            return badRequest("Invalid response from OGEL registration service");
          }
          return redirect(ogelRegistrationRootUrl + response.getResult().redirectUrl);
        });
  }

  public static class Response {

    private final OgelRegistrationServiceResult result;

    private final ServiceResponseStatus status;

    private Response(ServiceResponseStatus status, JsonNode responseJson) {
      this.status = status;
      this.result = Json.fromJson(responseJson, OgelRegistrationServiceResult.class);
    }

    private Response(ServiceResponseStatus status) {
      this.status = status;
      this.result = null;
    }

    public static Response success(JsonNode responseJson){
      return new Response(ServiceResponseStatus.SUCCESS, responseJson);
    }

    public static Response failure(ServiceResponseStatus status){
      return new Response(status);
    }

    public ServiceResponseStatus getStatus() {
      return status;
    }

    public OgelRegistrationServiceResult getResult() {
      return this.result;
    }

    public boolean isOk() {
      return this.status == ServiceResponseStatus.SUCCESS;
    }
  }

}
