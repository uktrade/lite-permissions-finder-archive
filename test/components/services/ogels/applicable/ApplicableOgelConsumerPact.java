package components.services.ogels.applicable;

import static org.assertj.core.api.Assertions.assertThat;

import au.com.dius.pact.consumer.Pact;
import au.com.dius.pact.consumer.PactProviderRuleMk2;
import au.com.dius.pact.consumer.PactVerification;
import au.com.dius.pact.consumer.dsl.PactDslJsonArray;
import au.com.dius.pact.consumer.dsl.PactDslJsonBody;
import au.com.dius.pact.consumer.dsl.PactDslWithProvider;
import au.com.dius.pact.model.RequestResponsePact;
import com.google.common.collect.ImmutableMap;
import exceptions.ServiceException;
import models.OgelActivityType;
import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import pact.PactConfig;
import play.libs.concurrent.HttpExecutionContext;
import play.libs.ws.WSClient;
import play.test.WSTestClient;
import uk.gov.bis.lite.ogel.api.view.ApplicableOgelView;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutionException;

public class ApplicableOgelConsumerPact {
  private ApplicableOgelServiceClient client;
  private WSClient ws;

  // service:password
  private static final Map<String, String> AUTH_HEADERS = ImmutableMap.of("Authorization", "Basic c2VydmljZTpwYXNzd29yZA==");
  private static final Map<String, String> CONTENT_TYPE_HEADERS = ImmutableMap.of("Content-Type", "application/json");

  private static final String OGEL_ID = "OGL1";
  private static final String OGEL_NAME = "name";
  private static final String OGEL_USAGE_SUMMARY = "can";
  private static final String CONTROL_CODE = "ML1a";
  private static final String ACTIVITY_TYPE_INVALID = "INVALID";
  private static final String SOURCE_COUNTRY = "CTRY0";
  private static final String DESTINATION_COUNTRY = "CTRY3";

  @Rule
  public PactProviderRuleMk2 mockProvider = new PactProviderRuleMk2(PactConfig.OGEL_SERVICE_PROVIDER, this);

  @Before
  public void setUp() throws Exception {
    ws = WSTestClient.newClient(mockProvider.getPort());
    client = new ApplicableOgelServiceClient(new HttpExecutionContext(Runnable::run),
        ws,
        mockProvider.getUrl(),
        10000,
        "service:password");
  }

  @After
  public void tearDown() throws Exception {
    ws.close();
  }

  @Pact(provider = PactConfig.OGEL_SERVICE_PROVIDER, consumer = PactConfig.CONSUMER)
  public RequestResponsePact applicableOgelsExist(PactDslWithProvider builder) {
    PactDslJsonArray body = PactDslJsonArray.arrayMinLike(1, 3)
        .stringType("id", OGEL_ID)
        .stringType("name", OGEL_NAME)
        .array("usageSummary")
          .string(OGEL_USAGE_SUMMARY)
          .closeArray()
        .closeObject()
        .asArray();

    return builder
        .given("applicable ogels exist for given parameters")
        .uponReceiving("a request for applicable ogels")
          .headers(AUTH_HEADERS)
          .path("/applicable-ogels")
          .query("controlCode=" + CONTROL_CODE + "&sourceCountry=" + SOURCE_COUNTRY + "&activityType=" + OgelActivityType.DU_ANY.value() + "&destinationCountry=" + DESTINATION_COUNTRY)
          .method("GET")
        .willRespondWith()
          .status(200)
          .headers(CONTENT_TYPE_HEADERS)
          .body(body)
        .toPact();
  }

  @Pact(provider = PactConfig.OGEL_SERVICE_PROVIDER, consumer = PactConfig.CONSUMER)
  public RequestResponsePact applicableOgelsDoNotExist(PactDslWithProvider builder) {
    PactDslJsonArray body = new PactDslJsonArray();

    return builder
        .given("no applicable ogels exist for given parameters")
        .uponReceiving("a request for applicable ogels")
          .headers(AUTH_HEADERS)
          .path("/applicable-ogels")
          .query("controlCode=" + CONTROL_CODE + "&sourceCountry=" + SOURCE_COUNTRY + "&activityType=" + OgelActivityType.DU_ANY.value() + "&destinationCountry=" + DESTINATION_COUNTRY)
          .method("GET")
        .willRespondWith()
          .status(200)
          .headers(CONTENT_TYPE_HEADERS)
          .body(body)
        .toPact();
  }

  @Pact(provider = PactConfig.OGEL_SERVICE_PROVIDER, consumer = PactConfig.CONSUMER)
  public RequestResponsePact invalidActivityType(PactDslWithProvider builder) {
    PactDslJsonBody body = new PactDslJsonBody()
        .integerType("code", 400)
        .stringType("message", "Invalid activity type: " + ACTIVITY_TYPE_INVALID)
        .asBody();

    return builder
        .given("activity type does not exist")
        .uponReceiving("a request for applicable ogels")
          .headers(AUTH_HEADERS)
          .path("/applicable-ogels")
          .query("controlCode=" + CONTROL_CODE + "&sourceCountry=" + SOURCE_COUNTRY + "&activityType=" + ACTIVITY_TYPE_INVALID + "&destinationCountry=" + DESTINATION_COUNTRY)
          .method("GET")
        .willRespondWith()
          .status(400)
          .headers(CONTENT_TYPE_HEADERS)
          .body(body)
        .toPact();
  }

