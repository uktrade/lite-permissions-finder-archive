package components.services;

import com.google.inject.Inject;
import components.client.CustomerService;
import components.common.auth.SpireAuthManager;
import components.persistence.LicenceFinderDao;
import play.Logger;
import uk.gov.bis.lite.customer.api.view.CustomerView;
import uk.gov.bis.lite.customer.api.view.SiteView;

import java.util.List;
import java.util.Optional;

public class LicenceFinderServiceImpl implements LicenceFinderService {

  private final LicenceFinderDao licenceFinderDao;
  //private final CustomerServiceClient customerService;
  private final CustomerService customerService1;
  private final SpireAuthManager authManager;

  @Inject
  public LicenceFinderServiceImpl(LicenceFinderDao licenceFinderDao,
                                  CustomerService customerService1, SpireAuthManager authManager) {
    this.licenceFinderDao = licenceFinderDao;
   // this.customerService = customerService;
    this.customerService1 = customerService1;
    this.authManager = authManager;
  }

  public void persistCustomerAndSiteData() {
    String userId = getUserId();
    Optional<String> optCustomerId = getCustomerId1(userId);
    if(optCustomerId.isPresent()) {
      String customerId = optCustomerId.get();
      licenceFinderDao.saveCustomerId(customerId); // persist customerId
      Logger.info("CustomerId persisted: " + customerId);
      Optional<String> optSiteId = getSiteId1(userId, optCustomerId.get());
      if(optSiteId.isPresent()) {
        String siteId = optSiteId.get();
        licenceFinderDao.saveSiteId(siteId); // persist siteId
        Logger.info("SiteId persisted: " + siteId);
      } else {
        Logger.warn("Not a single Site associated with user/customer: " + userId + "/" + customerId);
      }
    } else {
      Logger.warn("Not a single Customer associated with user: " + userId);
    }
  }

  /**
   * We only return a CustomerId if there is only one Customer associated with the user
   */
  private Optional<String> getCustomerId1(String userId) {
    Optional<List<CustomerView>> optCustomers = customerService1.getCustomersByUserId(userId);
    if (optCustomers.isPresent()) {
      List<CustomerView> customers = optCustomers.get();

      // Check for single customer only TODO when we have single Customer user
      if (customers.size() > 0) {
        return Optional.of(customers.get(0).getCustomerId());
      }
    }
    return Optional.empty();
  }

  /**
   * We only return a SiteId if there is only one Site associated with the user/customer
   */
  private Optional<String> getSiteId1(String userId, String customerId) {
    Optional<List<SiteView>> optSites = customerService1.getSitesByCustomerIdUserId(customerId, userId);
    if (optSites.isPresent()) {
      List<SiteView> sites = optSites.get();
      // Check for single site only TODO when we have single Site user
      if (sites.size() > 0) {
        return Optional.of(sites.get(0).getSiteId());
      }
    }
    return Optional.empty();
  }

  /**
   * We only return a CustomerId if there is only one Customer associated with the user
   *
  private Optional<String> getCustomerId(String userId) {
    try {
      List<CustomerView> customers = customerService.getCustomersByUserId(userId).toCompletableFuture().get();
      if(customers.size() == 1) {
        return Optional.of(customers.get(0).getCustomerId());
      }
    } catch (InterruptedException | ExecutionException e) {
      Logger.warn("InterruptedException | ExecutionException", e);
    }
    return Optional.empty();
  }/

  /**
   * We only return a SiteId if there is only one Site associated with the user/customer

  private Optional<String> getSiteId(String userId, String customerId) {
    try {
      List<SiteView> sites = customerService.getCustomerSites(customerId, userId).toCompletableFuture().get();
      if(sites.size() == 1) {
        return Optional.of(sites.get(0).getSiteId());
      }
    } catch (InterruptedException | ExecutionException e) {
      Logger.warn("InterruptedException | ExecutionException", e);
    }
    return Optional.empty();
  }*/

  private String getUserId() {
    return authManager.getAuthInfoFromContext().getId();
  }

}
