package components.client;

import uk.gov.bis.lite.customer.api.view.CustomerView;
import uk.gov.bis.lite.customer.api.view.SiteView;

import java.util.List;
import java.util.Optional;

public interface CustomerService {

  Optional<List<CustomerView>> getCustomersByUserId(String userId);

  Optional<List<SiteView>> getSitesByCustomerIdUserId(String customerId, String userId);
}
