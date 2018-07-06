package components.services;

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

public class OgelServiceImpl implements OgelService {

  private final WSClient wsClient;
  private final String ogelServiceAddress;
  private final int ogelServiceTimeout;
  private final String credentials;
  private final HttpExecutionContext httpExecutionContext;

  @Inject
  public OgelServiceImpl(WSClient wsClient,
                         @Named("ogelServiceAddress") String ogelServiceAddress,
                         @Named("ogelServiceTimeout") int ogelServiceTimeout,
                         @Named("ogelServiceCredentials") String credentials,
                         HttpExecutionContext httpExecutionContext) {
    this.wsClient = wsClient;
    this.ogelServiceAddress = ogelServiceAddress;
    this.ogelServiceTimeout = ogelServiceTimeout;
    this.credentials = credentials;
    this.httpExecutionContext = httpExecutionContext;
  }

  @Override
  public CompletionStage<OgelFullView> getById(String ogelId) {
    String escapedId = UrlEscapers.urlFragmentEscaper().escape(ogelId);
    String url = ogelServiceAddress + "/ogels/" + escapedId;
    WSRequest request = wsClient.url(url)
        .setAuth(credentials)
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