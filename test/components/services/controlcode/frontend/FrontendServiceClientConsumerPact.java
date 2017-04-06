package components.services.controlcode.frontend;

import static org.assertj.core.api.Assertions.assertThat;

import au.com.dius.pact.consumer.Pact;
import au.com.dius.pact.consumer.PactProviderRule;
import au.com.dius.pact.consumer.PactVerification;
import au.com.dius.pact.consumer.dsl.PactDslJsonBody;
import au.com.dius.pact.consumer.dsl.PactDslWithProvider;
import au.com.dius.pact.model.PactFragment;
import exceptions.ServiceException;
import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import pact.PactConfig;
import play.libs.concurrent.HttpExecutionContext;
import play.libs.ws.WS;
import play.libs.ws.WSClient;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ExecutionException;

public class FrontendServiceClientConsumerPact {
  private FrontendServiceClient client;
  private WSClient ws;

  private static final String CONTROL_CODE = "ControlCode";

  @Rule
  public PactProviderRule mockProvider = new PactProviderRule(PactConfig.CONTROL_CODE_SERVICE_PROVIDER, this);

  @Pact(provider = PactConfig.CONTROL_CODE_SERVICE_PROVIDER, consumer = PactConfig.CONSUMER)
  public PactFragment existingControlCode(PactDslWithProvider builder) {
    PactDslJsonBody existing = new PactDslJsonBody()
        .object("controlCodeData")
          .stringType("controlCode", CONTROL_CODE)
          .stringType("friendlyDescription")
          .stringType("title")
          .stringType("technicalNotes")
          .stringType("alias")
          .object("additionalSpecifications")
            .stringType("clauseText")
            .eachLike("specificationText", 3)
              .stringType("text")
              .stringType("linkedControlCode")
              .closeObject()
            .closeArray()
          .closeObject()
          .eachLike("decontrols", 3)
            .stringType("originControlCode")
            .stringType("text")
            .closeObject()
          .closeArray()
        .closeObject()
        .eachLike("lineage", 3)
          .stringType("controlCode")
          .stringType("alias")
          .stringType("friendlyDescription")
          .closeObject()
        .closeArray()
        .asBody();

    Map<String, String> headers = new HashMap<>();
    headers.put("Content-Type", "application/json");

    return builder
        .given("the code ControlCode does exists")
        .uponReceiving("a request for code ControlCode")
          .path("/frontend-control-codes/" + CONTROL_CODE)
          .method("GET")
          .willRespondWith()
            .status(200)
            .headers(headers)
            .body(existing)
        .toFragment();
  }

  @Pact(provider = PactConfig.CONTROL_CODE_SERVICE_PROVIDER, consumer = PactConfig.CONSUMER)
  public PactFragment missingControlCode(PactDslWithProvider builder) {
    PactDslJsonBody missing = new PactDslJsonBody()
        .integerType("code", 404)
        .stringType("message")
        .asBody();

    Map<String, String> headers = new HashMap<>();
    headers.put("Content-Type", "application/json");

    return builder
        .given("the code ControlCode does not exist")
        .uponReceiving("a request for code ControlCode")
        .path("/frontend-control-codes/" + CONTROL_CODE)
        .method("GET")
        .willRespondWith()
            .status(404)
            .headers(headers)
            .body(missing)
        .toFragment();
  }

  @Before
  public void setUp() throws Exception {
    ws = WS.newClient(mockProvider.getConfig().getPort());
    client = new FrontendServiceClient(new HttpExecutionContext(Runnable::run), ws, mockProvider.getConfig().url(), 10000);
  }

  @Test
  @PactVerification(value = PactConfig.CONTROL_CODE_SERVICE_PROVIDER, fragment = "existingControlCode")
  public void testExistingControlCode() throws Exception {
    FrontendServiceResult frontendServiceResult;
    try {
      frontendServiceResult = client.get(CONTROL_CODE).toCompletableFuture().get();
    }
    catch (InterruptedException | ExecutionException e) {
      throw new RuntimeException(e);
    }
    assertThat(frontendServiceResult).isNotNull();
    String controlCode = frontendServiceResult.getFrontendControlCode().getControlCodeData().getControlCode();
    assertThat(controlCode).isEqualTo(CONTROL_CODE);
    assertThat(frontendServiceResult.canShowDecontrols()).isTrue();
    assertThat(frontendServiceResult.canShowAdditionalSpecifications()).isTrue();
    assertThat(frontendServiceResult.canShowTechnicalNotes()).isTrue();
  }

  @Test
  @PactVerification(value = PactConfig.CONTROL_CODE_SERVICE_PROVIDER, fragment = "missingControlCode")
  public void testMissingControlCode() {
    FrontendServiceResult frontendServiceResult = null;
    try {
      frontendServiceResult = client.get(CONTROL_CODE).toCompletableFuture().get();
    }
    catch (Exception e) {
      assertThat(e)
          .isInstanceOf(ExecutionException.class)
          .hasCauseInstanceOf(ServiceException.class);
    }
    assertThat(frontendServiceResult).isNull();
  }

  @After
  public void tearDown() throws Exception {
    ws.close();
  }
}
