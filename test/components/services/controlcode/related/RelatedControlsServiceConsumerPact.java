package components.services.controlcode.related;

import static org.assertj.core.api.Assertions.assertThat;

import au.com.dius.pact.consumer.Pact;
import au.com.dius.pact.consumer.PactProviderRule;
import au.com.dius.pact.consumer.PactVerification;
import au.com.dius.pact.consumer.dsl.PactDslJsonArray;
import au.com.dius.pact.consumer.dsl.PactDslWithProvider;
import au.com.dius.pact.model.PactFragment;
import models.GoodsType;
import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import pact.PactConfig;
import play.libs.concurrent.HttpExecutionContext;
import play.libs.ws.WS;
import play.libs.ws.WSClient;
import uk.gov.bis.lite.controlcode.api.view.ControlCodeFullView;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ExecutionException;

public class RelatedControlsServiceConsumerPact {
  private RelatedControlsServiceClient client;
  private WSClient ws;

  private final static String CONTROL_CODE = "ML1a";
  private final static String TITLE = "Smooth-bore military weapons, components and accessories";

  @Rule
  public PactProviderRule mockProvider = new PactProviderRule(PactConfig.CONTROL_CODE_SERVICE_PROVIDER, this);

  @Before
  public void setUp() throws Exception {
    ws = WS.newClient(mockProvider.getConfig().getPort());
    client = new RelatedControlsServiceClient(new HttpExecutionContext(Runnable::run), ws, mockProvider.getConfig().url(), 10000);
  }

  @After
  public void tearDown() throws Exception {
    ws.close();
  }

  @Pact(provider = PactConfig.CONTROL_CODE_SERVICE_PROVIDER, consumer = PactConfig.CONSUMER)
  public PactFragment softwareControlsRelatedToML1aExist(PactDslWithProvider builder) {
    PactDslJsonArray codes = PactDslJsonArray.arrayMinLike(1,3)
        .stringType("controlCode", CONTROL_CODE)
        .stringType("title", TITLE)
        .closeObject()
        .asArray();

    Map<String, String> headers = new HashMap<>();
    headers.put("Content-Type", "application/json");

    return builder
        .given("software controls related to ML1a exist")
        .uponReceiving("a request for software controls related to ML1a")
          .path("/mapped-controls/software/ML1a")
          .method("GET")
          .willRespondWith()
            .headers(headers)
            .body(codes)
        .toFragment();
  }

  @Pact(provider = PactConfig.CONTROL_CODE_SERVICE_PROVIDER, consumer = PactConfig.CONSUMER)
  public PactFragment softwareControlsRelatedToML1aDoNotExist(PactDslWithProvider builder) {

    Map<String, String> headers = new HashMap<>();
    headers.put("Content-Type", "application/json");

    return builder
        .given("software controls related to ML1a do not exist")
        .uponReceiving("a request for software controls related to ML1a")
          .path("/mapped-controls/software/ML1a")
          .method("GET")
          .willRespondWith()
            .headers(headers)
            .body(new PactDslJsonArray())
        .toFragment();
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
