package components.client;


import com.google.inject.Inject;
import com.google.inject.name.Named;
import components.common.logging.CorrelationId;
import components.common.logging.ServiceClientLogger;
import filters.common.JwtRequestFilter;
import play.Logger;
import play.libs.Json;
import play.libs.concurrent.HttpExecutionContext;
import play.libs.ws.WSClient;
import play.libs.ws.WSRequest;
import play.libs.ws.WSResponse;
import uk.gov.bis.lite.customer.api.view.CustomerView;
import uk.gov.bis.lite.customer.api.view.SiteView;

import java.time.Duration;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.ExecutionException;

public class CustomerServiceImpl implements CustomerService {

  private static final String SITE_PATH_TEMPLATE = "/user-sites/customer/%s/user/%s";
  private static final String USER_CUSTOMERS_PATH = "/user-customers/user/";

  private final HttpExecutionContext httpContext;
  private final WSClient wsClient;
  private final String address;
  private final int timeout;
  private final JwtRequestFilter jwtRequestFilter;

  @Inject
  public CustomerServiceImpl(HttpExecutionContext httpContext, WSClient wsClient, @Named("customerServiceAddress") String address,
                             @Named("customerServiceTimeout") int timeout, @Named("JwtRequestAuthFilter") JwtRequestFilter jwtRequestFilter) {
    this.httpContext = httpContext;
    this.wsClient = wsClient;
    this.address = address;
    this.timeout = timeout;
    this.jwtRequestFilter = jwtRequestFilter;
  }

  public Optional<List<SiteView>> getSitesByCustomerIdUserId(String customerId, String userId) {
    WSRequest request = wsClient.url(address + String.format(SITE_PATH_TEMPLATE, customerId, userId))
        .setRequestFilter(ServiceClientLogger.requestFilter("Customer", "GET", httpContext))
        .setRequestFilter(jwtRequestFilter)
        .setRequestFilter(CorrelationId.requestFilter)
        .setRequestTimeout(Duration.ofMillis(timeout));
    try {
      WSResponse response = request.get().toCompletableFuture().get();
      return Optional.of(Arrays.asList(Json.fromJson(response.asJson(), SiteView[].class)));
    } catch (InterruptedException | ExecutionException e) {
      Logger.error("Exception", e);
    }
    return Optional.empty();
  }

  public Optional<List<CustomerView>> getCustomersByUserId(String userId) {
    WSRequest request = wsClient.url(address + USER_CUSTOMERS_PATH + userId)
        .setRequestFilter(ServiceClientLogger.requestFilter("Customer", "GET", httpContext))
        .setRequestFilter(jwtRequestFilter)
        .setRequestFilter(CorrelationId.requestFilter)
        .setRequestTimeout(Duration.ofMillis(timeout));
    try {
      WSResponse response = request.get().toCompletableFuture().get();
      return Optional.of(Arrays.asList(Json.fromJson(response.asJson(), CustomerView[].class)));
    } catch (InterruptedException | ExecutionException e) {
      Logger.error("Exception", e);
    }
    return Optional.empty();
  }

}
