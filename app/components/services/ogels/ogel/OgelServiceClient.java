package components.services.ogels.ogel;

import com.google.common.net.UrlEscapers;
import com.google.inject.Inject;
import com.google.inject.name.Named;
import components.common.logging.CorrelationId;
import components.common.logging.ServiceClientLogger;
import exceptions.ServiceException;
import play.libs.Json;
import play.libs.concurrent.HttpExecutionContext;
import play.libs.ws.WSClient;
import uk.gov.bis.lite.ogel.api.view.OgelFullView;

import java.time.Duration;
import java.util.concurrent.CompletionStage;

public class OgelServiceClient {

  private final HttpExecutionContext httpExecutionContext;
  private final WSClient wsClient;
  private final int webServiceTimeout;
  private final String webServiceUrl;
  private final String credentials;

  @Inject
  public OgelServiceClient(HttpExecutionContext httpExecutionContext,
                           WSClient wsClient,
                           @Named("ogelServiceAddress") String webServiceAddress,
                           @Named("ogelServiceTimeout") int webServiceTimeout,
                           @Named("ogelServiceCredentials") String credentials) {
    this.httpExecutionContext = httpExecutionContext;
    this.wsClient = wsClient;
    this.webServiceTimeout = webServiceTimeout;
    this.webServiceUrl = webServiceAddress + "/ogels";
    this.credentials = credentials;
  }

  public CompletionStage<OgelFullView> get(String ogelId) {
    return wsClient.url(webServiceUrl + "/" + UrlEscapers.urlFragmentEscaper().escape(ogelId))
        .setAuth(credentials)
        .setRequestFilter(CorrelationId.requestFilter)
        .setRequestFilter(ServiceClientLogger.requestFilter("OGEL", "GET", httpExecutionContext))
        .setRequestTimeout(Duration.ofMillis(webServiceTimeout))
        .get()
        .handleAsync((response, error) -> {
          if (error != null) {
            throw new ServiceException("OGEL service request failed", error);
          } else if (response.getStatus() != 200) {
            throw new ServiceException(String.format("Unexpected HTTP status code from OGEL service /ogels/%s: %s",
                ogelId, response.getStatus()));
          } else {
            return Json.fromJson(response.asJson(), OgelFullView.class);
          }
        }, httpExecutionContext.current());
  }
}
