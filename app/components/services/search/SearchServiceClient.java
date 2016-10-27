package components.services.search;

import com.google.inject.Inject;
import com.google.inject.name.Named;
import components.common.logging.CorrelationId;
import exceptions.ServiceException;
import play.libs.Json;
import play.libs.concurrent.HttpExecutionContext;
import play.libs.ws.WSClient;

import java.util.concurrent.CompletionStage;

public class SearchServiceClient {

  private final HttpExecutionContext httpExecutionContext;
  private final WSClient wsClient;
  private final int webServiceTimeout;
  private final String webServiceUrl;

  @Inject
  public SearchServiceClient(HttpExecutionContext httpExecutionContext,
                             WSClient wsClient,
                             @Named("searchServiceHost") String webServiceHost,
                             @Named("searchServicePort") int webServicePort,
                             @Named("searchServiceTimeout") int webServiceTimeout
  ){
    this.httpExecutionContext = httpExecutionContext;
    this.wsClient = wsClient;
    this.webServiceTimeout = webServiceTimeout;
    this.webServiceUrl= "http://" + webServiceHost + ":" + webServicePort + "/search";
  }

  public CompletionStage<SearchServiceResult> get(String searchTerm){
    return wsClient.url(webServiceUrl)
        .withRequestFilter(CorrelationId.requestFilter)
        .setRequestTimeout(webServiceTimeout)
        .setQueryParameter("term", searchTerm)
        .setQueryParameter("goodsType", "physical") // Hard coded to physical search for now
        .get()
        .handleAsync((response, throwable) -> {
          if (throwable != null) {
            throw new ServiceException("Error during Search service request", throwable);
          }
          else {
            return response;
          }
        })
        .thenApplyAsync(response -> {
          if (response.getStatus() != 200) {
            throw new ServiceException(String.format("Unexpected HTTP status code from Search service /search: %s", response.getStatus()));
          }
          else {
            return Json.fromJson(response.asJson(), SearchServiceResult.class);
          }
        }, httpExecutionContext.current());
  }

}
