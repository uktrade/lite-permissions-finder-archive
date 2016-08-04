package components.services.controlcode.frontend;

import com.fasterxml.jackson.databind.JsonNode;
import com.google.inject.Inject;
import com.google.inject.name.Named;
import components.services.controlcode.ServiceResponseStatus;
import play.Logger;
import play.libs.Json;
import play.libs.ws.WSClient;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;
import java.util.function.Function;

public class FrontendServiceClient {

  private static final long REQUEST_TIMEOUT_MS = 10000; //10 Seconds

  private final String webServiceUrl;

  private final WSClient ws;

  @Inject
  public FrontendServiceClient(WSClient ws, @Named("controlCodeFrontendServiceHostname") String webServiceUrl){
    this.ws = ws;
    this.webServiceUrl = "http://" + webServiceUrl + "/frontend-control-codes";
  }

  public CompletionStage<Response> get(String controlCode) {
    return ws.url(webServiceUrl + "/" + controlCode)
        .setRequestTimeout(REQUEST_TIMEOUT_MS).get()
        .handle((response, error) -> {
          if (error != null) {
            Logger.error("Unchecked exception in ControlCodeFrontendService");
            Logger.error(error.getMessage(), error);
            return CompletableFuture.completedFuture(Response.failure(ServiceResponseStatus.UNCHECKED_EXCEPTION));
          }
          else if (response.getStatus() != 200) {
            String errorMessage = response.asJson() != null ? errorMessage = response.asJson().get("message").asText() : "";
            Logger.error("Unexpected HTTP status code from ControlCodeFrontendService: {} {}", response.getStatus(), errorMessage);
            return CompletableFuture.completedFuture(Response.failure(ServiceResponseStatus.UNEXPECTED_HTTP_STATUS_CODE));
          }
          else {
            return CompletableFuture.completedFuture(Response.success(response.asJson()));
          }
        })
        .thenCompose(Function.identity());
  }

  public static class Response {

    private final FrontendServiceResult frontendServiceResult;

    private final ServiceResponseStatus status;

    private Response(ServiceResponseStatus status, JsonNode responseJson) {
      this.status = status;
      this.frontendServiceResult = Json.fromJson(responseJson, FrontendServiceResult.class);
    }

    private Response(ServiceResponseStatus status) {
      this.status = status;
      this.frontendServiceResult = null;
    }

    public static Response success(JsonNode responseJson) {
      return new Response(ServiceResponseStatus.SUCCESS, responseJson);
    }

    public static Response failure(ServiceResponseStatus status) {
      return new Response(status);
    }

    public FrontendServiceResult getFrontendServiceResult() {
      return frontendServiceResult;
    }

    public ServiceResponseStatus getStatus() {
      return status;
    }

    public boolean isOk() {
      return this.status == ServiceResponseStatus.SUCCESS;
    }

  }

}