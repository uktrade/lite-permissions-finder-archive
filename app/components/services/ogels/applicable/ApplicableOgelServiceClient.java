package components.services.ogels.applicable;

import com.fasterxml.jackson.databind.JsonNode;
import com.google.inject.Inject;
import com.google.inject.name.Named;
import components.common.logging.CorrelationId;
import components.services.ServiceResponseStatus;
import play.Logger;
import play.libs.Json;
import play.libs.concurrent.HttpExecutionContext;
import play.libs.ws.WSClient;
import play.libs.ws.WSRequest;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.CompletionStage;

public class ApplicableOgelServiceClient {
  private final WSClient ws;

  private final String webServiceHost;

  private final int webServicePort;

  private final int webServiceTimeout;

  private final String webServiceUrl;

  @Inject
  public ApplicableOgelServiceClient(WSClient ws,
                                     @Named("ogelServiceHost") String webServiceHost,
                                     @Named("ogelServicePort") int webServicePort,
                                     @Named("ogelServiceTimeout") int webServiceTimeout) {
    this.ws = ws;
    this.webServiceHost = webServiceHost;
    this.webServicePort = webServicePort;
    this.webServiceTimeout = webServiceTimeout;
    this.webServiceUrl= "http://" + webServiceHost + ":" + webServicePort + "/applicable-ogels";
  }

  public CompletionStage<Response> get(String controlCode, String sourceCountry, List<String> destinationCountries, List<String> activityTypes, HttpExecutionContext httpExecutionContext){

    WSRequest req = ws.url(webServiceUrl)
        .withRequestFilter(CorrelationId.requestFilter)
        .setRequestTimeout(webServiceTimeout)
        .setQueryParameter("controlCode", controlCode)
        .setQueryParameter("sourceCountry", sourceCountry);

    destinationCountries.forEach(country -> req.setQueryParameter("destinationCountry", country));

    activityTypes.forEach(activityType -> req.setQueryParameter("activityType", activityType));

    return req.get().handleAsync((response, error) -> {
      if (error != null) {
        Logger.error("Unchecked exception in ApplicableOgelService");
        Logger.error(error.getMessage(), error);
        return Response.failure(ServiceResponseStatus.UNCHECKED_EXCEPTION);
      }
      else if (response.getStatus() != 200) {
        Logger.error("Unexpected HTTP status code from ApplicableOgelService: {}", response.getStatus());
        return Response.failure(ServiceResponseStatus.UNEXPECTED_HTTP_STATUS_CODE);
      }
      else {
        return Response.success(response.asJson());
      }
    }, httpExecutionContext.current());
  }

  public static class Response {

    private List<ApplicableOgelServiceResult> results;

    private final ServiceResponseStatus status;

    private Response(ServiceResponseStatus status, JsonNode responseJson) {
      this.status = status;
      this.results = Arrays.asList(Json.fromJson(responseJson, ApplicableOgelServiceResult[].class));
    }

    private Response(ServiceResponseStatus status) {
      this.status = status;
      this.results = new ArrayList<>();
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

    public List<ApplicableOgelServiceResult> getResults() {
      return this.results;
    }

    public boolean isOk() {
      return this.status == ServiceResponseStatus.SUCCESS;
    }
  }
}
