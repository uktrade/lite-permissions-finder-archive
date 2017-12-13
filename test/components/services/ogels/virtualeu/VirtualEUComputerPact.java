package components.services.ogels.virtualeu;

import static org.assertj.core.api.Assertions.assertThat;

import au.com.dius.pact.consumer.Pact;
import au.com.dius.pact.consumer.PactProviderRule;
import au.com.dius.pact.consumer.PactVerification;
import au.com.dius.pact.consumer.dsl.PactDslJsonBody;
import au.com.dius.pact.consumer.dsl.PactDslWithProvider;
import au.com.dius.pact.model.PactFragment;
import com.google.common.collect.ImmutableMap;
import exceptions.ServiceException;
import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import pact.PactConfig;
import play.libs.concurrent.HttpExecutionContext;
import play.libs.ws.WS;
import play.libs.ws.WSClient;
import uk.gov.bis.lite.ogel.api.view.VirtualEuView;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ExecutionException;

public class VirtualEUComputerPact {
  private VirtualEUOgelClient client;
  private WSClient ws;

  // service:password
  private static final Map<String, String> AUTH_HEADERS = ImmutableMap.of("Authorization", "Basic c2VydmljZTpwYXNzd29yZA==");
  private static final Map<String, String> CONTENT_TYPE_HEADERS = ImmutableMap.of("Content-Type", "application/json");

  private static final String OGEL_ID = "OGL1";
  private static final String CONTROL_CODE = "ML1a";
  private static final String SOURCE_COUNTRY = "CTRY0";
  private static final String DESTINATION_COUNTRY = "CRTY1";
  private static final String ADDITIONAL_DESTINATION_COUNTRY = "CRTY2";

  @Rule
  public PactProviderRule mockProvider = new PactProviderRule(PactConfig.OGEL_SERVICE_PROVIDER, this);

  @Before
  public void setUp() throws Exception {
    ws = WS.newClient(mockProvider.getConfig().getPort());
    client = new VirtualEUOgelClient(new HttpExecutionContext(Runnable::run),
        ws,
        mockProvider.getConfig().url(),
        10000,
        "service:password");
  }

  @After
  public void tearDown() throws Exception {
    ws.close();
  }

  @Pact(provider = PactConfig.OGEL_SERVICE_PROVIDER, consumer = PactConfig.CONSUMER)
  public PactFragment virtualEUOgelExists(PactDslWithProvider builder) {
    PactDslJsonBody body = new PactDslJsonBody()
        .booleanType("virtualEu", true)
        .stringType("ogelId", OGEL_ID)
        .asBody();

    return builder
        .given("parameters match virtual EU ogel")
        .uponReceiving("a request to check parameters for a virtual EU ogel")
          .headers(AUTH_HEADERS)
          .path("/virtual-eu")
          .query("controlCode=" + CONTROL_CODE + "&sourceCountry=" + SOURCE_COUNTRY + "&destinationCountry=" + DESTINATION_COUNTRY)
          .method("GET")
        .willRespondWith()
          .status(200)
          .headers(CONTENT_TYPE_HEADERS)
          .body(body)
        .toFragment();
  }

  @Pact(provider = PactConfig.OGEL_SERVICE_PROVIDER, consumer = PactConfig.CONSUMER)
  public PactFragment virtualEUOgelDoesNotExist(PactDslWithProvider builder) {
    PactDslJsonBody body = new PactDslJsonBody()
        .booleanType("virtualEu", false)
        .asBody();

    return builder
        .given("parameters do not match virtual EU ogel")
        .uponReceiving("a request to check parameters for a virtual EU ogel")
          .headers(AUTH_HEADERS)
          .path("/virtual-eu")
          .query("controlCode=" + CONTROL_CODE + "&sourceCountry=" + SOURCE_COUNTRY + "&destinationCountry=" + DESTINATION_COUNTRY)
          .method("GET")
        .willRespondWith()
          .status(200)
          .headers(CONTENT_TYPE_HEADERS)
          .body(body)
        .toFragment();
  }

