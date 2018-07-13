package components.services;

import com.google.inject.Inject;
import com.google.inject.name.Named;
import components.client.CustomerServiceClient;
import components.client.OgelServiceClient;
import components.client.PermissionsServiceClient;
import models.admin.AdminCheckResult;
import org.slf4j.LoggerFactory;
import play.libs.ws.WSClient;

import java.time.Duration;
import java.util.concurrent.CompletionStage;
import java.util.concurrent.ExecutionException;

public class PingServiceImpl implements PingService {

  private static final org.slf4j.Logger LOGGER = LoggerFactory.getLogger(PingServiceImpl.class);

  private final PermissionsServiceClient permissionsService;
  private final OgelServiceClient ogelService;
  private final CustomerServiceClient customerService;

  private final WSClient wsClient;

  // Country service config
  private final String countryServiceAddress;
  private final int countryServiceTimeout;
  private final String countryServiceCredentials;

  // User service config
  private final String userServiceAddress;
  private final int userServiceTimeout;
  private final String userServiceCredentials;

  @Inject
  public PingServiceImpl(PermissionsServiceClient permissionsService, OgelServiceClient ogelService,
                         CustomerServiceClient customerService, WSClient wsClient,
                         @Named("countryServiceAddress") String countryServiceAddress,
                         @Named("countryServiceTimeout") int countryServiceTimeout,
                         @Named("countryServiceCredentials") String countryServiceCredentials,
                         @Named("userServiceAddress") String userServiceAddress,
                         @Named("userServiceTimeout") int userServiceTimeout,
                         @Named("userServiceCredentials") String userServiceCredentials) {
    this.permissionsService = permissionsService;
    this.ogelService = ogelService;
    this.customerService = customerService;

    this.wsClient = wsClient;

    this.countryServiceAddress = countryServiceAddress;
    this.countryServiceTimeout = countryServiceTimeout;
    this.countryServiceCredentials = countryServiceCredentials;

    this.userServiceAddress = userServiceAddress;
    this.userServiceTimeout = userServiceTimeout;
    this.userServiceCredentials = userServiceCredentials;
  }

  public AdminCheckResult adminCheck(String adminCheckPath) {
    LOGGER.info("adminCheck started...");

    AdminCheckResult result = new AdminCheckResult();

    try {
      boolean userServiceReachable = userServiceReachable(adminCheckPath).toCompletableFuture().get();
      boolean permissionsServiceReachable = permissionsService.serviceReachable(adminCheckPath).toCompletableFuture().get();
      boolean ogelServiceReachable = ogelService.serviceReachable(adminCheckPath).toCompletableFuture().get();
      boolean countryServiceReachable = countryServiceReachable(adminCheckPath).toCompletableFuture().get();
      boolean customerServiceReachable = customerService.serviceReachable(adminCheckPath).toCompletableFuture().get();

      result.addDetailPart("UserService", userServiceReachable);
      result.addDetailPart("PermissionsService", permissionsServiceReachable);
      result.addDetailPart("OgelService", ogelServiceReachable);
      result.addDetailPart("CountryService", countryServiceReachable);
      result.addDetailPart("CustomerService", customerServiceReachable);

      LOGGER.info("User service reachable: " + userServiceReachable);
      LOGGER.info("Permissions service reachable: " + permissionsServiceReachable);
      LOGGER.info("Ogel service reachable: " + ogelServiceReachable);
      LOGGER.info("Country service reachable: " + countryServiceReachable);
      LOGGER.info("Customer service reachable: " + customerServiceReachable);

      if(userServiceReachable && permissionsServiceReachable && ogelServiceReachable
          && countryServiceReachable && customerServiceReachable) {
        result.setStatusOk();
      }

    } catch (InterruptedException | ExecutionException e) {
      LOGGER.error("doPingAudit", e);
    }
    return result;
  }

  private CompletionStage<Boolean> countryServiceReachable(String adminCheckPath) {
    String url = countryServiceAddress + adminCheckPath;
    return wsClient.url(url)
        .setRequestTimeout(Duration.ofMillis(countryServiceTimeout))
        .setAuth(countryServiceCredentials)
        .get()
        .handleAsync((response, error) -> response.getStatus() == 200);
  }

  private CompletionStage<Boolean> userServiceReachable(String adminCheckPath) {
    String url = userServiceAddress + adminCheckPath;
    return wsClient.url(url)
        .setRequestTimeout(Duration.ofMillis(userServiceTimeout))
        .setAuth(userServiceCredentials)
        .get()
        .handleAsync((response, error) -> response.getStatus() == 200);
  }
}
