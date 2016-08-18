package components.services.controlcode.search;

import com.fasterxml.jackson.databind.JsonNode;
import com.google.inject.Inject;
import com.google.inject.name.Named;
import components.services.ServiceResponseStatus;
import play.Logger;
import play.libs.Json;
import play.libs.ws.WSClient;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.CompletionStage;

public class SearchServiceClient {

  private final WSClient ws;

  private final String webServiceHost;

  private final int webServicePort;

  private final int webServiceTimeout;

  private final String webServiceUrl;

  @Inject
  public SearchServiceClient(WSClient ws,
                             @Named("controlCodeSearchServiceHost") String webServiceHost,
                             @Named("controlCodeSearchServicePort") int webServicePort,
                             @Named("controlCodeSearchServiceTimeout") int webServiceTimeout
  ){
    this.ws = ws;
    this.webServiceHost = webServiceHost;
    this.webServicePort = webServicePort;
    this.webServiceTimeout = webServiceTimeout;
    this.webServiceUrl= "http://" + webServiceHost + ":" + webServicePort + "/search";
  }

  public CompletionStage<Response> get(String searchTerm){
    return ws.url(webServiceUrl)
        .setRequestTimeout(webServiceTimeout)
        .setQueryParameter("term", searchTerm)
        .get().handle((response, error) -> {
          if (error != null) {
            Logger.error("Unchecked exception in ControlCodeSearchService");
            Logger.error(error.getMessage(), error);
            return Response.failure(ServiceResponseStatus.UNCHECKED_EXCEPTION);
          }
          else if (response.getStatus() != 200) {
            Logger.error("Unexpected HTTP status code from ControlCodeSearchService: {}", response.getStatus());
            return Response.failure(ServiceResponseStatus.UNEXPECTED_HTTP_STATUS_CODE);
          }
          else {
            return Response.success(response.asJson());
          }
        });
  }

  public static class Response {

    private List<SearchServiceResult> searchResults;

    private final ServiceResponseStatus status;

    private Response(ServiceResponseStatus status, JsonNode responseJson) {
      this.status = status;
      this.searchResults = Arrays.asList(Json.fromJson(responseJson.get("results"), SearchServiceResult[].class));
    }

    private Response(ServiceResponseStatus status) {
      this.status = status;
      this.searchResults = new ArrayList<>();
    }

    public static Response success(JsonNode responseJson){
      return new Response(ServiceResponseStatus.SUCCESS, responseJson);
    }

    public static Response failure(ServiceResponseStatus status){
      return new Response(status);
    }

    public List<SearchServiceResult> getSearchResults() {
      return searchResults;
    }

    public ServiceResponseStatus getStatus() {
      return status;
    }

    public boolean isOk() {
      return this.status == ServiceResponseStatus.SUCCESS;
    }

  }

}
