package components.services;

import com.google.inject.Inject;
import com.google.inject.name.Named;
import components.common.logging.CorrelationId;
import components.common.logging.ServiceClientLogger;
import exceptions.ServiceException;
import filters.common.JwtRequestFilter;
import org.slf4j.LoggerFactory;
import play.libs.Json;
import play.libs.concurrent.HttpExecutionContext;
import play.libs.ws.WSClient;
import play.libs.ws.WSRequest;
import uk.gov.bis.lite.customer.api.view.CustomerView;
import uk.gov.bis.lite.customer.api.view.SiteView;
import utils.RequestUtil;

import java.time.Duration;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.CompletionStage;

public class CustomerServiceImpl implements CustomerService {

  private static final org.slf4j.Logger LOGGER = LoggerFactory.getLogger(CustomerServiceImpl.class);

  private static final String SITE_PATH_TEMPLATE = "/user-sites/customer/%s/user/%s";
  private static final String USER_CUSTOMERS_PATH = "/user-customers/user/";

  private final HttpExecutionContext httpContext;
  private final WSClient wsClient;
  private final String customerServiceAddress;
  private final int timeout;
  private final JwtRequestFilter jwtRequestFilter;

  @Inject
  public CustomerServiceImpl(HttpExecutionContext httpContext, WSClient wsClient,
                             @Named("customerServiceAddress") String customerServiceAddress,
                             @Named("customerServiceTimeout") int timeout,
                             JwtRequestFilter jwtRequestFilter) {
    this.httpContext = httpContext;
    this.wsClient = wsClient;
    this.customerServiceAddress = customerServiceAddress;
    this.timeout = timeout;
    this.jwtRequestFilter = jwtRequestFilter;
  }

  @Override
  public CompletionStage<List<SiteView>> getSitesByCustomerIdUserId(String customerId, String userId) {
    WSRequest request = wsClient.url(customerServiceAddress + String.format(SITE_PATH_TEMPLATE, customerId, userId))
        .setRequestFilter(ServiceClientLogger.requestFilter("Customer", "GET", httpContext))
        .setRequestFilter(jwtRequestFilter)
        .setRequestFilter(CorrelationId.requestFilter)
        .setRequestTimeout(Duration.ofMillis(timeout));
    return request.get().handle((response, error) -> {
      if (RequestUtil.hasError(response, error)) {
        String message = String.format("Unable to get sites with customerId %s and userId %s", customerId, userId);
        RequestUtil.logError(request, response, error, message);
        throw new ServiceException(message);
      } else {
        SiteView[] siteViews = Json.fromJson(response.asJson(), SiteView[].class);
        return Arrays.asList(siteViews);
      }
    });
  }

  @Override
  public CompletionStage<List<CustomerView>> getCustomersByUserId(String userId) {
    WSRequest request = wsClient.url(customerServiceAddress + USER_CUSTOMERS_PATH + userId)
        .setRequestFilter(ServiceClientLogger.requestFilter("Customer", "GET", httpContext))
        .setRequestFilter(jwtRequestFilter)
        .setRequestFilter(CorrelationId.requestFilter)
        .setRequestTimeout(Duration.ofMillis(timeout));
    return request.get().handle((response, error) -> {
      if (RequestUtil.hasError(response, error)) {
        String message = "Unable to get customers with userId " + userId;
        RequestUtil.logError(request, response, error, message);
        throw new ServiceException(message);
      } else {
        CustomerView[] customerViews = Json.fromJson(response.asJson(), CustomerView[].class);
        return Arrays.asList(customerViews);
      }
    });
  }

  @Override
  public CompletionStage<CustomerView> getCustomer(String customerId) {
    String url = String.format("%s/customers/%s", customerServiceAddress, customerId);
    WSRequest request = wsClient.url(url)
        .setRequestFilter(CorrelationId.requestFilter)
        .setRequestFilter(ServiceClientLogger.requestFilter("Customer", "GET", httpContext))
        .setRequestFilter(jwtRequestFilter)
        .setRequestTimeout(Duration.ofMillis(timeout));
    return request.get().handle((response, error) -> {
      if (RequestUtil.hasError(response, error)) {
        String message = "Unable to get customer with customerId " + customerId;
        RequestUtil.logError(request, response, error, message);
        throw new ServiceException(message);
      } else {
        return Json.fromJson(response.asJson(), CustomerView.class);
      }
    });
  }

  @Override
  public CompletionStage<SiteView> getSite(String siteId) {
    String url = String.format("%s/sites/%s", customerServiceAddress, siteId);
    WSRequest request = wsClient.url(url)
        .setRequestFilter(CorrelationId.requestFilter)
        .setRequestFilter(ServiceClientLogger.requestFilter("Customer", "GET", httpContext))
        .setRequestFilter(jwtRequestFilter)
        .setRequestTimeout(Duration.ofMillis(timeout));
    return request.get().handle((response, error) -> {
      if (RequestUtil.hasError(response, error)) {
        String message = "Unable to get site with siteId " + siteId;
        RequestUtil.logError(request, response, error, message);
        throw new ServiceException(message);
      } else {
        return Json.fromJson(response.asJson(), SiteView.class);
      }
    });
  }

}
