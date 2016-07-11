package controllers.services.controlcode.search;

import com.google.inject.Inject;
import com.google.inject.name.Named;
import controllers.services.controlcode.ServiceResponseStatus;
import play.Logger;
import play.libs.ws.WSClient;

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

  public CompletionStage<SearchServiceResponse> search(String searchTerm){
    return ws.url(webServiceUrl)
        .setRequestTimeout(REQUEST_TIMEOUT_MS)
        .setQueryParameter("term", searchTerm)
        .get().handle((response, error) -> {
          if (error != null) {
            Logger.error("Unchecked exception in ControlCodeSearchService");
            Logger.error(error.getMessage(), error);
            return CompletableFuture.completedFuture(
                SearchServiceResponse.builder()
                    .setStatus(ServiceResponseStatus.UNCHECKED_EXCEPTION)
                    .build()
            );
          }
          else if (response.getStatus() != 200) {
            Logger.error("Unexpected HTTP status code from ControlCodeSearchService: {}", response.getStatus());
            return CompletableFuture.completedFuture(
                SearchServiceResponse.builder()
                    .setStatus(ServiceResponseStatus.UNEXPECTED_HTTP_STATUS_CODE)
                    .build()
            );
          }
          else {
            return CompletableFuture.completedFuture(
                SearchServiceResponse.builder()
                    .setSearchResults(response.asJson())
                    .setStatus(ServiceResponseStatus.SUCCESS)
                    .build()
            );
          }
        })
        .thenCompose(Function.identity());
  }
}
