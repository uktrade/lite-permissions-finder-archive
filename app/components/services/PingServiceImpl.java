package components.services;

import com.google.inject.Inject;
import com.google.inject.name.Named;
import org.slf4j.LoggerFactory;
import play.libs.ws.WSClient;

import java.time.Duration;
import java.util.concurrent.CompletionStage;
import java.util.concurrent.ExecutionException;

public class PingServiceImpl implements PingService {

  private static final org.slf4j.Logger LOGGER = LoggerFactory.getLogger(PingServiceImpl.class);

  private final PermissionsService permissionsService;
  private final OgelService ogelService;

  // For Country Service
  private static final String PING_PATH = "/admin/ping";

  private final WSClient wsClient;
  private final String countryServiceAddress;
  private final int countryServiceTimeout;
  private final String countryServiceCredentials;

  // For User Service
  private final String userServiceAddress;
  private final int userServiceTimeout;
  private final String userServiceCredentials;

  @Inject
  public PingServiceImpl(PermissionsService permissionsService, OgelService ogelService,
                         @Named("countryServiceAddress") String countryServiceAddress,
                         @Named("countryServiceTimeout") int countryServiceTimeout,
                         @Named("countryServiceCredentials") String countryServiceCredentials,
                         WSClient wsClient,
                         @Named("userServiceAddress") String userServiceAddress,
                         @Named("userServiceTimeout") int userServiceTimeout,
                         @Named("userServiceCredentials") String userServiceCredentials) {
    this.permissionsService = permissionsService;
    this.ogelService = ogelService;

    this.wsClient = wsClient;
    this.countryServiceAddress = countryServiceAddress;
    this.countryServiceTimeout = countryServiceTimeout;
    this.countryServiceCredentials = countryServiceCredentials;

    this.userServiceAddress = userServiceAddress;
    this.userServiceTimeout = userServiceTimeout;
    this.userServiceCredentials = userServiceCredentials;
  }

  public void pingAudit() {
    LOGGER.info("pingAudit started...");

    try {
      CompletionStage<Boolean> userServiceStage = userServicePing();
      LOGGER.info("User service ping acknowledged: " + userServiceStage.toCompletableFuture().get());

      CompletionStage<Boolean> permissionsServiceStage = permissionsService.ping();
      LOGGER.info("Permissions service ping acknowledged: " + permissionsServiceStage.toCompletableFuture().get());

      CompletionStage<Boolean> ogelServiceStage = ogelService.ping();
      LOGGER.info("Ogel service ping acknowledged: " + ogelServiceStage.toCompletableFuture().get());

      CompletionStage<Boolean> countryServiceStage = countryServicePing();
      LOGGER.info("Country service ping acknowledged: " + countryServiceStage.toCompletableFuture().get());

    } catch (InterruptedException | ExecutionException e) {
      LOGGER.error("doPingAudit", e);
    }

  }

  private CompletionStage<Boolean> countryServicePing() {
    String url = countryServiceAddress + PING_PATH;
    return wsClient.url(url)
        .setRequestTimeout(Duration.ofMillis(countryServiceTimeout))
        .setAuth(countryServiceCredentials)
        .get()
        .handleAsync((response, error) -> response.getStatus() == 200);
  }

  private CompletionStage<Boolean> userServicePing() {
    String url = userServiceAddress + PING_PATH;
    return wsClient.url(url)
        .setRequestTimeout(Duration.ofMillis(userServiceTimeout))
        .setAuth(userServiceCredentials)
        .get()
        .handleAsync((response, error) -> response.getStatus() == 200);
  }
}
