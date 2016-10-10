package components.services.ogels.ogel;

import com.fasterxml.jackson.databind.JsonNode;
import com.google.inject.Inject;
import com.google.inject.name.Named;
import components.common.logging.CorrelationId;
import components.services.ServiceResponseStatus;
import play.Logger;
import play.libs.Json;
import play.libs.concurrent.HttpExecutionContext;
import play.libs.ws.WSClient;

import java.util.concurrent.CompletionStage;

public class OgelServiceClient {

  private final HttpExecutionContext httpExecutionContext;
  private final WSClient ws;
  private final String webServiceHost;
  private final int webServicePort;
  private final int webServiceTimeout;
  private final String webServiceUrl;

  @Inject
  public OgelServiceClient(HttpExecutionContext httpExecutionContext,
                           WSClient ws,
                           @Named("ogelServiceHost") String webServiceHost,
                           @Named("ogelServicePort") int webServicePort,
                           @Named("ogelServiceTimeout") int webServiceTimeout) {
    this.httpExecutionContext = httpExecutionContext;
    this.ws = ws;
    this.webServiceHost = webServiceHost;
    this.webServicePort = webServicePort;
    this.webServiceTimeout = webServiceTimeout;
    this.webServiceUrl= "http://" + webServiceHost + ":" + webServicePort + "/ogels/";
  }

  public CompletionStage<Response> get(String ogelId){
    return ws.url(webServiceUrl + ogelId)
        .withRequestFilter(CorrelationId.requestFilter)
        .setRequestTimeout(webServiceTimeout)
        .get().handleAsync((response, error) -> {
          if (error != null) {
            Logger.error("Unchecked exception in OgelService");
            Logger.error(error.getMessage(), error);
            return Response.failure(ServiceResponseStatus.UNCHECKED_EXCEPTION);
          }
          else if (response.getStatus() != 200) {
            Logger.error("Unexpected HTTP status code from OgelService: {}", response.getStatus());
            return Response.failure(ServiceResponseStatus.UNEXPECTED_HTTP_STATUS_CODE);
          }
          else {
            return Response.success(response.asJson());
          }
        }, httpExecutionContext.current());
  }

  public static class Response {

    private final OgelServiceResult result;

    private final ServiceResponseStatus status;

    private Response(ServiceResponseStatus status, JsonNode responseJson) {
      this.status = status;
      this.result = Json.fromJson(responseJson, OgelServiceResult.class);
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

    public OgelServiceResult getResult() {
      return this.result;
    }

    public boolean isOk() {
      return this.status == ServiceResponseStatus.SUCCESS;
    }
  }
}
