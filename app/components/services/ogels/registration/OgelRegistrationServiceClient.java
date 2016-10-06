package components.services.ogels.registration;

import static play.mvc.Results.badRequest;
import static play.mvc.Results.redirect;

import com.fasterxml.jackson.databind.JsonNode;
import com.google.inject.Inject;
import com.google.inject.name.Named;
import components.common.logging.CorrelationId;
import components.services.ServiceResponseStatus;
import components.services.controlcode.frontend.ControlCodeData;
import components.services.ogels.ogel.OgelServiceResult;
import models.common.Country;
import play.Logger;
import play.libs.Json;
import play.libs.concurrent.HttpExecutionContext;
import play.libs.ws.WSClient;
import play.mvc.Result;

import java.util.List;
import java.util.concurrent.CompletionStage;

public class OgelRegistrationServiceClient {

  public final static String STATUS_CODE_OK = "ok";

  private final WSClient ws;
  private final String webServiceHost;
  private final int webServicePort;
  private final int webServiceTimeout;
  private final String webServiceSharedSecret;
  private final String webServiceUrl;
  private final String ogelRegistrationRootUrl;

  @Inject
  public OgelRegistrationServiceClient(WSClient ws,
                           @Named("ogelRegistrationServiceHost") String webServiceHost,
                           @Named("ogelRegistrationServicePort") int webServicePort,
                           @Named("ogelRegistrationServiceTimeout") int webServiceTimeout,
                           @Named("ogelRegistrationServiceSharedSecret") String webServiceSharedSecret) {
    this.ws = ws;
    this.webServiceHost = webServiceHost;
    this.webServicePort = webServicePort;
    this.webServiceTimeout = webServiceTimeout;
    this.webServiceSharedSecret = webServiceSharedSecret;
    this.ogelRegistrationRootUrl = "http://" + webServiceHost + ":" + webServicePort;
    this.webServiceUrl = ogelRegistrationRootUrl + "/create-transaction";
  }

  public CompletionStage<Result> handOffToOgelRegistration(String transactionId, OgelServiceResult ogel,
                                                           List<Country> destinationCountries, ControlCodeData controlCode,
                                                           HttpExecutionContext httpExecutionContext){
    OgelRegistrationServiceRequest request = new OgelRegistrationServiceRequest(transactionId, ogel, destinationCountries, controlCode);

    return ws.url(webServiceUrl)
        .withRequestFilter(CorrelationId.requestFilter)
        .setRequestTimeout(webServiceTimeout)
        .setQueryParameter("securityToken", webServiceSharedSecret)
        .post(Json.toJson(request))
        .handleAsync((response, error) -> {
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
        }, httpExecutionContext.current())
        .thenApplyAsync(response -> {
          // TODO this entire handler isn't needed
          if (!response.isOk() && STATUS_CODE_OK.equals(response.getResult().status)
              && response.getResult().redirectUrl != null || response.getResult().redirectUrl.isEmpty()) {
            return badRequest("Invalid response from OGEL registration service");
          }
          return redirect(ogelRegistrationRootUrl + response.getResult().redirectUrl);
        }, httpExecutionContext.current());
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
