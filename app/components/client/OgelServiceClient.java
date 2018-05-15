package components.client;

import com.google.inject.Inject;
import com.google.inject.name.Named;
import components.common.logging.CorrelationId;
import play.Logger;
import play.libs.Json;
import play.libs.ws.WSClient;
import play.libs.ws.WSRequest;
import uk.gov.bis.lite.ogel.api.view.OgelFullView;

import java.time.Duration;
import java.util.Optional;
import java.util.concurrent.CompletionStage;

public class OgelServiceClient {

  private static final Logger.ALogger LOGGER = Logger.of(OgelServiceClient.class);

  private static final String OGELS_PATH = "/ogels/";

  private final WSClient wsClient;
  private final String ogelServiceAddress;
  private final int ogelServiceTimeout;
  private final String credentials;

  @Inject
  public OgelServiceClient(WSClient wsClient,
                           @Named("ogelServiceAddress") String ogelServiceAddress,
                           @Named("ogelServiceTimeout") int ogelServiceTimeout,
                           @Named("ogelServiceCredentials") String credentials) {
    this.wsClient = wsClient;
    this.ogelServiceAddress = ogelServiceAddress;
    this.ogelServiceTimeout = ogelServiceTimeout;
    this.credentials = credentials;
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