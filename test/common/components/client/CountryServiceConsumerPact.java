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
import pact.consumer.components.common.client.CommonCountryServiceSetConsumerPact;
import play.libs.ws.WSClient;
import play.test.WSTestClient;

public class CountryServiceConsumerPact {

  @Rule
  public final PactProviderRuleMk2 mockProvider = new PactProviderRuleMk2(PactConfig.COUNTRY_SERVICE, this);

  private WSClient wsClient;

  @Before
  public void setUp() throws Exception {
    wsClient = WSTestClient.newClient(mockProvider.getPort());
  }

  @After
  public void tearDown() throws Exception {
    wsClient.close();
  }

  @Pact(provider = PactConfig.COUNTRY_SERVICE, consumer = PactConfig.CONSUMER)
  public RequestResponsePact countrySetExists(PactDslWithProvider builder) {
    return CommonCountryServiceSetConsumerPact.countrySetExists(builder);
  }

  @Pact(provider = PactConfig.COUNTRY_SERVICE, consumer = PactConfig.CONSUMER)
  public RequestResponsePact countrySetDoesNotExist(PactDslWithProvider builder) {
    return CommonCountryServiceSetConsumerPact.countrySetDoesNotExist(builder);
  }

  @Test
  @PactVerification(value = PactConfig.COUNTRY_SERVICE, fragment = "countrySetExists")
  public void countrySetExists() throws Exception {
    CountryServiceClient client = CommonCountryServiceSetConsumerPact.buildCountryServiceSetExistsClient(mockProvider, wsClient);
    CommonCountryServiceSetConsumerPact.countrySetExists(client);
  }

  @Test
  @PactVerification(value = PactConfig.COUNTRY_SERVICE, fragment = "countrySetDoesNotExist")
  public void countrySetDoesNotExistPact() throws Exception {
    CountryServiceClient client = CommonCountryServiceSetConsumerPact.buildCountryServiceSetDoesNotExistClient(mockProvider, wsClient);
    CommonCountryServiceSetConsumerPact.countrySetDoesNotExist(client);
  }

}
