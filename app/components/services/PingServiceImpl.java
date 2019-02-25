package components.services;

import com.google.common.collect.ImmutableMap;
import com.google.inject.Inject;
import com.google.inject.name.Named;
import components.common.cache.CountryProvider;
import components.common.client.CustomerServiceClient;
import components.common.client.OgelServiceClient;
import components.common.client.PermissionsServiceClient;
import components.common.client.UserServiceClientBasicAuth;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;
import models.admin.PingResult;

public class PingServiceImpl implements PingService {

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

  private static boolean allServicesHealthy(boolean service1Healthy, boolean service2Healthy) {
    return service1Healthy && service2Healthy;
  }

  /**
   * We send a GET request to each of the dependent services and record the result
   */
  public PingResult pingServices() {
    PingResult result = new PingResult();

    Map<String, CompletableFuture<Boolean>> serviceNameToCheckResultFuture = ImmutableMap.of(
      "UserService", userClient.serviceReachable().toCompletableFuture(),
      "CustomerService", customerClient.serviceReachable().toCompletableFuture(),
      "PermissionsService", permissionsClient.serviceReachable().toCompletableFuture(),
      "OgelService", ogelClient.serviceReachable().toCompletableFuture(),
      "CountryService", countryProvider.serviceReachable().toCompletableFuture()
    );

    CompletableFuture.allOf(serviceNameToCheckResultFuture.values().toArray(new CompletableFuture[0])).join();

    serviceNameToCheckResultFuture.forEach(
      (serviceName, serviceCheckResultFuture) -> result.addDetailPart(serviceName, serviceCheckResultFuture.join()));

    boolean allServicesHealthy = serviceNameToCheckResultFuture.values()
      .stream()
      .map(CompletableFuture::join)
      .reduce(true, PingServiceImpl::allServicesHealthy);

    if (allServicesHealthy) {
      result.setStatusOk();
    } else {
      String statusMessage = "Services not responding: " + String.join(", ",
        serviceNameToCheckResultFuture.entrySet()
          .stream()
          .filter(entry -> entry.getValue().join().booleanValue() == false)
          .map(Entry::getKey)
          .collect(Collectors.toList()));
      result.setStatus(statusMessage);
    }

    return result;
  }
}
