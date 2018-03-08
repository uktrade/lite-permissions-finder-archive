package components.services.controlcode.related;

import static org.assertj.core.api.Assertions.assertThat;

import au.com.dius.pact.consumer.Pact;
import au.com.dius.pact.consumer.PactProviderRuleMk2;
import au.com.dius.pact.consumer.PactVerification;
import au.com.dius.pact.consumer.dsl.PactDslJsonArray;
import au.com.dius.pact.consumer.dsl.PactDslWithProvider;
import au.com.dius.pact.model.RequestResponsePact;
import com.google.common.collect.ImmutableMap;
import models.GoodsType;
import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import pact.PactConfig;
import play.libs.concurrent.HttpExecutionContext;
import play.libs.ws.WSClient;
import play.test.WSTestClient;
import uk.gov.bis.lite.controlcode.api.view.ControlCodeFullView;

import java.util.Map;
import java.util.concurrent.ExecutionException;

public class RelatedControlsServiceConsumerPact {

  // service:password
  private static final Map<String, String> AUTH_HEADERS = ImmutableMap.of("Authorization", "Basic c2VydmljZTpwYXNzd29yZA==");
  private static final Map<String, String> CONTENT_TYPE_HEADERS = ImmutableMap.of("Content-Type", "application/json");

  private RelatedControlsServiceClient client;
  private WSClient ws;

  private final static String CONTROL_CODE = "ML1a";
  private final static String TITLE = "Smooth-bore military weapons, components and accessories";

  @Rule
  public PactProviderRuleMk2 mockProvider = new PactProviderRuleMk2(PactConfig.CONTROL_CODE_SERVICE_PROVIDER, this);

  @Before
  public void setUp() throws Exception {
    ws = WSTestClient.newClient(mockProvider.getPort());
    client = new RelatedControlsServiceClient(new HttpExecutionContext(Runnable::run),
        ws,
        mockProvider.getUrl(),
        10000,
        "service:password");
  }

  @After
  public void tearDown() throws Exception {
    ws.close();
  }

  @Pact(provider = PactConfig.CONTROL_CODE_SERVICE_PROVIDER, consumer = PactConfig.CONSUMER)
  public RequestResponsePact softwareControlsRelatedToML1aExist(PactDslWithProvider builder) {
    PactDslJsonArray codes = PactDslJsonArray.arrayMinLike(1,3)
        .stringType("controlCode", CONTROL_CODE)
        .stringType("title", TITLE)
        .closeObject()
        .asArray();

    return builder
        .given("software controls related to ML1a exist")
        .uponReceiving("a request for software controls related to ML1a")
          .headers(AUTH_HEADERS)
          .path("/mapped-controls/software/ML1a")
          .method("GET")
          .willRespondWith()
            .status(200)
            .headers(CONTENT_TYPE_HEADERS)
            .body(codes)
        .toPact();
  }

  @Pact(provider = PactConfig.CONTROL_CODE_SERVICE_PROVIDER, consumer = PactConfig.CONSUMER)
  public RequestResponsePact softwareControlsRelatedToML1aDoNotExist(PactDslWithProvider builder) {
    return builder
        .given("software controls related to ML1a do not exist")
        .uponReceiving("a request for software controls related to ML1a")
          .headers(AUTH_HEADERS)
          .path("/mapped-controls/software/ML1a")
          .method("GET")
          .willRespondWith()
            .headers(CONTENT_TYPE_HEADERS)
            .body(new PactDslJsonArray())
        .toPact();
  }

  @Test
  @PactVerification(value = PactConfig.CONTROL_CODE_SERVICE_PROVIDER, fragment = "softwareControlsRelatedToML1aExist")
  public void verifySoftwareControlsRelatedToML1aExist() throws Exception {
    RelatedControlsServiceResult result;
    try {
      result = client.get(GoodsType.SOFTWARE, CONTROL_CODE).toCompletableFuture().get();
    } catch (InterruptedException | ExecutionException e) {
      throw new RuntimeException(e);
    }
    assertThat(result).isNotNull();
    assertThat(result.getControlCodes().size()).isEqualTo(3);
    ControlCodeFullView controlCode = result.getControlCodes().get(0);
    assertThat(controlCode.getControlCode()).isEqualTo(CONTROL_CODE);
    assertThat(controlCode.getTitle()).isEqualTo(TITLE);
  }

  @Test
  @PactVerification(value = PactConfig.CONTROL_CODE_SERVICE_PROVIDER, fragment = "softwareControlsRelatedToML1aDoNotExist")
  public void verifySoftwareControlsRelatedToML1aDoNotExist() throws Exception {
    RelatedControlsServiceResult result;
    try {
      result = client.get(GoodsType.SOFTWARE, CONTROL_CODE).toCompletableFuture().get();
    } catch (InterruptedException | ExecutionException e) {
      throw new RuntimeException(e);
    }
    assertThat(result).isNotNull();
    assertThat(result.getControlCodes().isEmpty()).isTrue();
  }
}
