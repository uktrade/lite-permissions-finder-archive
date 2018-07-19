package common.components.client;

import au.com.dius.pact.consumer.Pact;
import au.com.dius.pact.consumer.PactProviderRuleMk2;
import au.com.dius.pact.consumer.PactVerification;
import au.com.dius.pact.consumer.dsl.PactDslWithProvider;
import au.com.dius.pact.model.RequestResponsePact;
import components.common.client.PermissionsServiceClient;
import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import pact.PactConfig;
import pact.consumer.components.common.client.CommonPermissionsServiceConsumerPact;
import play.libs.ws.WSClient;
import play.test.WSTestClient;

public class PermissionsServiceConsumerPact {

  @Rule
  public final PactProviderRuleMk2 mockProvider = new PactProviderRuleMk2(PactConfig.PERMISSIONS_SERVICE, this);

  private WSClient wsClient;
  private PermissionsServiceClient client;

  @Before
  public void setup() {
    wsClient = WSTestClient.newClient(mockProvider.getPort());
    client = CommonPermissionsServiceConsumerPact.buildClient(wsClient, mockProvider);
  }

  @After
  public void teardown() throws Exception {
    wsClient.close();
  }

  @Pact(provider = PactConfig.PERMISSIONS_SERVICE, consumer = PactConfig.CONSUMER)
  public static RequestResponsePact registerExistingCustomerExistingSite(PactDslWithProvider builder) {
    return CommonPermissionsServiceConsumerPact.registerExistingCustomerExistingSite(builder);
  }

  @Pact(provider = PactConfig.PERMISSIONS_SERVICE, consumer = PactConfig.CONSUMER)
  public static RequestResponsePact registerNewCustomer(PactDslWithProvider builder) {
    return CommonPermissionsServiceConsumerPact.registerNewCustomer(builder);
  }

  @Pact(provider = PactConfig.PERMISSIONS_SERVICE, consumer = PactConfig.CONSUMER)
  public static RequestResponsePact registerExistingCustomerNewSite(PactDslWithProvider builder) {
    return CommonPermissionsServiceConsumerPact.registerExistingCustomerNewSite(builder);
  }

  @Pact(provider = PactConfig.PERMISSIONS_SERVICE, consumer = PactConfig.CONSUMER)
  public RequestResponsePact existingRegistration(PactDslWithProvider builder) {
    return CommonPermissionsServiceConsumerPact.existingRegistration(builder);
  }

  @Pact(provider = PactConfig.PERMISSIONS_SERVICE, consumer = PactConfig.CONSUMER)
  public RequestResponsePact noRegistration(PactDslWithProvider builder) {
    return CommonPermissionsServiceConsumerPact.noRegistration(builder);
  }

  @Pact(provider = PactConfig.PERMISSIONS_SERVICE, consumer = PactConfig.CONSUMER)
  public RequestResponsePact existingRegistrations(PactDslWithProvider builder) {
    return CommonPermissionsServiceConsumerPact.existingRegistrations(builder);
  }

  @Pact(provider = PactConfig.PERMISSIONS_SERVICE, consumer = PactConfig.CONSUMER)
  public RequestResponsePact noRegistrations(PactDslWithProvider builder) {
    return CommonPermissionsServiceConsumerPact.noRegistrations(builder);
  }

  @Pact(provider = PactConfig.PERMISSIONS_SERVICE, consumer = PactConfig.CONSUMER)
  public RequestResponsePact userNotFoundRegistrations(PactDslWithProvider builder) {
    return CommonPermissionsServiceConsumerPact.userNotFoundRegistrations(builder);
  }

  @Test
  @PactVerification(value = PactConfig.PERMISSIONS_SERVICE, fragment = "registerExistingCustomerExistingSite")
  public void registerExistingCustomerExistingSitePact() throws Exception {
    CommonPermissionsServiceConsumerPact.registerExistingCustomerExistingSite(client);
  }

  @Test
  @PactVerification(value = PactConfig.PERMISSIONS_SERVICE, fragment = "registerNewCustomer")
  public void registerNewCustomerPact() throws Exception {
    CommonPermissionsServiceConsumerPact.registerNewCustomer(client);
  }

  @Test
  @PactVerification(value = PactConfig.PERMISSIONS_SERVICE, fragment = "registerExistingCustomerNewSite")
  public void registerExistingCustomerNewSitePact() throws Exception {
    CommonPermissionsServiceConsumerPact.registerExistingCustomerNewSite(client);
  }

  @Test
  @PactVerification(value = PactConfig.PERMISSIONS_SERVICE, fragment = "existingRegistration")
  public void existingRegistrationPact() throws Exception {
    CommonPermissionsServiceConsumerPact.existingRegistration(client);
  }

  @Test
  @PactVerification(value = PactConfig.PERMISSIONS_SERVICE, fragment = "noRegistration")
  public void noRegistrationPact() {
    CommonPermissionsServiceConsumerPact.noRegistration(client);
  }

  @Test
  @PactVerification(value = PactConfig.PERMISSIONS_SERVICE, fragment = "existingRegistrations")
  public void existingRegistrationsPact() throws Exception {
    CommonPermissionsServiceConsumerPact.existingRegistrations(client);
  }

  @Test
  @PactVerification(value = PactConfig.PERMISSIONS_SERVICE, fragment = "noRegistrations")
  public void noRegistrationsPact() throws Exception {
    CommonPermissionsServiceConsumerPact.noRegistrations(client);
  }

  @Test
  @PactVerification(value = PactConfig.PERMISSIONS_SERVICE, fragment = "userNotFoundRegistrations")
  public void userNotFoundRegistrationsPact() throws Exception {
    CommonPermissionsServiceConsumerPact.userNotFoundRegistrations(client);
  }

}
