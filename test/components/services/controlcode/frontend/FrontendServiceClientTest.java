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
import play.libs.concurrent.HttpExecutionContext;
import play.libs.ws.WS;
import play.libs.ws.WSClient;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ExecutionException;

public class FrontendServiceClientTest {
  private FrontendServiceClient client;
  private WSClient ws;

  private static final String MINIMUM = "MINIMUM";
  private static final String MAXIMUM = "MAXIMUM";
  private static final String MISSING = "MISSING";

  @Rule
  public PactProviderRule mockProvider = new PactProviderRule("lite-control-code-service", this);

  @Pact(provider = "lite-control-code-service", consumer="lite-permissions-finder")
  public PactFragment createFragment(PactDslWithProvider builder) {

    PactDslJsonBody minimal = new PactDslJsonBody()
        .object("controlCodeData")
          .stringType("controlCode", MINIMUM)
          .stringType("friendlyDescription")
          .nullValue("title")
          .stringValue("technicalNotes", "")
          .stringType("alias")
          .nullValue("additionalSpecifications")
          .array("decontrols").closeArray()
        .closeObject()
        .array("lineage").closeArray()
        .asBody();

    PactDslJsonBody maximal = new PactDslJsonBody()
        .object("controlCodeData")
          .stringType("controlCode", MAXIMUM)
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

    PactDslJsonBody missing = new PactDslJsonBody()
        .integerType("code", 404)
        .stringType("message")
        .asBody();

    Map<String, String> headers = new HashMap<>();
    headers.put("Content-Type", "application/json");

    return builder
        .uponReceiving("Minimal existing frontend control code request")
          .path("/frontend-control-codes/" + MINIMUM)
          .method("GET")
          .willRespondWith()
            .status(200)
            .headers(headers)
            .body(minimal)
        .uponReceiving("Maximal existing frontend control code request")
          .path("/frontend-control-codes/" + MAXIMUM)
          .method("GET")
          .willRespondWith()
            .status(200)
            .headers(headers)
            .body(maximal)
        .uponReceiving("Missing frontend control code request")
          .path("/frontend-control-codes/" + MISSING)
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
  @PactVerification("lite-control-code-service")
  public void testControlCodeServicePact() throws Exception {
    // MINIMUM
    FrontendServiceResult frontendServiceResult;
    try {
      frontendServiceResult = client.get(MINIMUM).toCompletableFuture().get();
    }
    catch (InterruptedException | ExecutionException e) {
      throw new RuntimeException(e);
    }
    assertThat(frontendServiceResult).isNotNull();
    String controlCode = frontendServiceResult.getFrontendControlCode().getControlCodeData().getControlCode();
    assertThat(controlCode).isEqualTo(MINIMUM);
    assertThat(frontendServiceResult.canShowDecontrols()).isFalse();
    assertThat(frontendServiceResult.canShowAdditionalSpecifications()).isFalse();
    assertThat(frontendServiceResult.canShowTechnicalNotes()).isFalse();

    // MAX
    try {
      frontendServiceResult = client.get(MAXIMUM).toCompletableFuture().get();
    }
    catch (InterruptedException | ExecutionException e) {
      throw new RuntimeException(e);
    }
    assertThat(frontendServiceResult).isNotNull();
    controlCode = frontendServiceResult.getFrontendControlCode().getControlCodeData().getControlCode();
    assertThat(controlCode).isEqualTo(MAXIMUM);
    assertThat(frontendServiceResult.canShowDecontrols()).isTrue();
    assertThat(frontendServiceResult.canShowAdditionalSpecifications()).isTrue();
    assertThat(frontendServiceResult.canShowTechnicalNotes()).isTrue();

    // MISSING
    frontendServiceResult = null;
    try {
      frontendServiceResult = client.get(MISSING).toCompletableFuture().get();
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
