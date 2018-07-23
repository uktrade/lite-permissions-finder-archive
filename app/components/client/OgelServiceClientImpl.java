package components.client;

import com.google.common.net.UrlEscapers;
import com.google.inject.Inject;
import com.google.inject.name.Named;
import components.common.logging.CorrelationId;
import components.common.logging.ServiceClientLogger;
import exceptions.ServiceException;
import play.libs.Json;
import play.libs.concurrent.HttpExecutionContext;
import play.libs.ws.WSClient;
import play.libs.ws.WSRequest;
import uk.gov.bis.lite.ogel.api.view.OgelFullView;
import utils.RequestUtil;

import java.time.Duration;
import java.util.concurrent.CompletionStage;

public class OgelServiceClientImpl implements OgelServiceClient {

  private final WSClient wsClient;
  private final String ogelServiceAddress;
  private final int ogelServiceTimeout;
  private final String ogelServiceCredentials;
  private final HttpExecutionContext httpExecutionContext;

  @Inject
  public OgelServiceClientImpl(WSClient wsClient,
                               @Named("ogelServiceAddress") String ogelServiceAddress,
                               @Named("ogelServiceTimeout") int ogelServiceTimeout,
                               @Named("ogelServiceCredentials") String ogelServiceCredentials,
                               HttpExecutionContext httpExecutionContext) {
    this.wsClient = wsClient;
    this.ogelServiceAddress = ogelServiceAddress;
    this.ogelServiceTimeout = ogelServiceTimeout;
    this.ogelServiceCredentials = ogelServiceCredentials;
    this.httpExecutionContext = httpExecutionContext;
  }

  /**
   * Attempts a GET to path provided, checks for http 200 response
   */
  public CompletionStage<Boolean> serviceReachable(String adminCheckPath) {
    return wsClient.url(ogelServiceAddress + adminCheckPath)
        .setRequestTimeout(Duration.ofMillis(ogelServiceTimeout))
        .setAuth(ogelServiceCredentials)
        .get()
        .handleAsync((response, error) -> response.getStatus() == 200);
  }

  @Override
  public CompletionStage<OgelFullView> getById(String ogelId) {
    String escapedId = UrlEscapers.urlFragmentEscaper().escape(ogelId);
    String url = ogelServiceAddress + "/ogels/" + escapedId;
    WSRequest request = wsClient.url(url)
        .setAuth(ogelServiceCredentials)
        .setRequestFilter(CorrelationId.requestFilter)
        .setRequestFilter(ServiceClientLogger.requestFilter("OGEL", "GET", httpExecutionContext))
        .setRequestTimeout(Duration.ofMillis(ogelServiceTimeout));

    return request.get().handleAsync((response, error) -> {
      if (RequestUtil.hasError(response, error)) {
        String message = "Ogel service request failed";
        RequestUtil.logError(request, response, error, message);
        throw new ServiceException(message);
      } else {
        return Json.fromJson(response.asJson(), OgelFullView.class);
      }
    }, httpExecutionContext.current());
  }

}