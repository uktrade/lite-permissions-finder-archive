package components.services.ogels.conditions;

import com.fasterxml.jackson.databind.JsonNode;
import com.google.inject.Inject;
import com.google.inject.name.Named;
import components.common.logging.CorrelationId;
import components.services.ServiceResponseStatus;
import play.Logger;
import play.libs.Json;
import play.libs.concurrent.HttpExecutionContext;
import play.libs.ws.WSClient;

import java.util.Optional;
import java.util.concurrent.CompletionStage;

public class OgelConditionsServiceClient {

  private final WSClient wsClient;
  private final int webServiceTimeout;
  private final String webServiceUrl;

  @Inject
  public OgelConditionsServiceClient(WSClient wsClient,
                                     @Named("ogelServiceHost") String webServiceHost,
                                     @Named("ogelServicePort") int webServicePort,
                                     @Named("ogelServiceTimeout") int webServiceTimeout) {
    this.wsClient = wsClient;
    this.webServiceTimeout = webServiceTimeout;
    this.webServiceUrl = "http://" + webServiceHost + ":" + webServicePort + "/control-code-conditions";
  }

  public CompletionStage<Response> get(String ogelId, String controlCode, HttpExecutionContext httpExecutionContext){
    return wsClient.url(webServiceUrl + "/" + ogelId + "/" + controlCode)
        .withRequestFilter(CorrelationId.requestFilter)
        .setRequestTimeout(webServiceTimeout)
        .get().handleAsync((response, error) -> {
          if (error != null) {
            Logger.error("Unchecked exception in OgelConditionsService");
            Logger.error(error.getMessage(), error);
            return Response.failure(ServiceResponseStatus.UNCHECKED_EXCEPTION);
          }
          else if (response.getStatus() != 200 && response.getStatus() != 204) {
            Logger.error("Unexpected HTTP status code from OgelConditionsService: {}", response.getStatus());
            return Response.failure(ServiceResponseStatus.UNEXPECTED_HTTP_STATUS_CODE);
          }
          else if (response.getStatus() == 200) {
            return Response.success(response.asJson());
          }
          else if (response.getStatus() == 204) {
            return Response.success();
          }
          else {
            Logger.error("Invalid response state in OgelConditionsService");
            return Response.failure(ServiceResponseStatus.UNCHECKED_EXCEPTION);
          }
        }, httpExecutionContext.current());
  }

  /**
   * Check the OgelConditionsServiceResult returned by this client against a user provided answer 'conditionsApply'
   * The result of this check indicates if the OGEL, control code, and 'conditionsApply' tuple are valid for licence registration
   * @param result The result of this client
   * @param conditionsApply Whether the conditions in the result apply to the users item
   * @return Business logic result of the OGEL, control code and answer tuple
   */
  public static boolean isItemAllowed(Optional<OgelConditionsServiceResult> result, boolean conditionsApply) {
    return !result.isPresent() || (Boolean.parseBoolean(result.get().itemsAllowed) && conditionsApply);
  }

  public static class Response {

    private final Optional<OgelConditionsServiceResult> result;

    private final ServiceResponseStatus status;

    private Response(ServiceResponseStatus status, JsonNode responseJson) {
      this.status = status;
      this.result = Optional.of(Json.fromJson(responseJson, OgelConditionsServiceResult.class));
    }

    private Response(ServiceResponseStatus status) {
      this.status = status;
      this.result = Optional.empty();
    }

    public static Response success(JsonNode responseJson){
      return new Response(ServiceResponseStatus.SUCCESS, responseJson);
    }

    public static Response success(){
      return new Response(ServiceResponseStatus.SUCCESS);
    }

    public static Response failure(ServiceResponseStatus status){
      return new Response(status);
    }

    public ServiceResponseStatus getStatus() {
      return status;
    }

    public Optional<OgelConditionsServiceResult> getResult() {
      return this.result;
    }

    public boolean isOk() {
      return this.status == ServiceResponseStatus.SUCCESS;
    }

  }
}
