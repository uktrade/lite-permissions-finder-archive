package components.services;

import com.google.inject.Inject;
import com.google.inject.name.Named;
import components.common.cache.CountryProvider;
import components.common.client.CustomerServiceClient;
import components.common.client.OgelServiceClient;
import components.common.client.PermissionsServiceClient;
import components.common.client.UserServiceClientBasicAuth;
import models.admin.PingResult;
import org.slf4j.LoggerFactory;

import java.util.concurrent.ExecutionException;

public class PingServiceImpl implements PingService {

  private static final org.slf4j.Logger LOGGER = LoggerFactory.getLogger(PingServiceImpl.class);

  private final PermissionsServiceClient permissionsClient;
  private final OgelServiceClient ogelClient;
  private final CustomerServiceClient customerClient;
  private final CountryProvider countryProvider;
  private final UserServiceClientBasicAuth userClient;

  @Inject
  public PingServiceImpl(PermissionsServiceClient permissionsClient, OgelServiceClient ogelClient,
                         CustomerServiceClient customerClient,
                         @Named("countryProviderExport") CountryProvider countryProvider,
                         UserServiceClientBasicAuth userClient) {
    this.permissionsClient = permissionsClient;
    this.ogelClient = ogelClient;
    this.customerClient = customerClient;
    this.countryProvider = countryProvider;
    this.userClient = userClient;
  }

  /**
   * We send a GET request to each of the dependent services and record the result
   */
  public PingResult pingServices() {
    PingResult result = new PingResult();

    try {
      boolean customerServiceReachable = customerClient.serviceReachable().toCompletableFuture().get();
      boolean permissionsServiceReachable = permissionsClient.serviceReachable().toCompletableFuture().get();
      boolean ogelServiceReachable = ogelClient.serviceReachable().toCompletableFuture().get();
      boolean countryServiceReachable = countryProvider.serviceReachable().toCompletableFuture().get();
      boolean userServiceReachable = userClient.serviceReachable().toCompletableFuture().get();

      result.addDetailPart("UserService", userServiceReachable);
      result.addDetailPart("CustomerService", customerServiceReachable);
      result.addDetailPart("PermissionsService", permissionsServiceReachable);
      result.addDetailPart("OgelService", ogelServiceReachable);
      result.addDetailPart("CountryService", countryServiceReachable);

      if (userServiceReachable && customerServiceReachable && permissionsServiceReachable
          && ogelServiceReachable && countryServiceReachable) {
        result.setStatusOk();
      }

    } catch (InterruptedException | ExecutionException e) {
      LOGGER.error("doPingAudit", e);
    }
    return result;
  }
}
