package components.services.controlcode.frontend;

import com.google.common.net.UrlEscapers;
import com.google.inject.Inject;
import com.google.inject.name.Named;
import components.common.logging.CorrelationId;
import components.common.logging.ServiceClientLogger;
import exceptions.ServiceException;
import play.libs.concurrent.HttpExecutionContext;
import play.libs.ws.WSClient;

import java.util.concurrent.CompletionStage;

public class FrontendServiceClient {

  private final HttpExecutionContext httpExecutionContext;
  private final WSClient wsClient;
  private final int webServiceTimeout;
  private final String webServiceUrl;
  private final ServiceClientLogger serviceClientLogger;

  @Inject
  public FrontendServiceClient(HttpExecutionContext httpExecutionContext,
                               WSClient wsClient,
                               @Named("controlCodeServiceAddress") String webServiceAddress,
                               @Named("controlCodeServiceTimeout") int webServiceTimeout,
                               ServiceClientLogger serviceClientLogger) {
    this.httpExecutionContext = httpExecutionContext;
    this.wsClient = wsClient;
    this.webServiceTimeout = webServiceTimeout;
    this.webServiceUrl = webServiceAddress + "/frontend-control-codes";
    this.serviceClientLogger = serviceClientLogger;
  }

  public CompletionStage<FrontendServiceResult> get(String controlCode) {
    return wsClient.url(webServiceUrl + "/" + UrlEscapers.urlFragmentEscaper().escape(controlCode))
        .withRequestFilter(CorrelationId.requestFilter)
        .withRequestFilter(serviceClientLogger.requestFilter("Control Code", "GET"))
        .setRequestTimeout(webServiceTimeout)
        .get()
        .thenApplyAsync(response -> {
          if (response.getStatus() != 200) {
            String errorMessage = response.asJson() != null ? response.asJson().get("message").asText() : "";
            throw new ServiceException(String.format("Unexpected HTTP status code from Control Code service /frontend-control-codes: %s %s", response.getStatus(), errorMessage));
          }
          else {
            return new FrontendServiceResult(response.asJson());
          }
       }, httpExecutionContext.current());
  }

}