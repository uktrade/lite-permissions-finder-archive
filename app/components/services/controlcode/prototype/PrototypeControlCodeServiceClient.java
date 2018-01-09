package components.services.controlcode.prototype;

import com.google.inject.Inject;
import com.google.inject.name.Named;
import components.common.logging.CorrelationId;
import components.common.logging.ServiceClientLogger;
import components.services.controlcode.related.RelatedControlsServiceResult;
import exceptions.ServiceException;
import play.libs.concurrent.HttpExecutionContext;
import play.libs.ws.WSClient;

import java.util.concurrent.CompletionStage;


public class PrototypeControlCodeServiceClient {

  private final HttpExecutionContext httpExecutionContext;
  private final WSClient wsClient;
  private final int webServiceTimeout;
  private final String webServiceUrl;
  private final String credentials;

  @Inject
  public PrototypeControlCodeServiceClient(HttpExecutionContext httpExecutionContext,
                                           WSClient wsClient,
                                           @Named("controlCodeServiceAddress") String webServiceAddress,
                                           @Named("controlCodeServiceTimeout") int webServiceTimeout,
                                           @Named("controlCodeServiceCredentials") String credentials) {
    this.httpExecutionContext = httpExecutionContext;
    this.wsClient = wsClient;
    this.webServiceTimeout = webServiceTimeout;
    this.webServiceUrl = webServiceAddress + "/control-codes/";
    this.credentials = credentials;
  }

  public CompletionStage<RelatedControlsServiceResult> get() {
    return wsClient.url(webServiceUrl)
        .setAuth(credentials)
        .withRequestFilter(CorrelationId.requestFilter)
        .withRequestFilter(ServiceClientLogger.requestFilter("Control Code", "GET", httpExecutionContext))
        .setRequestTimeout(webServiceTimeout)
        .get()
        .handleAsync((response, error) -> {
          if (error != null) {
            throw new ServiceException("Control Code service request failed", error);
          } else if (response.getStatus() != 200) {
            String errorMessage = response.asJson() != null ? response.asJson().get("message").asText() : "";
            throw new ServiceException(String.format("Unexpected HTTP status code from Control Code service: %s %s", response.getStatus(), errorMessage));
          } else {
            return new RelatedControlsServiceResult(response.asJson());
          }
        }, httpExecutionContext.current());
  }

}