  @Pact(provider = PactConfig.OGEL_SERVICE_PROVIDER, consumer = PactConfig.CONSUMER)
  public PactFragment virtualEUOgelExistsForMultipleDestinations(PactDslWithProvider builder) {
    PactDslJsonBody body = new PactDslJsonBody()
        .booleanType("virtualEu", true)
        .stringType("ogelId", OGEL_ID)
        .asBody();

    return builder
        .given("parameters match virtual EU ogel")
        .uponReceiving("a request to check parameters for a virtual EU ogel with multiple destinations")
          .headers(AUTH_HEADERS)
          .path("/virtual-eu")
          .query("controlCode=" + CONTROL_CODE + "&sourceCountry=" + SOURCE_COUNTRY + "&destinationCountry=" + DESTINATION_COUNTRY + "&destinationCountry=" + ADDITIONAL_DESTINATION_COUNTRY)
          .method("GET")
        .willRespondWith()
          .status(200)
          .headers(CONTENT_TYPE_HEADERS)
          .body(body)
        .toFragment();
  }

  @Pact(provider = PactConfig.OGEL_SERVICE_PROVIDER, consumer = PactConfig.CONSUMER)
  public PactFragment destinationCountryParametersMissing(PactDslWithProvider builder) {
    return builder
        .given("parameters match virtual EU ogel")
        .uponReceiving("a request with a missing parameter")
          .headers(AUTH_HEADERS)
          .path("/virtual-eu")
          .query("controlCode=" + CONTROL_CODE + "&sourceCountry=" + SOURCE_COUNTRY)
          .method("GET")
        .willRespondWith()
          .status(400)
        .toFragment();
  }


  @Test
  @PactVerification(value = PactConfig.OGEL_SERVICE_PROVIDER, fragment = "virtualEUOgelExists")
  public void virtualEUOgelExistsTest() throws Exception {
    VirtualEuView result;
    try {
      result = client.sendServiceRequest(CONTROL_CODE, SOURCE_COUNTRY, Collections.singletonList(DESTINATION_COUNTRY))
          .toCompletableFuture().get();
    }
    catch (InterruptedException | ExecutionException e) {
      throw new RuntimeException(e);
    }
    assertThat(result).isNotNull();
    assertThat(result.isVirtualEu()).isTrue();
    assertThat(result.getOgelId()).isEqualTo(OGEL_ID);
  }

  @Test
  @PactVerification(value = PactConfig.OGEL_SERVICE_PROVIDER, fragment = "virtualEUOgelDoesNotExist")
  public void virtualEUOgelDoesNotExistTest() throws Exception {
    VirtualEuView result;
    try {
      result = client.sendServiceRequest(CONTROL_CODE, SOURCE_COUNTRY, Collections.singletonList(DESTINATION_COUNTRY))
          .toCompletableFuture().get();
    }
    catch (InterruptedException | ExecutionException e) {
      throw new RuntimeException(e);
    }
    assertThat(result).isNotNull();
    assertThat(result.isVirtualEu()).isFalse();
    assertThat(result.getOgelId()).isNull();
  }

  @Test
  @PactVerification(value = PactConfig.OGEL_SERVICE_PROVIDER, fragment = "virtualEUOgelExistsForMultipleDestinations")
  public void virtualEUOgelExistsForMultipleDestinationsTest() throws Exception {
    VirtualEuView result;
    try {
      result = client.sendServiceRequest(CONTROL_CODE, SOURCE_COUNTRY, Arrays.asList(DESTINATION_COUNTRY, ADDITIONAL_DESTINATION_COUNTRY))
          .toCompletableFuture().get();
    }
    catch (InterruptedException | ExecutionException e) {
      throw new RuntimeException(e);
    }
    assertThat(result).isNotNull();
    assertThat(result.isVirtualEu()).isTrue();
    assertThat(result.getOgelId()).isEqualTo(OGEL_ID);
  }

  @Test
  @PactVerification(value = PactConfig.OGEL_SERVICE_PROVIDER, fragment = "destinationCountryParametersMissing")
  public void destinationCountryParametersMissingTest() throws Exception {
    VirtualEuView result = null;
    try {
      result = client.sendServiceRequest(CONTROL_CODE, SOURCE_COUNTRY, Collections.emptyList())
          .toCompletableFuture().get();
    }
    catch (InterruptedException | ExecutionException e) {
      assertThat(e)
          .isInstanceOf(ExecutionException.class)
          .hasCauseInstanceOf(ServiceException.class);
    }
    assertThat(result).isNull();
  }

}
