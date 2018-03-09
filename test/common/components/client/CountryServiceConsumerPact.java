package common.components.client;

import au.com.dius.pact.consumer.Pact;
import au.com.dius.pact.consumer.PactProviderRuleMk2;
import au.com.dius.pact.consumer.PactVerification;
import au.com.dius.pact.consumer.dsl.PactDslWithProvider;
import au.com.dius.pact.model.RequestResponsePact;
import components.common.client.CountryServiceClient;
import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import pact.PactConfig;
import pact.consumer.components.common.client.CountryServiceGroupConsumerPact;
import pact.consumer.components.common.client.CountryServiceSetConsumerPact;
import play.libs.ws.WSClient;
import play.test.WSTestClient;

public class CountryServiceConsumerPact {
  private WSClient ws;

  @Rule
  public PactProviderRuleMk2 mockProvider = new PactProviderRuleMk2(PactConfig.COUNTRY_SERVICE_PROVIDER, this);

  @Before
  public void setUp() throws Exception {
    ws = WSTestClient.newClient(mockProvider.getPort());
  }

  @After
  public void tearDown() throws Exception {
    ws.close();
  }

  @Pact(provider = PactConfig.COUNTRY_SERVICE_PROVIDER, consumer = PactConfig.CONSUMER)
  public RequestResponsePact countryGroupExists(PactDslWithProvider builder) {
    return CountryServiceGroupConsumerPact.countryGroupExists(builder);
  }

  @Pact(provider = PactConfig.COUNTRY_SERVICE_PROVIDER, consumer = PactConfig.CONSUMER)
  public RequestResponsePact countryGroupDoesNotExist(PactDslWithProvider builder) {
    return CountryServiceGroupConsumerPact.countryGroupDoesNotExist(builder);
  }

  @Pact(provider = PactConfig.COUNTRY_SERVICE_PROVIDER, consumer = PactConfig.CONSUMER)
  public RequestResponsePact countrySetExists(PactDslWithProvider builder) {
    return CountryServiceSetConsumerPact.countrySetExists(builder);
  }

  @Pact(provider = PactConfig.COUNTRY_SERVICE_PROVIDER, consumer = PactConfig.CONSUMER)
  public RequestResponsePact countrySetDoesNotExist(PactDslWithProvider builder) {
    return CountryServiceSetConsumerPact.countrySetDoesNotExist(builder);
  }

  @Test
  @PactVerification(value = PactConfig.COUNTRY_SERVICE_PROVIDER, fragment = "countryGroupExists")
  public void countryGroupExistsTest() {
    CountryServiceClient client = CountryServiceGroupConsumerPact.buildCountryGroupExistsClient(mockProvider, ws);
    CountryServiceGroupConsumerPact.doCountryGroupExistsTest(client);
  }

  @Test
  @PactVerification(value = PactConfig.COUNTRY_SERVICE_PROVIDER, fragment = "countryGroupDoesNotExist")
  public void countryGroupDoesNotExistTest() {
    CountryServiceClient client = CountryServiceGroupConsumerPact.buildCountryGroupDoesNotExistClient(mockProvider, ws);
    CountryServiceGroupConsumerPact.doCountryGroupDoesNotExistTest(client);
  }

  @Test
  @PactVerification(value = PactConfig.COUNTRY_SERVICE_PROVIDER, fragment = "countrySetExists")
  public void countrySetExistsTest() {
    CountryServiceClient client = CountryServiceSetConsumerPact.buildCountrySetExistsClient(mockProvider, ws);
    CountryServiceSetConsumerPact.doCountrySetExistsTest(client);
  }

  @Test
  @PactVerification(value = PactConfig.COUNTRY_SERVICE_PROVIDER, fragment = "countrySetDoesNotExist")
  public void countrySetDoesNotExistTest() {
    CountryServiceClient client = CountryServiceSetConsumerPact.buildCountrySetDoesNotExistClient(mockProvider, ws);
    CountryServiceSetConsumerPact.doCountrySetDoesNotExistTest(client);
  }

}
