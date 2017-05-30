package components.services.search.search;

import static org.assertj.core.api.Assertions.assertThat;

import au.com.dius.pact.consumer.Pact;
import au.com.dius.pact.consumer.PactProviderRule;
import au.com.dius.pact.consumer.PactVerification;
import au.com.dius.pact.consumer.dsl.PactDslJsonBody;
import au.com.dius.pact.consumer.dsl.PactDslWithProvider;
import au.com.dius.pact.model.PactFragment;
import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import pact.PactConfig;
import play.libs.concurrent.HttpExecutionContext;
import play.libs.ws.WS;
import play.libs.ws.WSClient;
import uk.gov.bis.lite.searchmanagement.api.view.SearchResultView;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ExecutionException;

public class SearchConsumerPact {
  private SearchServiceClient client;
  private WSClient ws;

  private static final String CONTROL_CODE = "ML1a";
  private static final String DISPLAY_TEXT = "Rifles and combination guns, handguns, machine, sub-machine and volley guns";
  private static final String RELATED_CODE = "ML3a";

  @Rule
  public PactProviderRule mockProvider = new PactProviderRule(PactConfig.SEARCH_MANAGEMENT_PROVIDER, this);

  @Before
  public void setUp() throws Exception {
    ws = WS.newClient(mockProvider.getConfig().getPort());
    client = new SearchServiceClient(new HttpExecutionContext(Runnable::run), ws, mockProvider.getConfig().url(), 10000);
  }

  @After
  public void tearDown() throws Exception {
    ws.close();
  }

  @Pact(provider = PactConfig.SEARCH_MANAGEMENT_PROVIDER, consumer = PactConfig.CONSUMER)
  public PactFragment noResultsFound(PactDslWithProvider builder) {
    PactDslJsonBody body = new PactDslJsonBody()
          .array("results")
          .closeArray()
        .asBody();

    Map<String, String> headers = new HashMap<>();
    headers.put("Content-Type", "application/json");

    return builder
        .given("no results exist for the search terms")
        .uponReceiving("a search request for the given terms")
          .path("/search")
          .query("term=NOT_FOUND&goodsType=physical")
          .method("GET")
        .willRespondWith()
          .status(200)
          .headers(headers)
          .body(body)
        .toFragment();
  }

  @Pact(provider = PactConfig.SEARCH_MANAGEMENT_PROVIDER, consumer = PactConfig.CONSUMER)
  public PactFragment aResultIsFound(PactDslWithProvider builder) {
    PactDslJsonBody body = new PactDslJsonBody()
        .eachLike("results", 1)
          .stringType("controlCode", CONTROL_CODE)
          .stringType("displayText", DISPLAY_TEXT)
          .array("relatedCodes").closeArray()
          .closeObject()
        .closeArray()
        .asBody();

    Map<String, String> headers = new HashMap<>();
    headers.put("Content-Type", "application/json");

    return builder
        .given("a result exists for the search terms")
        .uponReceiving("a search request for the given terms")
          .path("/search")
          .query("term=FOUND&goodsType=physical")
          .method("GET")
        .willRespondWith()
          .status(200)
          .headers(headers)
          .body(body)
        .toFragment();
  }

  @Pact(provider = PactConfig.SEARCH_MANAGEMENT_PROVIDER, consumer = PactConfig.CONSUMER)
  public PactFragment aResultIsFoundWithRelatedCodes(PactDslWithProvider builder) {
    PactDslJsonBody body = new PactDslJsonBody()
        .eachLike("results", 1)
          .stringType("controlCode", CONTROL_CODE)
          .stringType("displayText", DISPLAY_TEXT)
          .array("relatedCodes")
            .string(RELATED_CODE)
            .closeArray()
          .closeObject()
        .closeArray()
        .asBody();

    Map<String, String> headers = new HashMap<>();
    headers.put("Content-Type", "application/json");

    return builder
        .given("a result with related codes exists for the search terms")
        .uponReceiving("a search request for the given terms")
          .path("/search")
          .query("term=FOUND&goodsType=physical")
          .method("GET")
        .willRespondWith()
          .status(200)
          .headers(headers)
          .body(body)
        .toFragment();
  }

