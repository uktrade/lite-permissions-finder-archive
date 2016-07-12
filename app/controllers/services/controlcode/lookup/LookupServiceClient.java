package controllers.services.controlcode.lookup;

import com.fasterxml.jackson.databind.JsonNode;
import com.google.inject.Inject;
import com.google.inject.name.Named;
import controllers.services.controlcode.ServiceResponseStatus;
import play.Logger;
import play.libs.Json;
import play.libs.ws.WSClient;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;
import java.util.function.Function;

public class LookupServiceClient {

  private static final long REQUEST_TIMEOUT_MS = 10000; //10 Seconds

  private final String webServiceUrl;

  private final WSClient ws;

  @Inject
  public LookupServiceClient(WSClient ws, @Named("controlCodeLookupServiceHostname") String webServiceUrl){
    this.ws = ws;
    this.webServiceUrl = "http://" + webServiceUrl + "/control-codes";
  }

  public CompletionStage<Response> lookup(String controlCode) {
    return ws.url(webServiceUrl + "/" + controlCode)
        .setRequestTimeout(REQUEST_TIMEOUT_MS).get()
        .handle((response, error) -> {
          if (error != null) {
            Logger.error("Unchecked exception in ControlCodeLookupService");
            Logger.error(error.getMessage(), error);
            return CompletableFuture.completedFuture(Response.failure(ServiceResponseStatus.UNCHECKED_EXCEPTION));
          }
          else if (response.getStatus() != 200) {
            String errorMessage = response.asJson() != null ? errorMessage = response.asJson().get("message").asText() : "";
            Logger.error("Unexpected HTTP status code from ControlCodeLookupService: {} {}", response.getStatus(), errorMessage);
            return CompletableFuture.completedFuture(Response.failure(ServiceResponseStatus.UNEXPECTED_HTTP_STATUS_CODE));
          }
          else {
            return CompletableFuture.completedFuture(Response.success(response.asJson()));
          }
        })
        .thenCompose(Function.identity());
  }

  public static class Response {

    private final LookupServiceResult lookupServiceResult;

    private final ServiceResponseStatus status;

    private Response(ServiceResponseStatus status, JsonNode responseJson) {
      this.status = status;
      this.lookupServiceResult = Json.fromJson(responseJson, LookupServiceResult.class);
    }

    private Response(ServiceResponseStatus status) {
      this.status = status;
      this.lookupServiceResult = null;
    }

    public static Response success(JsonNode responseJson) {
      return new Response(ServiceResponseStatus.SUCCESS, responseJson);
    }

    public static Response failure(ServiceResponseStatus status) {
      return new Response(status);
    }

    public LookupServiceResult getLookupServiceResult() {
      return lookupServiceResult;
    }

    public ServiceResponseStatus getStatus() {
      return status;
    }

    public boolean isOk() {
      return this.status == ServiceResponseStatus.SUCCESS;
    }

  }

}