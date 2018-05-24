package components.client;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.inject.Inject;
import com.google.inject.name.Named;
import components.common.logging.CorrelationId;
import filters.common.JwtRequestFilter;
import play.Logger;
import play.libs.ws.WSClient;
import play.libs.ws.WSRequest;
import uk.gov.bis.lite.customer.api.UsersResponse;
import uk.gov.bis.lite.customer.api.view.CustomerView;
import uk.gov.bis.lite.customer.api.view.SiteView;

import java.io.IOException;
import java.time.Duration;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.CompletionStage;

public class CustomerServiceClient {

  private static final Logger.ALogger LOGGER = Logger.of(CustomerServiceClient.class);

  private static final String CUSTOMERS_PATH = "/customers/";
  private static final String CUSTOMER_ADMINS_PATH = "/customer-admins/";
  private static final String CUSTOMER_SITES_PATH = "/user-sites/customer/";
  private static final String SEARCH_CUSTOMERS_PATH = "/search-customers/org-info";
  private static final String SITES_PATH = "/sites/";
  private static final String USER_CUSTOMERS_PATH = "/user-customers/user/";

  private final WSClient wsClient;
  private final String customerServiceAddress;
  private final int customerServiceTimeout;
  private final ObjectMapper objectMapper;
  private final JwtRequestFilter jwtRequestFilter;

  @Inject
  public CustomerServiceClient(WSClient wsClient,
                               @Named("customerServiceAddress") String customerServiceAddress,
                               @Named("customerServiceTimeout") int customerServiceTimeout,
                               ObjectMapper objectMapper,
                               @Named("JwtRequestAuthFilter") JwtRequestFilter jwtRequestFilter) {
    this.wsClient = wsClient;
    this.customerServiceAddress = customerServiceAddress;
    this.customerServiceTimeout = customerServiceTimeout;
    this.objectMapper = objectMapper;
    this.jwtRequestFilter = jwtRequestFilter;
  }

  /**
   * Retrieves customers by user Id
   *
   * @param userId the user Id
   * @return the list of customers if found, otherwise an empty list
   */
  public CompletionStage<List<CustomerView>> getCustomersByUserId(String userId) {
    return getCustomers(USER_CUSTOMERS_PATH + userId);
  }

  /**
   * Retrieves customers by EORI number or postcode
   *
   * @param eoriNumber the EORI number
   * @param postcode   the postcode
   * @return the list of customers if found, otherwise an empty list
   */
  public CompletionStage<List<CustomerView>> getCustomersByEori(String eoriNumber, String postcode) {
    Map<String, String> parameters = new HashMap<>();
    parameters.put("postcode", postcode);
    parameters.put("eori", eoriNumber);
    return getCustomers(SEARCH_CUSTOMERS_PATH, parameters);
  }

  /**
   * Retrieves customers by postcode
   *
   * @param postcode the postcode
   * @return the list of customers if found, otherwise an empty list
   */
  public CompletionStage<List<CustomerView>> getCustomersByPostcode(String postcode) {
    Map<String, String> parameters = new HashMap<>();
    parameters.put("postcode", postcode);
    return getCustomers(SEARCH_CUSTOMERS_PATH, parameters);
  }

  /**
   * Retrieves sites by customer Id and user Id
   *
   * @param customerId the Customer Id
   * @param userId     the User Id
   * @return the list of sites if found, otherwise an empty list
   */
  public CompletionStage<List<SiteView>> getCustomerSites(String customerId, String userId) {
    String path = CUSTOMER_SITES_PATH + customerId + "/user/" + userId;
    return get(path).thenApplyAsync(r -> {
      if (r != null) {
        try {
          return objectMapper.readValue(r, new TypeReference<List<SiteView>>() {});
        } catch (IOException e) {
          LOGGER.error("Failed to parse CustomerView service response. {request path=" + path + "}", e);
        }
      }

      return new ArrayList<>();
    });
  }

  /**
   * Retrieves a customer by Id
   *
   * @param customerId the Customer Id
   * @return the Customer if found, otherwise empty Optional
   */
  public CompletionStage<Optional<CustomerView>> getCustomer(String customerId) {
    String path = CUSTOMERS_PATH + customerId;
    return get(path).thenApplyAsync(r -> {
      if (r != null) {
        try {
          return Optional.of(objectMapper.readValue(r, CustomerView.class));
        } catch (IOException e) {
          LOGGER.error("Failed to parse CustomerView service response. {request path=" + path + "}", e);
        }
      }

      return Optional.empty();
    });

  }

  /**
   * Retrieves a site by Id
   *
   * @param siteId the Site Id
   * @return the Site if found, otherwise throw exception
   */
  public CompletionStage<SiteView> getSite(String siteId) {
    String path = SITES_PATH + siteId;
    return get(path).thenApplyAsync(r -> {
      if (r != null) {
        try {
          return objectMapper.readValue(r, SiteView.class);
        } catch (IOException e) {
          LOGGER.error("Failed to parse Customer service response. {request path=" + path + "}", e);
        }
      }
      throw new ClientException("Customer service error - Failed to get site details. {request path=" + path + "}");
    });
  }

  /**
   * Retrieves customer admins for a given customer Id
   *
   * @param customerId Customer Id
   * @return an Optional user admins
   */
  public CompletionStage<Optional<UsersResponse>> getCustomerAdmins(String customerId) {
    String path = CUSTOMER_ADMINS_PATH + customerId;
    return get(path).thenApplyAsync(r -> {
      if (r != null) {
        try {
          UsersResponse customerAdmins = objectMapper.readValue(r, UsersResponse.class);
          return Optional.of(customerAdmins);
        } catch (IOException e) {
          LOGGER.error("Failed to parse Customer service response. {request path=" + path + "}", e);
        }
      }
      return Optional.empty();
    });
  }

  private CompletionStage<List<CustomerView>> getCustomers(String path) {
    return getCustomers(path, null);
  }

  private CompletionStage<List<CustomerView>> getCustomers(String path, Map<String, String> parameters) {

    return get(path, parameters).thenApplyAsync(r -> {
      if (r != null) {
        try {
          return objectMapper.readValue(r, new TypeReference<List<CustomerView>>() {});
        } catch (IOException e) {
          LOGGER.error("Failed to parse Customer service response. {request path=" + path + "}", e);
        }
      }

      return new ArrayList<>();
    });
  }


  private CompletionStage<String> get(String path) {
    return get(path, null);
  }

  private CompletionStage<String> get(String path, Map<String, String> parameters) {
    WSRequest request = wsClient.url(customerServiceAddress + path)
        .setRequestFilter(CorrelationId.requestFilter)
        .setRequestFilter(jwtRequestFilter)
        .setRequestTimeout(Duration.ofMillis(customerServiceTimeout));

    if (parameters != null) {
      parameters.forEach(request::addQueryParameter);
    }

    LOGGER.info("Sending GET request {}", path);

    return request.get().handle((result, error) -> {
      if (error != null) {
        LOGGER.error("Customer service client failure. {request path=" + path + ", parameters=" + parameters + "}", error);
        return null;
      } else if (result.getStatus() != 200) {
        LOGGER.error("Customer service error response - {} {request path=" + path + ", parameters=" + parameters + "}", result.getBody());
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
