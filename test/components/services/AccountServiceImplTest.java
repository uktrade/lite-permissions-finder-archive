package components.services;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import components.common.client.CustomerServiceClient;
import models.AccountData;
import org.junit.Test;
import uk.gov.bis.lite.customer.api.view.CustomerView;
import uk.gov.bis.lite.customer.api.view.SiteView;

import java.util.Collections;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;

public class AccountServiceImplTest {

  private static final String CUSTOMER_ID = "customerId";
  private static final String USER_ID = "userId";

  private final CustomerServiceClient customerServiceClient = mock(CustomerServiceClient.class);
  private final AccountServiceImpl accountServiceImpl = new AccountServiceImpl(customerServiceClient);

  @Test
  public void shouldReturnAccountDataForOneCustomerAndOneSite() {
    CustomerView customerView = new CustomerView();
    customerView.setCustomerId(CUSTOMER_ID);
    SiteView siteView = new SiteView();
    when(customerServiceClient.getCustomersByUserId(USER_ID)).thenReturn(
        CompletableFuture.completedFuture(Collections.singletonList(customerView)));
    when(customerServiceClient.getSitesByCustomerIdUserId(CUSTOMER_ID, USER_ID)).thenReturn(
        CompletableFuture.completedFuture(Collections.singletonList(siteView)));

    Optional<AccountData> accountDataOptional = accountServiceImpl.getAccountData(USER_ID);
    assertThat(accountDataOptional).isPresent();
    AccountData accountData = accountDataOptional.get();
    assertThat(accountData.getCustomerView()).isEqualTo(customerView);
    assertThat(accountData.getSiteView()).isEqualTo(siteView);
  }

  @Test
  public void shouldNotReturnAccountDataForZeroCustomersAndZeroSites() {
    customerServiceClientShouldReturnCustomersAndSites(0, 0);
    assertThat(accountServiceImpl.getAccountData(USER_ID)).isEmpty();
  }

  @Test
  public void shouldNotReturnAccountDataForZeroCustomersAndOneSite() {
    customerServiceClientShouldReturnCustomersAndSites(0, 1);
    assertThat(accountServiceImpl.getAccountData(USER_ID)).isEmpty();
  }

  @Test
  public void shouldNotReturnAccountDataForZeroCustomersAndTwoSites() {
    customerServiceClientShouldReturnCustomersAndSites(0, 2);
    assertThat(accountServiceImpl.getAccountData(USER_ID)).isEmpty();
  }

  @Test
  public void shouldNotReturnAccountDataForOneCustomerAndZeroSites() {
    customerServiceClientShouldReturnCustomersAndSites(1, 0);
    assertThat(accountServiceImpl.getAccountData(USER_ID)).isEmpty();
  }

  @Test
  public void shouldNotReturnAccountDataForOneCustomerAndTwoSites() {
    customerServiceClientShouldReturnCustomersAndSites(1, 2);
    assertThat(accountServiceImpl.getAccountData(USER_ID)).isEmpty();
  }

  @Test
  public void shouldNotReturnAccountDataForTwoCustomersAndZeroSites() {
    customerServiceClientShouldReturnCustomersAndSites(2, 0);
    assertThat(accountServiceImpl.getAccountData(USER_ID)).isEmpty();
  }

  @Test
  public void shouldNotReturnAccountDataForTwoCustomersAndOneSite() {
    customerServiceClientShouldReturnCustomersAndSites(2, 1);
    assertThat(accountServiceImpl.getAccountData(USER_ID)).isEmpty();
  }

  @Test
  public void shouldNotReturnAccountDataForTwoCustomerAndTwoSites() {
    customerServiceClientShouldReturnCustomersAndSites(2, 2);
    assertThat(accountServiceImpl.getAccountData(USER_ID)).isEmpty();
  }

  private void customerServiceClientShouldReturnCustomersAndSites(int customerCount, int siteCount) {
    CustomerView customerView = new CustomerView();
    customerView.setCustomerId(CUSTOMER_ID);
    SiteView siteView = new SiteView();
    when(customerServiceClient.getSitesByCustomerIdUserId(CUSTOMER_ID, USER_ID)).thenReturn(
        CompletableFuture.completedFuture(Collections.nCopies(customerCount, siteView)));
    when(customerServiceClient.getCustomersByUserId(USER_ID)).thenReturn(
        CompletableFuture.completedFuture(Collections.nCopies(siteCount, customerView)));
  }

}
