package components.services.controlcode.nonexempt;

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

public class NonExemptControlServicePact {

  // service:password
  private static final Map<String, String> AUTH_HEADERS = ImmutableMap.of("Authorization", "Basic c2VydmljZTpwYXNzd29yZA==");
  private static final Map<String, String> CONTENT_TYPE_HEADERS = ImmutableMap.of("Content-Type", "application/json");

  private NonExemptControlServiceClient client;
  private WSClient ws;

  private final static String CONTROL_CODE = "ML1a";
  private final static String TITLE = "Smooth-bore military weapons, components and accessories";
  private final static String FRIENDLY_DESCRIPTION = "Rifles and combination guns, handguns, machine, sub-machine and volley guns";

  @Rule
  public PactProviderRule mockProvider = new PactProviderRule(PactConfig.CONTROL_CODE_SERVICE_PROVIDER, this);

  @Before
  public void setUp() throws Exception {
    ws = WSTestClient.newClient(mockProvider.getConfig().getPort());
    client = new NonExemptControlServiceClient(new HttpExecutionContext(Runnable::run),
        ws,
        mockProvider.getConfig().url(),
        10000,
        "service:password");
  }

  @After
  public void tearDown() throws Exception {
    ws.close();
  }

  @Pact(provider = PactConfig.CONTROL_CODE_SERVICE_PROVIDER, consumer = PactConfig.CONSUMER)
  public PactFragment softwareMilitaryControlsExist(PactDslWithProvider builder) {
    PactDslJsonArray codes = PactDslJsonArray.arrayMinLike(1,3)
        .stringType("controlCode", CONTROL_CODE)
        .stringType("title", TITLE)
        .stringType("friendlyDescription", FRIENDLY_DESCRIPTION)
        .closeObject()
        .asArray();

    return builder
        .given("military software non exempt controls exist")
        .uponReceiving("a request for military non exempt controls")
          .headers(AUTH_HEADERS)
          .path("/non-exempt/software/military")
          .method("GET")
          .willRespondWith()
            .headers(CONTENT_TYPE_HEADERS)
            .body(codes)
        .toFragment();
  }

  @Pact(provider = PactConfig.CONTROL_CODE_SERVICE_PROVIDER, consumer = PactConfig.CONSUMER)
  public PactFragment softwareMilitaryControlsDoNotExist(PactDslWithProvider builder) {
    return builder
        .given("military software non exempt controls do not exist")
        .uponReceiving("a request for military non exempt controls")
          .headers(AUTH_HEADERS)
          .path("/non-exempt/software/military")
          .method("GET")
          .willRespondWith()
            .status(200)
            .headers(CONTENT_TYPE_HEADERS)
            .body(new PactDslJsonArray())
        .toFragment();
  }

  @Pact(provider = PactConfig.CONTROL_CODE_SERVICE_PROVIDER, consumer = PactConfig.CONSUMER)
  public PactFragment softwareDualUseTelecomsControlsExist(PactDslWithProvider builder) {
    PactDslJsonArray codes = PactDslJsonArray.arrayMinLike(1,3)
        .stringType("controlCode", CONTROL_CODE)
        .stringType("title", TITLE)
        .stringType("friendlyDescription", FRIENDLY_DESCRIPTION)
        .closeObject()
        .asArray();

    return builder
        .given("military software non exempt controls exist")
        .uponReceiving("a request for dual use telecoms software non exempt controls")
          .headers(AUTH_HEADERS)
          .path("/non-exempt/software/dual-use/telecoms")
          .method("GET")
          .willRespondWith()
            .status(200)
            .headers(CONTENT_TYPE_HEADERS)
            .body(codes)
        .toFragment();
  }

  @Pact(provider = PactConfig.CONTROL_CODE_SERVICE_PROVIDER, consumer = PactConfig.CONSUMER)
  public PactFragment softwareDualUseTelecomsControlsDoNotExist(PactDslWithProvider builder) {
    return builder
        .given("dual use software non exempt controls do not exist")
        .uponReceiving("a request for dual use telecoms software non exempt controls")
          .headers(AUTH_HEADERS)
          .path("/non-exempt/software/dual-use/telecoms")
          .method("GET")
          .willRespondWith()
            .status(200)
            .headers(CONTENT_TYPE_HEADERS)
            .body(new PactDslJsonArray())
        .toFragment();
  }

  @Test
  @PactVerification(value = PactConfig.CONTROL_CODE_SERVICE_PROVIDER, fragment = "softwareMilitaryControlsExist")
  public void verifySoftwareMilitaryControlsExist() throws Exception {
    NonExemptControlsServiceResult result;
    try {
      result = client.get(GoodsType.SOFTWARE, SoftTechCategory.MILITARY).toCompletableFuture().get();
    } catch (InterruptedException | ExecutionException e) {
      throw new RuntimeException(e);
    }
    assertThat(result).isNotNull();
    assertThat(result.getControlCodes().size()).isEqualTo(3);
    ControlCodeFullView controlCode = result.getControlCodes().get(0);
    assertThat(controlCode.getControlCode()).isEqualTo(CONTROL_CODE);
    assertThat(controlCode.getTitle()).isEqualTo(TITLE);
    assertThat(controlCode.getFriendlyDescription()).isEqualTo(FRIENDLY_DESCRIPTION);
  }

  @Test
  @PactVerification(value = PactConfig.CONTROL_CODE_SERVICE_PROVIDER, fragment = "softwareMilitaryControlsDoNotExist")
  public void verifySoftwareMilitaryControlsDoNotExist() throws Exception {
    NonExemptControlsServiceResult result;
    try {
      result = client.get(GoodsType.SOFTWARE, SoftTechCategory.MILITARY).toCompletableFuture().get();
    } catch (InterruptedException | ExecutionException e) {
      throw new RuntimeException(e);
    }
    assertThat(result).isNotNull();
    assertThat(result.getControlCodes().isEmpty()).isTrue();
  }


  @Test
  @PactVerification(value = PactConfig.CONTROL_CODE_SERVICE_PROVIDER, fragment = "softwareDualUseTelecomsControlsExist")
  public void verifySoftwareDualUseTelecomsControlsExist() throws Exception {
    NonExemptControlsServiceResult result;
    try {
      result = client.get(GoodsType.SOFTWARE, SoftTechCategory.TELECOMS).toCompletableFuture().get();
    } catch (InterruptedException | ExecutionException e) {
      throw new RuntimeException(e);
    }
    assertThat(result).isNotNull();
    assertThat(result.getControlCodes().size()).isEqualTo(3);
    ControlCodeFullView controlCode = result.getControlCodes().get(0);
    assertThat(controlCode.getControlCode()).isEqualTo(CONTROL_CODE);
    assertThat(controlCode.getTitle()).isEqualTo(TITLE);
    assertThat(controlCode.getFriendlyDescription()).isEqualTo(FRIENDLY_DESCRIPTION);
  }

  @Test
  @PactVerification(value = PactConfig.CONTROL_CODE_SERVICE_PROVIDER, fragment = "softwareDualUseTelecomsControlsDoNotExist")
  public void verifySoftwareDualUseTelecomsControlsDoNotExist() throws Exception {
    NonExemptControlsServiceResult result;
    try {
      result = client.get(GoodsType.SOFTWARE, SoftTechCategory.TELECOMS).toCompletableFuture().get();
    } catch (InterruptedException | ExecutionException e) {
      throw new RuntimeException(e);
    }
    assertThat(result).isNotNull();
    assertThat(result.getControlCodes().isEmpty()).isTrue();
  }
}
