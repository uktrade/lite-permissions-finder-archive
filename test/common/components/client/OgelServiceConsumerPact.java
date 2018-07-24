package common.components.client;

import au.com.dius.pact.consumer.Pact;
import au.com.dius.pact.consumer.PactProviderRuleMk2;
import au.com.dius.pact.consumer.PactVerification;
import au.com.dius.pact.consumer.dsl.PactDslWithProvider;
import au.com.dius.pact.model.RequestResponsePact;
import components.common.client.OgelServiceClient;
import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import pact.consumer.components.common.client.CommonOgelServiceConsumerPact;
import play.libs.ws.WSClient;
import play.test.WSTestClient;

public class OgelServiceConsumerPact {

  @Rule
  public final PactProviderRuleMk2 mockProvider = new PactProviderRuleMk2(PactConfig.OGEL_SERVICE, this);

  private WSClient wsClient;
  private OgelServiceClient client;

  @Before
  public void setup() {
    wsClient = WSTestClient.newClient(mockProvider.getPort());
    client = CommonOgelServiceConsumerPact.buildClient(wsClient, mockProvider);
  }

  @After
  public void teardown() throws Exception {
    wsClient.close();
  }

  @Pact(provider = PactConfig.OGEL_SERVICE, consumer = PactConfig.CONSUMER)
  public RequestResponsePact applicableOgelsExist(PactDslWithProvider builder) {
    return CommonOgelServiceConsumerPact.applicableOgelsExist(builder);
  }

  @Pact(provider = PactConfig.OGEL_SERVICE, consumer = PactConfig.CONSUMER)
  public RequestResponsePact applicableOgelsDoNotExist(PactDslWithProvider builder) {
    return CommonOgelServiceConsumerPact.applicableOgelsDoNotExist(builder);
  }

  @Pact(provider = PactConfig.OGEL_SERVICE, consumer = PactConfig.CONSUMER)
  public RequestResponsePact applicableOgelsInvalidActivityType(PactDslWithProvider builder) {
    return CommonOgelServiceConsumerPact.applicableOgelsInvalidActivityType(builder);
  }

  @Pact(provider = PactConfig.OGEL_SERVICE, consumer = PactConfig.CONSUMER)
  public RequestResponsePact applicableOgelsExistForMultipleActivityTypes(PactDslWithProvider builder) {
    return CommonOgelServiceConsumerPact.applicableOgelsExistForMultipleActivityTypes(builder);
  }

  @Pact(provider = PactConfig.OGEL_SERVICE, consumer = PactConfig.CONSUMER)
  public RequestResponsePact ogelExists(PactDslWithProvider builder) {
    return CommonOgelServiceConsumerPact.ogelExists(builder);
  }

  @Pact(provider = PactConfig.OGEL_SERVICE, consumer = PactConfig.CONSUMER)
  public RequestResponsePact ogelDoesNotExist(PactDslWithProvider builder) {
    return CommonOgelServiceConsumerPact.ogelDoesNotExist(builder);
  }

  @Test
  @PactVerification(value = PactConfig.OGEL_SERVICE, fragment = "applicableOgelsExist")
  public void applicableOgelsExistPact() throws Exception {
    CommonOgelServiceConsumerPact.applicableOgelsExist(client);
  }

  @Test
  @PactVerification(value = PactConfig.OGEL_SERVICE, fragment = "applicableOgelsDoNotExist")
  public void applicableOgelsDoNotExistPact() throws Exception {
    CommonOgelServiceConsumerPact.applicableOgelsDoNotExist(client);
  }

  @Test
  @PactVerification(value = PactConfig.OGEL_SERVICE, fragment = "applicableOgelsInvalidActivityType")
  public void applicableOgelsInvalidActivityTypePact() throws Exception {
    CommonOgelServiceConsumerPact.applicableOgelsInvalidActivityType(client);
  }

  @Test
  @PactVerification(value = PactConfig.OGEL_SERVICE, fragment = "applicableOgelsExistForMultipleActivityTypes")
  public void applicableOgelsExistForMultipleActivityTypesPact() throws Exception {
    CommonOgelServiceConsumerPact.applicableOgelsExistForMultipleActivityTypes(client);
  }

  @Test
  @PactVerification(value = PactConfig.OGEL_SERVICE, fragment = "ogelExists")
  public void ogelExistsPact() throws Exception {
    CommonOgelServiceConsumerPact.ogelExists(client);
  }

  @Test
  @PactVerification(value = PactConfig.OGEL_SERVICE, fragment = "ogelDoesNotExist")
  public void ogelDoesNotExistTest() throws Exception {
    CommonOgelServiceConsumerPact.ogelDoesNotExist(client);
  }

}
