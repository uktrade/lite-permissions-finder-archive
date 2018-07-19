package components.client;

import uk.gov.bis.lite.customer.api.view.CustomerView;
import uk.gov.bis.lite.customer.api.view.SiteView;

import java.util.List;
import java.util.concurrent.CompletionStage;

public interface CustomerServiceClient {

  CompletionStage<List<CustomerView>> getCustomersByUserId(String userId);

  CompletionStage<List<SiteView>> getSitesByCustomerIdUserId(String customerId, String userId);

  CompletionStage<SiteView> getSite(String siteId);

  CompletionStage<CustomerView> getCustomer(String customerId);

  CompletionStage<Boolean> serviceReachable();

}