  @Pact(provider = PactConfig.OGEL_SERVICE_PROVIDER, consumer = PactConfig.CONSUMER)
  public RequestResponsePact applicableOgelsExistForMultipleActivityTypes(PactDslWithProvider builder) {
    PactDslJsonArray body = PactDslJsonArray.arrayMinLike(1, 3)
        .stringType("id", OGEL_ID)
        .stringType("name", OGEL_NAME)
        .array("usageSummary")
          .string(OGEL_USAGE_SUMMARY)
          .closeArray()
        .closeObject()
        .asArray();

    return builder
        .given("applicable ogels exist for multiple activity types")
        .uponReceiving("a request for applicable ogels")
          .headers(AUTH_HEADERS)
          .path("/applicable-ogels")
          .query("controlCode=" + CONTROL_CODE + "&sourceCountry=" + SOURCE_COUNTRY +
            "&activityType=" + OgelActivityType.MIL_GOV.value() + "&activityType=" + OgelActivityType.MIL_ANY.value() +
            "&destinationCountry=" + DESTINATION_COUNTRY)
          .method("GET")
        .willRespondWith()
          .status(200)
          .headers(CONTENT_TYPE_HEADERS)
          .body(body)
        .toPact();
  }

  @Test
  @PactVerification(value = PactConfig.OGEL_SERVICE_PROVIDER, fragment = "applicableOgelsExist")
  public void applicableOgelsExistTest() throws Exception {
    List<ApplicableOgelView> result;
    List<String> activityTypes = Arrays.asList(OgelActivityType.DU_ANY.value());
    List<String> destinationCountries = Arrays.asList(DESTINATION_COUNTRY);
    try {
      result = client.get(CONTROL_CODE, SOURCE_COUNTRY, destinationCountries, activityTypes)
          .toCompletableFuture().get();
    } catch (InterruptedException | ExecutionException e) {
      throw new RuntimeException(e);
    }
    assertThat(result).isNotNull();
    assertThat(result.size()).isEqualTo(3);
    ApplicableOgelView ogel = result.get(0);
    assertThat(ogel.getId()).isEqualTo(OGEL_ID);
    assertThat(ogel.getName()).isEqualTo(OGEL_NAME);
    assertThat(ogel.getUsageSummary().size()).isEqualTo(1);
    assertThat(ogel.getUsageSummary().get(0)).isEqualTo(OGEL_USAGE_SUMMARY);
  }

  @Test
  @PactVerification(value = PactConfig.OGEL_SERVICE_PROVIDER, fragment = "applicableOgelsDoNotExist")
  public void applicableOgelsDoNotExistTest() throws Exception {
    List<ApplicableOgelView> result;
    List<String> activityTypes = Arrays.asList(OgelActivityType.DU_ANY.value());
    List<String> destinationCountries = Arrays.asList(DESTINATION_COUNTRY);
    try {
      result = client.get(CONTROL_CODE, SOURCE_COUNTRY, destinationCountries, activityTypes)
          .toCompletableFuture().get();
    } catch (InterruptedException | ExecutionException e) {
      throw new RuntimeException(e);
    }
    assertThat(result).isNotNull();
    assertThat(result.isEmpty()).isTrue();
  }

  @Test
  @PactVerification(value = PactConfig.OGEL_SERVICE_PROVIDER, fragment = "invalidActivityType")
  public void invalidActivityTypeTest() throws Exception {
    List<ApplicableOgelView> result = null;
    List<String> activityTypes = Arrays.asList(ACTIVITY_TYPE_INVALID);
    List<String> destinationCountries = Arrays.asList(DESTINATION_COUNTRY);
    try {
      result = client.get(CONTROL_CODE, SOURCE_COUNTRY, destinationCountries, activityTypes)
          .toCompletableFuture().get();
    } catch (InterruptedException | ExecutionException e) {
      assertThat(e)
          .isInstanceOf(ExecutionException.class)
          .hasCauseInstanceOf(ServiceException.class);
    }
    assertThat(result).isNull();
  }

  @Test
  @PactVerification(value = PactConfig.OGEL_SERVICE_PROVIDER, fragment = "applicableOgelsExistForMultipleActivityTypes")
  public void applicableOgelsExistForMultipleActivityTypesTest() throws Exception {
    List<ApplicableOgelView> result;
    List<String> activityTypes = Arrays.asList(OgelActivityType.MIL_GOV.value(), OgelActivityType.MIL_ANY.value());
    List<String> destinationCountries = Arrays.asList(DESTINATION_COUNTRY);
    try {
      result = client.get(CONTROL_CODE, SOURCE_COUNTRY, destinationCountries, activityTypes)
          .toCompletableFuture().get();
    } catch (InterruptedException | ExecutionException e) {
      throw new RuntimeException(e);
    }
    assertThat(result).isNotNull();
    assertThat(result.size()).isEqualTo(3);
    ApplicableOgelView ogel = result.get(0);
    assertThat(ogel.getId()).isEqualTo(OGEL_ID);
    assertThat(ogel.getName()).isEqualTo(OGEL_NAME);
    assertThat(ogel.getUsageSummary().size()).isEqualTo(1);
    assertThat(ogel.getUsageSummary().get(0)).isEqualTo(OGEL_USAGE_SUMMARY);
  }
}
