package components.services.controlcode.search;

import com.fasterxml.jackson.databind.JsonNode;
import com.google.inject.Inject;
import com.google.inject.name.Named;
import components.services.controlcode.ServiceResponseStatus;
import play.Logger;
import play.libs.Json;
import play.libs.ws.WSClient;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;
import java.util.function.Function;

public class SearchServiceClient {

  private static final long REQUEST_TIMEOUT_MS = 10000; //10 Seconds

  private final String webServiceUrl;

  private final WSClient ws;

  @Inject
  public SearchServiceClient(WSClient ws, @Named("controlCodeSearchServiceHostname") String webServiceHostname){
    this.ws = ws;
    this.webServiceUrl= "http://" + webServiceHostname + "/search";
  }

  public CompletionStage<Response> get(String searchTerm){
    return ws.url(webServiceUrl)
        .setRequestTimeout(REQUEST_TIMEOUT_MS)
        .setQueryParameter("term", searchTerm)
        .get().handle((response, error) -> {
          if (error != null) {
            Logger.error("Unchecked exception in ControlCodeSearchService");
            Logger.error(error.getMessage(), error);
            return CompletableFuture.completedFuture(Response.failure(ServiceResponseStatus.UNCHECKED_EXCEPTION));
          }
          else if (response.getStatus() != 200) {
            Logger.error("Unexpected HTTP status code from ControlCodeSearchService: {}", response.getStatus());
            return CompletableFuture.completedFuture(Response.failure(ServiceResponseStatus.UNEXPECTED_HTTP_STATUS_CODE));
          }
          else {
            return CompletableFuture.completedFuture(Response.success(response.asJson()));
          }
        })
        .thenCompose(Function.identity());
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
