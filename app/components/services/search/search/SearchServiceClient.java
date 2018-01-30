package components.services.search.search;

import com.google.inject.Inject;
import com.google.inject.name.Named;
import components.common.logging.CorrelationId;
import components.common.logging.ServiceClientLogger;
import exceptions.ServiceException;
import play.libs.concurrent.HttpExecutionContext;
import play.libs.ws.WSClient;

import java.util.concurrent.CompletionStage;

public class SearchServiceClient {

  private final HttpExecutionContext httpExecutionContext;
  private final WSClient wsClient;
  private final int webServiceTimeout;
  private final String webServiceUrl;
  private final String credentials;

  @Inject
  public SearchServiceClient(HttpExecutionContext httpExecutionContext,
                             WSClient wsClient,
                             @Named("searchServiceAddress") String webServiceAddress,
                             @Named("searchServiceTimeout") int webServiceTimeout,
                             @Named("searchServiceCredentials") String credentials) {
    this.httpExecutionContext = httpExecutionContext;
    this.wsClient = wsClient;
    this.webServiceTimeout = webServiceTimeout;
    this.webServiceUrl = webServiceAddress + "/search";
    this.credentials = credentials;
  }

  public CompletionStage<SearchServiceResult> get(String searchTerm){
    return wsClient.url(webServiceUrl)
        .setAuth(credentials)
        .withRequestFilter(CorrelationId.requestFilter)
        .withRequestFilter(ServiceClientLogger.requestFilter("Search", "GET", httpExecutionContext))
        .setRequestTimeout(webServiceTimeout)
        .setQueryParameter("term", searchTerm)
        .setQueryParameter("goodsType", "physical") // Hard coded to physical search for now
        .get()
        .handleAsync((response, error) -> {
          if (error != null) {
            throw new ServiceException("Search service request failed", error);
          }
          else if (response.getStatus() != 200) {
            throw new ServiceException(String.format("Unexpected HTTP status code from Search service /search: %s", response.getStatus()));
          }
          else {
            return new SearchServiceResult(response.asJson());
          }
        }, httpExecutionContext.current());
  }

}
