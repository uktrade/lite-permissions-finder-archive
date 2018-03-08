package components.services.search.relatedcodes;

import com.google.common.net.UrlEscapers;
import com.google.inject.Inject;
import com.google.inject.name.Named;
import components.common.logging.CorrelationId;
import components.common.logging.ServiceClientLogger;
import exceptions.ServiceException;
import play.libs.concurrent.HttpExecutionContext;
import play.libs.ws.WSClient;

import java.time.Duration;
import java.util.concurrent.CompletionStage;

public class RelatedCodesServiceClient {

  private final HttpExecutionContext httpExecutionContext;
  private final WSClient wsClient;
  private final int webServiceTimeout;
  private final String webServiceUrl;
  private final String credentials;

  @Inject
  public RelatedCodesServiceClient(HttpExecutionContext httpExecutionContext,
                                   WSClient wsClient,
                                   @Named("searchServiceAddress") String webServiceAddress,
                                   @Named("searchServiceTimeout") int webServiceTimeout,
                                   @Named("searchServiceCredentials") String credentials) {
    this.httpExecutionContext = httpExecutionContext;
    this.wsClient = wsClient;
    this.webServiceTimeout = webServiceTimeout;
    this.webServiceUrl = webServiceAddress + "/related-codes";
    this.credentials = credentials;
  }

  public CompletionStage<RelatedCodesServiceResult> get(String controlCode) {
    return wsClient.url(webServiceUrl + "/" + UrlEscapers.urlFragmentEscaper().escape(controlCode))
        .setAuth(credentials)
        .setRequestFilter(CorrelationId.requestFilter)
        .setRequestFilter(ServiceClientLogger.requestFilter("Search", "GET", httpExecutionContext))
        .setRequestTimeout(Duration.ofMillis(webServiceTimeout))
        .get()
        .handleAsync((response, error) -> {
          if (error != null) {
            throw new ServiceException("Search service request failed", error);
          } else if (response.getStatus() != 200) {
            throw new ServiceException(String.format("Unexpected HTTP status code from Search service /search: %s", response.getStatus()));
          } else {
            return new RelatedCodesServiceResult(response.asJson());
          }
        }, httpExecutionContext.current());
  }
}
