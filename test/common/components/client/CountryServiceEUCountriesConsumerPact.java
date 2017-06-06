package common.components.client;

import au.com.dius.pact.consumer.Pact;
import au.com.dius.pact.consumer.PactProviderRule;
import au.com.dius.pact.consumer.PactVerification;
import au.com.dius.pact.consumer.dsl.PactDslWithProvider;
import au.com.dius.pact.model.PactFragment;
import components.common.client.CountryServiceClient;
import models.common.Country;
import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import pact.PactConfig;
import pact.consumer.components.common.client.CountryServiceConsumerPactUtil;
import play.libs.Json;
import play.libs.concurrent.HttpExecutionContext;
import play.libs.ws.WS;
import play.libs.ws.WSClient;

import java.util.List;
import java.util.concurrent.ExecutionException;
@SuppressWarnings("Duplicates")
public class CountryServiceEUCountriesConsumerPact {
  private WSClient ws;
  private static final String path = "/countries/group/eu";

  @Rule
  public PactProviderRule mockProvider = new PactProviderRule(CountryServiceConsumerPactUtil.PROVIDER, this);

  @Before
  public void setUp() throws Exception {
    ws = WS.newClient(mockProvider.getConfig().getPort());
  }

  @After
  public void tearDown() throws Exception {
    ws.close();
  }

  @Pact(provider = CountryServiceConsumerPactUtil.PROVIDER, consumer = PactConfig.CONSUMER)
  public PactFragment EUCountriesExist(PactDslWithProvider builder) {
    String given = "EU countries exist";
    String uponReceiving = "a request for all EU countries";
    return CountryServiceConsumerPactUtil.buildCountriesExistFragment(builder, path, given, uponReceiving);
  }

  @Pact(provider = CountryServiceConsumerPactUtil.PROVIDER, consumer = PactConfig.CONSUMER)
  public PactFragment EUCountriesExistDoNotExist(PactDslWithProvider builder) {
    String given = "EU countries do not exist";
    String uponReceiving = "a request for all EU countries";
    return CountryServiceConsumerPactUtil.buildCountriesDoNotExistFragment(builder, path, given, uponReceiving);
  }

  @Test
  @PactVerification(value = CountryServiceConsumerPactUtil.PROVIDER, fragment = "EUCountriesExist")
  public void EUCountriesExistTest() {
    String clientUrl = mockProvider.getConfig().url() + path;
    CountryServiceClient client = new CountryServiceClient(new HttpExecutionContext(Runnable::run), ws, clientUrl, 10000, Json.newDefaultMapper());
    List<Country> results;
    try {
      results = client.getCountries().toCompletableFuture().get();
    }
    catch (InterruptedException | ExecutionException e) {
      throw new RuntimeException(e);
    }
    CountryServiceConsumerPactUtil.countriesExistAssertions(results);
  }

  @Test
  @PactVerification(value = CountryServiceConsumerPactUtil.PROVIDER, fragment = "EUCountriesExistDoNotExist")
  public void EUCountriesExistDoNotExistTest() {
    String clientUrl = mockProvider.getConfig().url() + path;
    CountryServiceClient client = new CountryServiceClient(new HttpExecutionContext(Runnable::run), ws, clientUrl, 10000, Json.newDefaultMapper());
    List<Country> results;
    try {
      results = client.getCountries().toCompletableFuture().get();
    }
    catch (InterruptedException | ExecutionException e) {
      throw new RuntimeException(e);
    }
    CountryServiceConsumerPactUtil.countriesDoNotExistAssertions(results);
  }
}
