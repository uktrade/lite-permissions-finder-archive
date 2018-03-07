package components.services.controlcode.catchall;


import static org.assertj.core.api.Assertions.assertThat;

import au.com.dius.pact.consumer.Pact;
import au.com.dius.pact.consumer.PactProviderRule;
import au.com.dius.pact.consumer.PactVerification;
import au.com.dius.pact.consumer.dsl.PactDslJsonArray;
import au.com.dius.pact.consumer.dsl.PactDslWithProvider;
import au.com.dius.pact.model.PactFragment;
import com.google.common.collect.ImmutableMap;
import models.GoodsType;
import models.softtech.SoftTechCategory;
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

public class CatchallControlsServiceConsumerPact {

  // service:password
  private static final Map<String, String> AUTH_HEADERS = ImmutableMap.of("Authorization", "Basic c2VydmljZTpwYXNzd29yZA==");
  private static final Map<String, String> CONTENT_TYPE_HEADERS = ImmutableMap.of("Content-Type", "application/json");

  private CatchallControlsServiceClient client;
  private WSClient ws;

  private final static String CONTROL_CODE = "ML1a";
  private final static String TITLE = "Smooth-bore military weapons, components and accessories";

  @Rule
  public PactProviderRule mockProvider = new PactProviderRule(PactConfig.CONTROL_CODE_SERVICE_PROVIDER, this);

  @Pact(provider = PactConfig.CONTROL_CODE_SERVICE_PROVIDER, consumer = PactConfig.CONSUMER)
  public PactFragment softwareMilitaryExists(PactDslWithProvider builder) {
    PactDslJsonArray codes = PactDslJsonArray.arrayMinLike(1,3)
        .stringType("controlCode", CONTROL_CODE)
        .stringType("title", TITLE)
        .closeObject()
        .asArray();

    return builder
        .given("military catchall controls exist for software")
        .uponReceiving("a request for military software catchall controls")
          .headers(AUTH_HEADERS)
          .path("/catch-all-controls/software/military")
          .method("GET")
          .willRespondWith()
            .status(200)
            .headers(CONTENT_TYPE_HEADERS)
            .body(codes)
        .toFragment();
  }

  @Pact(provider = PactConfig.CONTROL_CODE_SERVICE_PROVIDER, consumer = PactConfig.CONSUMER)
  public PactFragment softwareMilitaryDoesNotExist(PactDslWithProvider builder) {
    return builder
        .given("military catchall controls do not exist for software")
        .uponReceiving("a request for military software catchall controls")
          .headers(AUTH_HEADERS)
          .path("/catch-all-controls/software/military")
          .method("GET")
          .willRespondWith()
            .status(200)
            .headers(CONTENT_TYPE_HEADERS)
            .body(new PactDslJsonArray())
        .toFragment();
  }

  @Pact(provider = PactConfig.CONTROL_CODE_SERVICE_PROVIDER, consumer = PactConfig.CONSUMER)
  public PactFragment softwareDualUseExists(PactDslWithProvider builder) {
    PactDslJsonArray codes = PactDslJsonArray.arrayMinLike(1,3)
        .stringType("controlCode", CONTROL_CODE)
        .stringType("title", TITLE)
        .closeObject()
        .asArray();

    return builder
        .given("dual use catchall controls exist for software")
        .uponReceiving("a request for dual use software catchall controls")
          .headers(AUTH_HEADERS)
          .path("/catch-all-controls/software/dual-use")
          .method("GET")
          .willRespondWith()
            .status(200)
            .headers(CONTENT_TYPE_HEADERS)
            .body(codes)
        .toFragment();
  }

  @Pact(provider = PactConfig.CONTROL_CODE_SERVICE_PROVIDER, consumer = PactConfig.CONSUMER)
  public PactFragment softwareDualUseDoesNotExist(PactDslWithProvider builder) {
    return builder
        .given("dual use catchall controls do not exist for software")
        .uponReceiving("a request for dual use software catchall controls")
          .headers(AUTH_HEADERS)
          .path("/catch-all-controls/software/dual-use")
          .method("GET")
          .willRespondWith()
            .status(200)
            .headers(CONTENT_TYPE_HEADERS)
            .body(new PactDslJsonArray())
        .toFragment();
  }

