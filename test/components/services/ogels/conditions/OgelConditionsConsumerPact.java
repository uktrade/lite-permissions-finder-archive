package components.services.ogels.conditions;

import static org.assertj.core.api.Assertions.assertThat;

import au.com.dius.pact.consumer.Pact;
import au.com.dius.pact.consumer.PactProviderRule;
import au.com.dius.pact.consumer.PactVerification;
import au.com.dius.pact.consumer.dsl.PactDslJsonBody;
import au.com.dius.pact.consumer.dsl.PactDslWithProvider;
import au.com.dius.pact.model.PactFragment;
import com.google.common.collect.ImmutableMap;
import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import pact.PactConfig;
import play.libs.concurrent.HttpExecutionContext;
import play.libs.ws.WS;
import play.libs.ws.WSClient;
import uk.gov.bis.lite.ogel.api.view.ControlCodeConditionFullView;

import java.util.Map;
import java.util.concurrent.ExecutionException;

public class OgelConditionsConsumerPact {
  private OgelConditionsServiceClient client;
  private WSClient ws;

  // service:password
  private static final Map<String, String> AUTH_HEADERS = ImmutableMap.of("Authorization", "Basic c2VydmljZTpwYXNzd29yZA==");
  private static final Map<String, String> CONTENT_TYPE_HEADERS = ImmutableMap.of("Content-Type", "application/json");

  private static final String OGEL_ID = "OGL1";
  private static final String CONTROL_CODE = "ML1a";
  private static final String CONTROL_CODE_FRIENDLY_DESC = "Rifles and combination guns, handguns, machine, sub-machine and volley guns";
  private static final String CONDITION_DESCRIPTION = "<p>Fully automatic weapons</p>";

  @Rule
  public PactProviderRule mockProvider = new PactProviderRule(PactConfig.OGEL_SERVICE_PROVIDER, this);

