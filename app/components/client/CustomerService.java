package components.client;

import uk.gov.bis.lite.customer.api.view.CustomerView;
import uk.gov.bis.lite.customer.api.view.SiteView;

import java.util.List;
import java.util.Optional;
import java.util.concurrent.CompletionStage;

public interface CustomerService {

  Optional<List<CustomerView>> getCustomersByUserId(String userId);

  Optional<List<SiteView>> getSitesByCustomerIdUserId(String customerId, String userId);

  CompletionStage<SiteView> getSite(String siteId);

  CompletionStage<Optional<CustomerView>> getCustomer(String customerId);

}
