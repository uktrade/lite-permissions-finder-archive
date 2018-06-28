package components.services;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.inject.Inject;
import com.google.inject.name.Named;
import components.common.logging.CorrelationId;
import components.common.logging.ServiceClientLogger;
import filters.common.JwtRequestFilter;
import org.slf4j.LoggerFactory;
import play.libs.Json;
import play.libs.concurrent.HttpExecutionContext;
import play.libs.ws.WSClient;
import play.libs.ws.WSRequest;
import play.libs.ws.WSResponse;
import uk.gov.bis.lite.customer.api.view.CustomerView;
import uk.gov.bis.lite.customer.api.view.SiteView;

import java.io.IOException;
import java.time.Duration;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.CompletionStage;
import java.util.concurrent.ExecutionException;

public class CustomerServiceImpl implements CustomerService {

  private static final org.slf4j.Logger LOGGER = LoggerFactory.getLogger(CustomerServiceImpl.class);

  private static final String SITE_PATH_TEMPLATE = "/user-sites/customer/%s/user/%s";
  private static final String USER_CUSTOMERS_PATH = "/user-customers/user/";
  private static final String CUSTOMERS_PATH = "/customers/";
  private static final String SITES_PATH = "/sites/";

  private final HttpExecutionContext httpContext;
  private final WSClient wsClient;
  private final String customerServiceAddress;
  private final int timeout;
  private final JwtRequestFilter jwtRequestFilter;
  private final ObjectMapper mapper;

  @Inject
  public CustomerServiceImpl(HttpExecutionContext httpContext, WSClient wsClient,
                             @Named("customerServiceAddress") String customerServiceAddress,
                             @Named("customerServiceTimeout") int timeout,
                             JwtRequestFilter jwtRequestFilter, ObjectMapper mapper) {
    this.httpContext = httpContext;
    this.wsClient = wsClient;
    this.customerServiceAddress = customerServiceAddress;
    this.timeout = timeout;
    this.jwtRequestFilter = jwtRequestFilter;
    this.mapper = mapper;
  }

  public Optional<List<SiteView>> getSitesByCustomerIdUserId(String customerId, String userId) {
    WSRequest request = wsClient.url(customerServiceAddress + String.format(SITE_PATH_TEMPLATE, customerId, userId))
        .setRequestFilter(ServiceClientLogger.requestFilter("Customer", "GET", httpContext))
        .setRequestFilter(jwtRequestFilter)
        .setRequestFilter(CorrelationId.requestFilter)
        .setRequestTimeout(Duration.ofMillis(timeout));
    try {
      WSResponse response = request.get().toCompletableFuture().get();
      return Optional.of(Arrays.asList(Json.fromJson(response.asJson(), SiteView[].class)));
    } catch (InterruptedException | ExecutionException e) {
      LOGGER.error("Exception", e);
    }
    return Optional.empty();
  }

  public Optional<List<CustomerView>> getCustomersByUserId(String userId) {
    WSRequest request = wsClient.url(customerServiceAddress + USER_CUSTOMERS_PATH + userId)
        .setRequestFilter(ServiceClientLogger.requestFilter("Customer", "GET", httpContext))
        .setRequestFilter(jwtRequestFilter)
        .setRequestFilter(CorrelationId.requestFilter)
        .setRequestTimeout(Duration.ofMillis(timeout));
    try {
      WSResponse response = request.get().toCompletableFuture().get();
      return Optional.of(Arrays.asList(Json.fromJson(response.asJson(), CustomerView[].class)));
    } catch (InterruptedException | ExecutionException e) {
      LOGGER.error("Exception", e);
    }
    return Optional.empty();
  }

  /**
   * Retrieves a customer by Id
   */
  public CompletionStage<Optional<CustomerView>> getCustomer(String customerId) {
    String path = CUSTOMERS_PATH + customerId;
    return get(path).thenApplyAsync(r -> {
      if (r != null) {
        try {
          return Optional.of(mapper.readValue(r, CustomerView.class));
        } catch (IOException e) {
          LOGGER.error("Failed to parse CustomerView service response. {request path=" + path + "}", e);
        }
      }
      return Optional.empty();
    });
  }

  /**
   * Retrieves a site by Id
   */
  public CompletionStage<SiteView> getSite(String siteId) {
    String path = SITES_PATH + siteId;
    return get(path).thenApplyAsync(r -> {
      if (r != null) {
        try {
          return mapper.readValue(r, SiteView.class);
        } catch (IOException e) {
          LOGGER.error("Failed to parse Customer service response. {request path=" + path + "}", e);
        }
      }
      throw new ClientException("Customer service error - Failed to get site details. {request path=" + path + "}");
    });
  }

  private CompletionStage<String> get(String path) {
    WSRequest request = wsClient.url(customerServiceAddress + path)
        .setRequestFilter(CorrelationId.requestFilter)
        .setRequestFilter(jwtRequestFilter)
        .setRequestTimeout(Duration.ofMillis(timeout));

    LOGGER.info("Sending GET request {}", path);

    return request.get().handle((result, error) -> {
      if (error != null) {
        LOGGER.error("Customer service client failure. {request path=" + path + "}", error);
        return null;
      } else if (result.getStatus() != 200) {
        LOGGER.error("Customer service error response - {} {request path=" + path + "}", result.getBody());
        return null;
      } else {
        return result.asJson().toString();
      }
    });
  }

  public static class ClientException extends RuntimeException {
    public ClientException(String message) {
      super(message);
    }
  }
}
