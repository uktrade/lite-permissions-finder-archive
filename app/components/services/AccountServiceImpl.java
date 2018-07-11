package components.services;

import com.google.inject.Inject;
import components.client.CustomerServiceClient;
import models.AccountData;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import uk.gov.bis.lite.customer.api.view.CustomerView;
import uk.gov.bis.lite.customer.api.view.SiteView;

import java.util.List;
import java.util.Optional;

public class AccountServiceImpl implements AccountService {

  private static final Logger LOGGER = LoggerFactory.getLogger(AccountServiceImpl.class);

  private final CustomerServiceClient customerService;

  @Inject
  public AccountServiceImpl(CustomerServiceClient customerService) {
    this.customerService = customerService;
  }

  @Override
  public Optional<AccountData> getAccountData(String userId) {
    Optional<CustomerView> customerViewOptional = getCustomer(userId);
    if (customerViewOptional.isPresent()) {
      CustomerView customerView = customerViewOptional.get();
      Optional<SiteView> siteViewOptional = getSite(customerView.getCustomerId(), userId);
      if (siteViewOptional.isPresent()) {
        SiteView siteView = siteViewOptional.get();
        return Optional.of(new AccountData(customerView, siteView));
      } else {
        return Optional.empty();
      }
    } else {
      return Optional.empty();
    }
  }

  private Optional<SiteView> getSite(String customerId, String userId) {
    List<SiteView> sites;
    try {
      sites = customerService.getSitesByCustomerIdUserId(customerId, userId).toCompletableFuture().get();
    } catch (Exception exception) {
      LOGGER.error("No site associated with customerId {} and userId {}", customerId, userId, exception);
      return Optional.empty();
    }
    if (sites.size() == 1) {
      return Optional.of(sites.get(0));
    } else {
      LOGGER.error("Expected userId {} to have 1 associated site but found {}", userId, sites.size());
      return Optional.empty();
    }
  }

  private Optional<CustomerView> getCustomer(String userId) {
    List<CustomerView> customerViews;
    try {
      customerViews = customerService.getCustomersByUserId(userId).toCompletableFuture().get();
    } catch (Exception exception) {
      LOGGER.error("No customer associated with userId {}", userId, exception);
      return Optional.empty();
    }
    if (customerViews.size() == 1) {
      return Optional.of(customerViews.get(0));
    } else {
      LOGGER.error("Expected userId {} to have 1 associated customer but found {}", userId, customerViews.size());
      return Optional.empty();
    }
  }

}