  @Test
  @PactVerification(value = PactConfig.CONTROL_CODE_SERVICE_PROVIDER, fragment = "softwareMilitaryExists")
  public void softwareMilitary() {
    CatchallControlsServiceResult catchallControls;
    try {
      catchallControls = client.get(GoodsType.SOFTWARE, SoftTechCategory.MILITARY).toCompletableFuture().get();
    }
    catch (InterruptedException | ExecutionException e) {
      throw new RuntimeException(e);
    }
    assertThat(catchallControls).isNotNull();
    assertThat(catchallControls.getControlCodes().size()).isEqualTo(3);
    assertThat(catchallControls.getControlCodesGroupedByTitle().size()).isEqualTo(1);
    assertThat(catchallControls.getRelatedControlCodes(CONTROL_CODE).size()).isEqualTo(3);
    ControlCodeFullView controlCode = catchallControls.getControlCodes().get(0);
    assertThat(controlCode.getControlCode()).isEqualTo(CONTROL_CODE);
    assertThat(controlCode.getTitle()).isEqualTo(TITLE);
  }

  @Test
  @PactVerification(value = PactConfig.CONTROL_CODE_SERVICE_PROVIDER, fragment = "softwareMilitaryDoesNotExist")
  public void softwareMilitaryDoesNotExist() {
    CatchallControlsServiceResult catchallControls;
    try {
      catchallControls = client.get(GoodsType.SOFTWARE, SoftTechCategory.MILITARY).toCompletableFuture().get();
    }
    catch (InterruptedException | ExecutionException e) {
      throw new RuntimeException(e);
    }
    assertThat(catchallControls).isNotNull();
    assertThat(catchallControls.getControlCodes().isEmpty()).isTrue();
  }

  @Test
  @PactVerification(value = PactConfig.CONTROL_CODE_SERVICE_PROVIDER, fragment = "softwareDualUseExists")
  public void softwareDualUseExists() {
    CatchallControlsServiceResult catchallControls;
    try {
      // TELECOMS is dual use
      catchallControls = client.get(GoodsType.SOFTWARE, SoftTechCategory.TELECOMS).toCompletableFuture().get();
    }
    catch (InterruptedException | ExecutionException e) {
      throw new RuntimeException(e);
    }
    assertThat(catchallControls).isNotNull();
    assertThat(catchallControls.getControlCodes().size()).isEqualTo(3);
    assertThat(catchallControls.getControlCodesGroupedByTitle().size()).isEqualTo(1);
    assertThat(catchallControls.getRelatedControlCodes(CONTROL_CODE).size()).isEqualTo(3);
    ControlCodeFullView controlCode = catchallControls.getControlCodes().get(0);
    assertThat(controlCode.getControlCode()).isEqualTo(CONTROL_CODE);
    assertThat(controlCode.getTitle()).isEqualTo(TITLE);
  }

  @Test
  @PactVerification(value = PactConfig.CONTROL_CODE_SERVICE_PROVIDER, fragment = "softwareDualUseDoesNotExist")
  public void softwareDualUseDoesNotExist() {
    CatchallControlsServiceResult catchallControls;
    try {
      // TELECOMS is dual use
      catchallControls = client.get(GoodsType.SOFTWARE, SoftTechCategory.TELECOMS).toCompletableFuture().get();
    }
    catch (InterruptedException | ExecutionException e) {
      throw new RuntimeException(e);
    }
    assertThat(catchallControls).isNotNull();
    assertThat(catchallControls.getControlCodes().isEmpty()).isTrue();
  }

  @Before
  public void setUp() throws Exception {
    ws = WSTestClient.newClient(mockProvider.getConfig().getPort());
    client = new CatchallControlsServiceClient(new HttpExecutionContext(Runnable::run),
        ws,
        mockProvider.getConfig().url(),
        10000,
        "service:password");
  }

  @After
  public void tearDown() throws Exception {
    ws.close();
  }
}