  @Pact(provider = PactConfig.SEARCH_MANAGEMENT_PROVIDER, consumer = PactConfig.CONSUMER)
  public PactFragment multipleResultsAreFound(PactDslWithProvider builder) {
    PactDslJsonBody body = new PactDslJsonBody()
        .eachLike("results", 3)
          .stringType("controlCode", CONTROL_CODE)
          .stringType("displayText", DISPLAY_TEXT)
          .array("relatedCodes").closeArray()
          .closeObject()
        .closeArray()
        .asBody();

    Map<String, String> headers = new HashMap<>();
    headers.put("Content-Type", "application/json");

    return builder
        .given("multiple results exists for the search terms")
        .uponReceiving("a search request for the given terms")
          .path("/search")
          .query("term=FOUND&goodsType=physical")
          .method("GET")
        .willRespondWith()
          .status(200)
          .headers(headers)
          .body(body)
        .toFragment();
  }

  @Test
  @PactVerification(value = PactConfig.SEARCH_MANAGEMENT_PROVIDER, fragment = "noResultsFound")
  public void noResultsFoundTest() throws Exception {
    SearchServiceResult result;
    try {
      result = client.get("NOT_FOUND").toCompletableFuture().get();
    }
    catch (InterruptedException | ExecutionException e) {
      throw new RuntimeException(e);
    }
    assertThat(result).isNotNull();
    assertThat(result.results.isEmpty()).isTrue();
  }

  @Test
  @PactVerification(value = PactConfig.SEARCH_MANAGEMENT_PROVIDER, fragment = "aResultIsFound")
  public void aResultIsFoundTest() throws Exception {
    SearchServiceResult result;
    try {
      result = client.get("FOUND").toCompletableFuture().get();
    }
    catch (InterruptedException | ExecutionException e) {
      throw new RuntimeException(e);
    }
    assertThat(result).isNotNull();
    assertThat(result.results.size()).isEqualTo(1);
    SearchResultView r = result.results.get(0);
    assertThat(r.getControlCode()).isEqualTo(CONTROL_CODE);
    assertThat(r.getDisplayText()).isEqualTo(DISPLAY_TEXT);
    assertThat(r.getRelatedCodes().isEmpty()).isTrue();
  }

  @Test
  @PactVerification(value = PactConfig.SEARCH_MANAGEMENT_PROVIDER, fragment = "aResultIsFoundWithRelatedCodes")
  public void aResultIsFoundWithRelatedCodesTest() throws Exception {
    SearchServiceResult result;
    try {
      result = client.get("FOUND").toCompletableFuture().get();
    }
    catch (InterruptedException | ExecutionException e) {
      throw new RuntimeException(e);
    }
    assertThat(result).isNotNull();
    assertThat(result.results.size()).isEqualTo(1);
    SearchResultView r = result.results.get(0);
    assertThat(r.getControlCode()).isEqualTo(CONTROL_CODE);
    assertThat(r.getDisplayText()).isEqualTo(DISPLAY_TEXT);
    assertThat(r.getRelatedCodes().size()).isEqualTo(1);
    assertThat(r.getRelatedCodes().get(0)).isEqualTo(RELATED_CODE);
  }

  @Test
  @PactVerification(value = PactConfig.SEARCH_MANAGEMENT_PROVIDER, fragment = "multipleResultsAreFound")
  public void multipleResultsAreFoundTest() throws Exception {
    SearchServiceResult result;
    try {
      result = client.get("FOUND").toCompletableFuture().get();
    }
    catch (InterruptedException | ExecutionException e) {
      throw new RuntimeException(e);
    }
    assertThat(result).isNotNull();
    assertThat(result.results.size()).isEqualTo(3);
    SearchResultView r = result.results.get(0);
    assertThat(r.getControlCode()).isEqualTo(CONTROL_CODE);
    assertThat(r.getDisplayText()).isEqualTo(DISPLAY_TEXT);
    assertThat(r.getRelatedCodes().isEmpty()).isTrue();
  }

}
