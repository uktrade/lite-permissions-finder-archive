package components.services;

import com.google.common.net.UrlEscapers;
import com.google.inject.Inject;
import com.google.inject.name.Named;
import components.common.logging.CorrelationId;
import components.common.logging.ServiceClientLogger;
import exceptions.ServiceException;
import org.slf4j.LoggerFactory;
import play.libs.Json;
import play.libs.concurrent.HttpExecutionContext;
import play.libs.ws.WSClient;
import play.libs.ws.WSRequest;
import uk.gov.bis.lite.ogel.api.view.OgelFullView;

import java.time.Duration;
import java.util.Optional;
import java.util.concurrent.CompletionStage;

public class OgelServiceImpl implements OgelService {

  private static final org.slf4j.Logger LOGGER = LoggerFactory.getLogger(OgelServiceImpl.class);

  private static final String OGELS_PATH = "/ogels/";

  private final WSClient wsClient;
  private final String ogelServiceAddress;
  private final int ogelServiceTimeout;
  private final String credentials;
  private final String webServiceUrl;
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
    this.webServiceUrl = ogelServiceAddress + "/ogels";
    this.httpExecutionContext = httpExecutionContext;
  }

  public CompletionStage<OgelFullView> get(String ogelId) {
    return wsClient.url(webServiceUrl + "/" + UrlEscapers.urlFragmentEscaper().escape(ogelId))
        .setAuth(credentials)
        .setRequestFilter(CorrelationId.requestFilter)
        .setRequestFilter(ServiceClientLogger.requestFilter("OGEL", "GET", httpExecutionContext))
        .setRequestTimeout(Duration.ofMillis(ogelServiceTimeout))
        .get()
        .handleAsync((response, error) -> {
          if (error != null) {
            throw new ServiceException("OGEL service request failed", error);
          } else if (response.getStatus() != 200) {
            throw new ServiceException(String.format("Unexpected HTTP status code from OGEL service /ogels/%s: %s", ogelId, response.getStatus()));
          } else {
            return Json.fromJson(response.asJson(), OgelFullView.class);
          }
        }, httpExecutionContext.current());
  }

  public CompletionStage<Optional<OgelFullView>> getOgel(String ogelId) {
    String path = OGELS_PATH + ogelId;
    WSRequest request = wsClient.url(ogelServiceAddress + path)
        .setAuth(credentials)
        .setRequestFilter(CorrelationId.requestFilter)
        .setRequestTimeout(Duration.ofMillis(ogelServiceTimeout));

    return request.get().handleAsync((response, error) -> {
      if (error != null) {
        LOGGER.error("OGEL service client failure {request path=" + path + "}", error);
      } else if (response.getStatus() != 200) {
        LOGGER.error("OGEL service error response {request path=" + path + "} - {}", response.getBody());
      } else {
        OgelFullView ogelInfo = Json.fromJson(response.asJson(), OgelFullView.class);
        return Optional.of(ogelInfo);
      }
      return Optional.empty();
    });
  }

}