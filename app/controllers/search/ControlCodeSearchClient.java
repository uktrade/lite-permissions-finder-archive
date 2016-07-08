package controllers.search;

import com.google.inject.Inject;
import com.google.inject.name.Named;
import play.Logger;
import play.libs.ws.WSClient;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;
import java.util.function.Function;

public class ControlCodeSearchClient {

  private static final long REQUEST_TIMEOUT_MS = 10000; //10 Seconds

  private final String webServiceUrl;

  private final WSClient ws;

  @Inject
  public ControlCodeSearchClient(WSClient ws, @Named("controlCodeSearchServiceHostname") String webServiceHostname){
    this.ws = ws;
    this.webServiceUrl= "http://" + webServiceHostname + "/search";
  }

  public CompletionStage<ControlCodeSearchResponse> search(String searchTerm){
    return ws.url(webServiceUrl)
        .setRequestTimeout(REQUEST_TIMEOUT_MS)
        .setQueryParameter("term", searchTerm)
        .get().handle((response, error) -> {
          if (error != null) {
            Logger.error("Unchecked exception in ControlCodeSearchService");
            Logger.error(error.getMessage(), error);
            return CompletableFuture.completedFuture(
                ControlCodeSearchResponse.builder()
                    .setStatus(ControlCodeResponseStatus.UNCHECKED_EXCEPTION)
                    .build()
            );
          }
          else if (response.getStatus() != 200) {
            Logger.error("Unexpected HTTP status code from ControlCodeSearchService: {}", response.getStatus());
            return CompletableFuture.completedFuture(
                ControlCodeSearchResponse.builder()
                    .setStatus(ControlCodeResponseStatus.UNEXPECTED_HTTP_STATUS_CODE)
                    .build()
            );
          }
          else {
            return CompletableFuture.completedFuture(
                ControlCodeSearchResponse.builder()
                    .setSearchResults(response.asJson())
                    .setStatus(ControlCodeResponseStatus.SUCCESS)
                    .build()
            );
          }
        })
        .thenCompose(Function.identity());
  }
}
