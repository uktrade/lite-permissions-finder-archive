package components.services.controlcode.search;

import com.google.inject.Inject;
import com.google.inject.name.Named;
import exceptions.ServiceException;
import play.libs.Json;
import play.libs.concurrent.HttpExecutionContext;
import play.libs.ws.WSClient;

import java.util.concurrent.CompletionStage;

public class SearchServiceClient {

  private final WSClient wsClient;

  private final int webServicePort;
  private final int webServiceTimeout;
  private final String webServiceUrl;

  @Inject
  public SearchServiceClient(WSClient wsClient,
                             @Named("controlCodeSearchServiceHost") String webServiceHost,
                             @Named("controlCodeSearchServicePort") int webServicePort,
                             @Named("controlCodeSearchServiceTimeout") int webServiceTimeout
  ){
    this.wsClient = wsClient;
    this.webServiceTimeout = webServiceTimeout;
    this.webServiceUrl= "http://" + webServiceHost + ":" + webServicePort + "/search";
  }

  public CompletionStage<SearchServiceResult> get(String searchTerm){
    return wsClient.url(webServiceUrl)
        .withRequestFilter(CorrelationId.requestFilter)
        .setRequestTimeout(webServiceTimeout)
        .setQueryParameter("term", searchTerm)
        .get()
        .thenApplyAsync(response -> {
          if (response.getStatus() != 200) {
            throw new ServiceException(String.format("Unexpected HTTP status code from ControlCodeSearchService: %s", response.getStatus()));
          }
          else {
            return Json.fromJson(response.asJson(), SearchServiceResult.class);
          }
        }, httpExecutionContext.current());
  }

}
