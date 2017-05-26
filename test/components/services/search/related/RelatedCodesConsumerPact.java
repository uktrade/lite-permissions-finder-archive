package components.services.search.related;

import static org.assertj.core.api.Assertions.assertThat;

import au.com.dius.pact.consumer.Pact;
import au.com.dius.pact.consumer.PactProviderRule;
import au.com.dius.pact.consumer.PactVerification;
import au.com.dius.pact.consumer.dsl.PactDslJsonBody;
import au.com.dius.pact.consumer.dsl.PactDslWithProvider;
import au.com.dius.pact.model.PactFragment;
import components.services.search.relatedcodes.RelatedCodesServiceClient;
import components.services.search.relatedcodes.RelatedCodesServiceResult;
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

public class RelatedCodesConsumerPact {

  private RelatedCodesServiceClient client;
  private WSClient ws;

  private final static String CONTROL_CODE = "ML1a";
  private final static String GROUP_TITLE = "Smooth-bore military weapons, components and accessories";
  private final static String DISPLAY_TEXT = "Rifles and combination guns, handguns, machine, sub-machine and volley guns";

  @Rule
  public PactProviderRule mockProvider = new PactProviderRule(PactConfig.SEARCH_MANAGEMENT_PROVIDER, this);

  @Before
  public void setUp() throws Exception {
    ws = WS.newClient(mockProvider.getConfig().getPort());
    client = new RelatedCodesServiceClient(new HttpExecutionContext(Runnable::run), ws, mockProvider.getConfig().url(), 10000);
  }

  @After
  public void tearDown() throws Exception {
    ws.close();
  }

  @Pact(provider = PactConfig.SEARCH_MANAGEMENT_PROVIDER, consumer = PactConfig.CONSUMER)
  public PactFragment controlCodeExistsWithResults(PactDslWithProvider builder) {
    PactDslJsonBody body = new PactDslJsonBody()
        .stringType("groupTitle", GROUP_TITLE)
        .eachLike("results", 3)
          .stringType("controlCode", CONTROL_CODE)
          .stringType("displayText", DISPLAY_TEXT)
          .closeObject()
        .closeArray()
        .asBody();

    Map<String, String> headers = new HashMap<>();
    headers.put("Content-Type", "application/json");

    return builder
        .given("related controls exist for given control code")
        .uponReceiving("a request for related controls")
          .path("/related-codes/" + CONTROL_CODE)
          .method("GET")
        .willRespondWith()
          .status(200)
          .headers(headers)
          .body(body)
        .toFragment();
  }

  @Pact(provider = PactConfig.SEARCH_MANAGEMENT_PROVIDER, consumer = PactConfig.CONSUMER)
  public PactFragment controlCodeExistsNoRelatedControls(PactDslWithProvider builder) {
    PactDslJsonBody body = new PactDslJsonBody()
        .stringType("groupTitle", GROUP_TITLE)
        .eachLike("results", 1)
          .stringType("controlCode", CONTROL_CODE)
          .stringType("displayText", DISPLAY_TEXT)
          .closeObject()
        .closeArray()
        .asBody();

    Map<String, String> headers = new HashMap<>();
    headers.put("Content-Type", "application/json");

    return builder
        .given("no related controls exist for given control code")
        .uponReceiving("a request for related controls")
          .path("/related-codes/" + CONTROL_CODE)
          .method("GET")
        .willRespondWith()
          .status(200)
          .headers(headers)
          .body(body)
        .toFragment();
  }

  @Pact(provider = PactConfig.SEARCH_MANAGEMENT_PROVIDER, consumer = PactConfig.CONSUMER)
  public PactFragment controlCodeDoesNotExist(PactDslWithProvider builder) {
    PactDslJsonBody body = new PactDslJsonBody()
        .stringType("code", "404")
        .stringType("message", "Control code not known: " + CONTROL_CODE)
        .asBody();

    Map<String, String> headers = new HashMap<>();
    headers.put("Content-Type", "application/json");

    return builder
        .given("control code does not exist")
        .uponReceiving("a request for related controls")
        .path("/related-codes/" + CONTROL_CODE)
          .method("GET")
        .willRespondWith()
        .status(404)
          .headers(headers)
          .body(body)
        .toFragment();
  }

  @Test
  @PactVerification(value = PactConfig.SEARCH_MANAGEMENT_PROVIDER, fragment = "controlCodeExistsWithResults")
  public void controlCodeExistsWithResultsTest() throws Exception {
    RelatedCodesServiceResult result;
    try {
      result = client.get(CONTROL_CODE).toCompletableFuture().get();
    }
    catch (InterruptedException | ExecutionException e) {
      throw new RuntimeException(e);
    }
    assertThat(result).isNotNull();
    assertThat(result.groupTitle).isEqualTo(GROUP_TITLE);
    assertThat(result.relatedCodes.size()).isEqualTo(3);
    assertThat(result.relatedCodes.get(0).getControlCode()).isEqualTo(CONTROL_CODE);
    assertThat(result.relatedCodes.get(0).getDisplayText()).isEqualTo(DISPLAY_TEXT);
  }

  @Test
  @PactVerification(value = PactConfig.SEARCH_MANAGEMENT_PROVIDER, fragment = "controlCodeExistsNoRelatedControls")
  public void controlCodeExistsNoRelatedControlsTest() throws Exception {
    RelatedCodesServiceResult result;
    try {
      result = client.get(CONTROL_CODE).toCompletableFuture().get();
    }
    catch (InterruptedException | ExecutionException e) {
      throw new RuntimeException(e);
    }
    assertThat(result).isNotNull();
    assertThat(result.groupTitle).isEqualTo(GROUP_TITLE);
    assertThat(result.relatedCodes.size()).isEqualTo(1);
    assertThat(result.relatedCodes.get(0).getControlCode()).isEqualTo(CONTROL_CODE);
    assertThat(result.relatedCodes.get(0).getDisplayText()).isEqualTo(DISPLAY_TEXT);
  }

  @Test
  @PactVerification(value = PactConfig.SEARCH_MANAGEMENT_PROVIDER, fragment = "controlCodeDoesNotExist")
  public void controlCodeDoesNotExistTest() throws Exception {
    RelatedCodesServiceResult result = null;
    try {
      result = client.get(CONTROL_CODE).toCompletableFuture().get();
    }
    catch (InterruptedException | ExecutionException e) {
      assertThat(e)
          .isInstanceOf(ExecutionException.class)
          .hasCauseInstanceOf(ServiceException.class);
    }
    assertThat(result).isNull();
  }
}
