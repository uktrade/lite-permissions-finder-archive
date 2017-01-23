package components.services.search.relatedcodes;

import com.google.inject.Inject;
import com.google.inject.name.Named;
import components.common.logging.CorrelationId;
import exceptions.ServiceException;
import play.libs.Json;
import play.libs.concurrent.HttpExecutionContext;
import play.libs.ws.WSClient;
import uk.gov.bis.lite.searchmanagement.api.view.RelatedCodesView;

import java.util.concurrent.CompletionStage;

public class RelatedCodesServiceClient {

  private final HttpExecutionContext httpExecutionContext;
  private final WSClient wsClient;
  private final int webServiceTimeout;
  private final String webServiceUrl;

  @Inject
  public RelatedCodesServiceClient(HttpExecutionContext httpExecutionContext,
                                   WSClient wsClient,
                                   @Named("searchServiceAddress") String webServiceAddress,
                                   @Named("searchServiceTimeout") int webServiceTimeout) {
    this.httpExecutionContext = httpExecutionContext;
    this.wsClient = wsClient;
    this.webServiceTimeout = webServiceTimeout;
    this.webServiceUrl = webServiceAddress + "/related-codes";
  }

  public CompletionStage<RelatedCodesServiceResult> get(String controlCode){
    return wsClient.url(webServiceUrl + "/" + controlCode)
        .withRequestFilter(CorrelationId.requestFilter)
        .setRequestTimeout(webServiceTimeout)
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
            return new RelatedCodesServiceResult(response.asJson());
          }
        }, httpExecutionContext.current());
  }
}
