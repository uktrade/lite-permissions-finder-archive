package common.components.client;

import au.com.dius.pact.consumer.Pact;
import au.com.dius.pact.consumer.PactProviderRuleMk2;
import au.com.dius.pact.consumer.PactVerification;
import au.com.dius.pact.consumer.dsl.PactDslWithProvider;
import au.com.dius.pact.model.RequestResponsePact;
import components.common.client.UserServiceClientBasicAuth;
import components.common.client.UserServiceClientJwt;
import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import pact.consumer.components.common.client.CommonUserServiceConsumerPact;
import play.libs.ws.WSClient;
import play.test.WSTestClient;

public class UserServiceConsumerPact {

  @Rule
  public final PactProviderRuleMk2 mockProvider = new PactProviderRuleMk2(PactConfig.USER_SERVICE, this);

  private WSClient wsClient;

  @Before
  public void setUp() throws Exception {
    wsClient = WSTestClient.newClient(mockProvider.getPort());
  }

  @After
  public void tearDown() throws Exception {
    wsClient.close();
  }

  @Pact(provider = PactConfig.USER_SERVICE, consumer = PactConfig.CONSUMER)
  public static RequestResponsePact userAccountTypeUserExists(PactDslWithProvider builder) {
    return CommonUserServiceConsumerPact.userAccountTypeUserExists(builder);
  }

  @Pact(provider = PactConfig.USER_SERVICE, consumer = PactConfig.CONSUMER)
  public static RequestResponsePact userAccountTypeUserDoesNotExist(PactDslWithProvider builder) {
    return CommonUserServiceConsumerPact.userAccountTypeUserDoesNotExist(builder);
  }

  @Pact(provider = PactConfig.USER_SERVICE, consumer = PactConfig.CONSUMER)
  public static RequestResponsePact userPrivilegeViewUserExists(PactDslWithProvider builder) {
    return CommonUserServiceConsumerPact.userPrivilegeViewUserExists(builder);
  }

  @Pact(provider = PactConfig.USER_SERVICE, consumer = PactConfig.CONSUMER)
  public static RequestResponsePact userPrivilegeViewUserDoesNotExist(PactDslWithProvider builder) {
    return CommonUserServiceConsumerPact.userPrivilegeViewUserDoesNotExist(builder);
  }

  @Pact(provider = PactConfig.USER_SERVICE, consumer = PactConfig.CONSUMER)
  public static RequestResponsePact userDetailsViewUserExists(PactDslWithProvider builder) {
    return CommonUserServiceConsumerPact.userDetailsViewUserExists(builder);
  }

  @Pact(provider = PactConfig.USER_SERVICE, consumer = PactConfig.CONSUMER)
  public static RequestResponsePact userDetailsViewUserDoesNotExist(PactDslWithProvider builder) {
    return CommonUserServiceConsumerPact.userDetailsViewUserDoesNotExist(builder);
  }

  @Test
  @PactVerification(value = PactConfig.USER_SERVICE, fragment = "userAccountTypeUserExists")
  public void userAccountTypeUserExistsPact() throws Exception {
    UserServiceClientBasicAuth userServiceClientBasicAuth = CommonUserServiceConsumerPact.buildBasicAuthClient(wsClient, mockProvider);
    CommonUserServiceConsumerPact.userAccountTypeUserExists(userServiceClientBasicAuth);
  }

  @Test
  @PactVerification(value = PactConfig.USER_SERVICE, fragment = "userAccountTypeUserDoesNotExist")
  public void userAccountTypeUserDoesNotExistPact() {
    UserServiceClientBasicAuth userServiceClientBasicAuth = CommonUserServiceConsumerPact.buildBasicAuthClient(wsClient, mockProvider);
    CommonUserServiceConsumerPact.userAccountTypeUserDoesNotExist(userServiceClientBasicAuth);
  }

  @Test
  @PactVerification(value = PactConfig.USER_SERVICE, fragment = "userPrivilegeViewUserExists")
  public void userPrivilegeViewUserExistsPact() throws Exception {
    UserServiceClientJwt userServiceClientJwt = CommonUserServiceConsumerPact.buildJwtClient(wsClient, mockProvider);
    CommonUserServiceConsumerPact.userPrivilegeViewUserExists(userServiceClientJwt);
  }

  @Test
  @PactVerification(value = PactConfig.USER_SERVICE, fragment = "userPrivilegeViewUserDoesNotExist")
  public void userPrivilegeViewUserDoesNotExistPact() throws Exception {
    UserServiceClientJwt userServiceClientJwt = CommonUserServiceConsumerPact.buildJwtClient(wsClient, mockProvider);
    CommonUserServiceConsumerPact.userPrivilegeViewUserDoesNotExist(userServiceClientJwt);
  }

  @Test
  @PactVerification(value = PactConfig.USER_SERVICE, fragment = "userDetailsViewUserExists")
  public void userDetailsViewUserExistsPact() throws Exception {
    UserServiceClientJwt userServiceClientJwt = CommonUserServiceConsumerPact.buildJwtClient(wsClient, mockProvider);
    CommonUserServiceConsumerPact.userDetailsViewUserExists(userServiceClientJwt);
  }

  @Test
  @PactVerification(value = PactConfig.USER_SERVICE, fragment = "userDetailsViewUserDoesNotExist")
  public void userDetailsViewUserDoesNotExistPact() throws Exception {
    UserServiceClientJwt userServiceClientJwt = CommonUserServiceConsumerPact.buildJwtClient(wsClient, mockProvider);
    CommonUserServiceConsumerPact.userDetailsViewUserDoesNotExist(userServiceClientJwt);
  }

}