  @Before
  public void setUp() throws Exception {
    ws = WS.newClient(mockProvider.getConfig().getPort());
    client = new OgelConditionsServiceClient(new HttpExecutionContext(Runnable::run),
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
  public PactFragment ogelConditionsExist(PactDslWithProvider builder) {
    PactDslJsonBody body = new PactDslJsonBody()
        .stringType("ogelId", OGEL_ID)
        .stringType("controlCode", CONTROL_CODE)
        .stringType("conditionDescription", CONDITION_DESCRIPTION)
        .nullValue("conditionDescriptionControlCodes")
        .booleanValue("itemsAllowed", false)
        .asBody();

    return builder
        .given("conditions exist for given ogel and control code")
        .uponReceiving("a request for ogel conditions")
          .headers(AUTH_HEADERS)
          .path("/control-code-conditions/" + OGEL_ID + "/" + CONTROL_CODE)
          .method("GET")
        .willRespondWith()
          .status(200)
          .headers(CONTENT_TYPE_HEADERS)
          .body(body)
        .toFragment();
  }

  @Pact(provider = PactConfig.OGEL_SERVICE_PROVIDER, consumer = PactConfig.CONSUMER)
  public PactFragment ogelConditionsDoNotExist(PactDslWithProvider builder) {
    return builder
        .given("no conditions exist for given ogel and control code")
        .uponReceiving("a request for ogel conditions")
          .headers(AUTH_HEADERS)
          .path("/control-code-conditions/" + OGEL_ID + "/" + CONTROL_CODE)
          .method("GET")
        .willRespondWith()
          .status(204)
        .toFragment();
  }

  @Pact(provider = PactConfig.OGEL_SERVICE_PROVIDER, consumer = PactConfig.CONSUMER)
  public PactFragment ogelConditionsExistWithControlCodes(PactDslWithProvider builder) {
    PactDslJsonBody body = new PactDslJsonBody()
        .stringType("ogelId", OGEL_ID)
        .stringType("controlCode", CONTROL_CODE)
        .stringType("conditionDescription", CONDITION_DESCRIPTION)
        .nullValue("conditionDescriptionControlCodes")
        .booleanValue("itemsAllowed", false)
        .object("conditionDescriptionControlCodes")
        .array("controlCodes")
          .object()
            .stringType("id")
            .stringType("controlCode")
            .stringType("friendlyDescription", CONTROL_CODE_FRIENDLY_DESC)
          .closeObject()
        .closeArray()
        .array("missingControlCodes")
        .closeArray()
        .closeObject()
        .asBody();

    return builder
        .given("conditions exist with related control codes for given ogel and control code")
        .uponReceiving("a request for ogel conditions")
          .headers(AUTH_HEADERS)
          .path("/control-code-conditions/" + OGEL_ID + "/" + CONTROL_CODE)
          .method("GET")
        .willRespondWith()
          .status(200)
          .headers(CONTENT_TYPE_HEADERS)
          .body(body)
        .toFragment();
  }

  @Pact(provider = PactConfig.OGEL_SERVICE_PROVIDER, consumer = PactConfig.CONSUMER)
  public PactFragment ogelConditionsExistWithMissingControlCodes(PactDslWithProvider builder) {
    PactDslJsonBody body = new PactDslJsonBody()
        .stringType("ogelId", OGEL_ID)
        .stringType("controlCode", CONTROL_CODE)
        .stringType("conditionDescription", CONDITION_DESCRIPTION)
        .nullValue("conditionDescriptionControlCodes")
        .booleanValue("itemsAllowed", false)
        .object("conditionDescriptionControlCodes")
        .array("controlCodes")
        .closeArray()
        .array("missingControlCodes")
          .stringType(CONTROL_CODE)
        .closeArray()
        .closeObject()
        .asBody();

    return builder
        .given("conditions exist with missing related control codes for given ogel and control code")
        .uponReceiving("a request for ogel conditions")
          .headers(AUTH_HEADERS)
          .path("/control-code-conditions/" + OGEL_ID + "/" + CONTROL_CODE)
          .method("GET")
        .willRespondWith()
          .status(206)
          .headers(CONTENT_TYPE_HEADERS)
          .body(body)
        .toFragment();
  }

  @Test
  @PactVerification(value = PactConfig.OGEL_SERVICE_PROVIDER, fragment = "ogelConditionsExist")
  public void ogelConditionsExistTest() throws Exception {
    OgelConditionsServiceResult result;
    try {
      result = client.get(OGEL_ID, CONTROL_CODE).toCompletableFuture().get();
    } catch (InterruptedException | ExecutionException e) {
      throw new RuntimeException(e);
    }
    assertThat(result).isNotNull();
    assertThat(result.isEmpty).isFalse();
    assertThat(result.ogelID).isEqualTo(OGEL_ID);
    assertThat(result.controlCode).isEqualTo(CONTROL_CODE);
    assertThat(result.conditionDescriptionControlCodes.isPresent()).isFalse();
    assertThat(result.conditionDescription).isEqualTo(CONDITION_DESCRIPTION);
    assertThat(result.itemsAllowed).isFalse();
  }

  @Test
  @PactVerification(value = PactConfig.OGEL_SERVICE_PROVIDER, fragment = "ogelConditionsDoNotExist")
  public void ogelConditionsDoNotExistTest() throws Exception {
    OgelConditionsServiceResult result;
    try {
      result = client.get(OGEL_ID, CONTROL_CODE).toCompletableFuture().get();
    } catch (InterruptedException | ExecutionException e) {
      throw new RuntimeException(e);
    }
    assertThat(result).isNotNull();
    assertThat(result.isEmpty).isTrue();
  }

  @Test
  @PactVerification(value = PactConfig.OGEL_SERVICE_PROVIDER, fragment = "ogelConditionsExistWithControlCodes")
  public void ogelConditionsExistWithControlCodesTest() throws Exception {
    OgelConditionsServiceResult result;
    try {
      result = client.get(OGEL_ID, CONTROL_CODE).toCompletableFuture().get();
    } catch (InterruptedException | ExecutionException e) {
      throw new RuntimeException(e);
    }
    assertThat(result).isNotNull();
    assertThat(result.isEmpty).isFalse();
    assertThat(result.ogelID).isEqualTo(OGEL_ID);
    assertThat(result.controlCode).isEqualTo(CONTROL_CODE);
    assertThat(result.conditionDescriptionControlCodes.isPresent()).isTrue();
    ControlCodeConditionFullView.ConditionDescriptionControlCodes desc = result.conditionDescriptionControlCodes.get();
    assertThat(desc.getControlCodes().isEmpty()).isFalse();
    assertThat(desc.getControlCodes().get(0).getFriendlyDescription()).isEqualTo(CONTROL_CODE_FRIENDLY_DESC);
    assertThat(desc.getMissingControlCodes().isEmpty()).isTrue();
    assertThat(result.conditionDescription).isEqualTo(CONDITION_DESCRIPTION);
    assertThat(result.itemsAllowed).isFalse();
  }

  @Test
  @PactVerification(value = PactConfig.OGEL_SERVICE_PROVIDER, fragment = "ogelConditionsExistWithMissingControlCodes")
  public void ogelConditionsExistWithMissingControlCodesTest() throws Exception {
    OgelConditionsServiceResult result;
    try {
      result = client.get(OGEL_ID, CONTROL_CODE).toCompletableFuture().get();
    } catch (InterruptedException | ExecutionException e) {
      throw new RuntimeException(e);
    }
    assertThat(result).isNotNull();
    assertThat(result.isEmpty).isFalse();
    assertThat(result.ogelID).isEqualTo(OGEL_ID);
    assertThat(result.controlCode).isEqualTo(CONTROL_CODE);
    assertThat(result.conditionDescriptionControlCodes.isPresent()).isTrue();
    ControlCodeConditionFullView.ConditionDescriptionControlCodes desc = result.conditionDescriptionControlCodes.get();
    assertThat(desc.getControlCodes().isEmpty()).isTrue();
    assertThat(desc.getMissingControlCodes().isEmpty()).isFalse();
    assertThat(result.conditionDescription).isEqualTo(CONDITION_DESCRIPTION);
    assertThat(result.itemsAllowed).isFalse();
  }
}
