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

  private final static String PROVIDER = "lite-control-code-service#frontend-control-codes";
  private final static String CONSUMER = "lite-permissions-finder";

  private static final String EXISTING = "EXISTING";
  private static final String MISSING = "MISSING";

  @Rule
  public PactProviderRule mockProvider = new PactProviderRule(PROVIDER, this);

  @Pact(provider = PROVIDER, consumer = CONSUMER)
  public PactFragment createFragment(PactDslWithProvider builder) {
    PactDslJsonBody existing = new PactDslJsonBody()
        .object("controlCodeData")
          .stringType("controlCode", EXISTING)
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
        .uponReceiving("Existing frontend control code request")
          .path("/frontend-control-codes/" + EXISTING)
          .method("GET")
          .willRespondWith()
            .status(200)
            .headers(headers)
            .body(existing)
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
  @PactVerification(PROVIDER)
  public void testControlCodeServicePact() throws Exception {
    testExistingControlCode();
    testMissingControlCode();
  }

  private void testExistingControlCode() {
    FrontendServiceResult frontendServiceResult;
    try {
      frontendServiceResult = client.get(EXISTING).toCompletableFuture().get();
    }
    catch (InterruptedException | ExecutionException e) {
      throw new RuntimeException(e);
    }
    assertThat(frontendServiceResult).isNotNull();
    String controlCode = frontendServiceResult.getFrontendControlCode().getControlCodeData().getControlCode();
    assertThat(controlCode).isEqualTo(EXISTING);
    assertThat(frontendServiceResult.canShowDecontrols()).isTrue();
    assertThat(frontendServiceResult.canShowAdditionalSpecifications()).isTrue();
    assertThat(frontendServiceResult.canShowTechnicalNotes()).isTrue();
  }

  private void testMissingControlCode() {
    FrontendServiceResult frontendServiceResult = null;
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
