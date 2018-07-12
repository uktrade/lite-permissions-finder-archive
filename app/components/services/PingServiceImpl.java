package components.services;

import com.google.inject.Inject;
import com.google.inject.name.Named;
import components.client.OgelServiceClient;
import components.client.PermissionsServiceClient;
import models.admin.PingAuditResult;
import org.slf4j.LoggerFactory;
import play.libs.ws.WSClient;

import java.time.Duration;
import java.util.concurrent.CompletionStage;
import java.util.concurrent.ExecutionException;

public class PingServiceImpl implements PingService {

  private static final org.slf4j.Logger LOGGER = LoggerFactory.getLogger(PingServiceImpl.class);

  private final PermissionsServiceClient permissionsService;
  private final OgelServiceClient ogelService;


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
  public PingServiceImpl(PermissionsServiceClient permissionsService, OgelServiceClient ogelService,
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

  public PingAuditResult pingAudit() {
    LOGGER.info("pingAudit started...");

    PingAuditResult result = new PingAuditResult();

    try {
      CompletionStage<Boolean> userServiceStage = userServicePing();
      boolean userServiceOk = userServiceStage.toCompletableFuture().get();
      result.addDetailPart("UserService", userServiceOk);

      CompletionStage<Boolean> permissionsServiceStage = permissionsService.ping();
      boolean permissionsServiceOk = permissionsServiceStage.toCompletableFuture().get();
      result.addDetailPart("PermissionsService", permissionsServiceOk);

      CompletionStage<Boolean> ogelServiceStage = ogelService.ping();
      boolean ogelServiceOk = ogelServiceStage.toCompletableFuture().get();
      result.addDetailPart("OgelService", ogelServiceOk);

      CompletionStage<Boolean> countryServiceStage = countryServicePing();
      boolean countryServiceOk = countryServiceStage.toCompletableFuture().get();
      result.addDetailPart("CountryService", countryServiceOk);

      LOGGER.info("User service ping acknowledged: " + userServiceOk);
      LOGGER.info("Permissions service ping acknowledged: " + permissionsServiceOk);
      LOGGER.info("Ogel service ping acknowledged: " + ogelServiceOk);
      LOGGER.info("Country service ping acknowledged: " + countryServiceOk);

      if(userServiceOk && permissionsServiceOk && ogelServiceOk && countryServiceOk) {
        result.setStatusOk();
      }

    } catch (InterruptedException | ExecutionException e) {
      LOGGER.error("doPingAudit", e);
    }
    return result;
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
