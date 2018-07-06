package models;

import uk.gov.bis.lite.customer.api.view.CustomerView;
import uk.gov.bis.lite.customer.api.view.SiteView;

public class AccountData {

  private final CustomerView customerView;
  private final SiteView siteView;

  public AccountData(CustomerView customerView, SiteView siteView) {
    this.customerView = customerView;
    this.siteView = siteView;
  }

  public CustomerView getCustomerView() {
    return customerView;
  }

  public SiteView getSiteView() {
    return siteView;
  }

}
