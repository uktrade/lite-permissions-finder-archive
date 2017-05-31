package components.services.ogels.ogel;

import static models.summary.SummaryFieldType.CONTROL_CODE;
import static org.assertj.core.api.Assertions.assertThat;

import au.com.dius.pact.consumer.Pact;
import au.com.dius.pact.consumer.PactProviderRule;
import au.com.dius.pact.consumer.PactVerification;
import au.com.dius.pact.consumer.dsl.PactDslJsonBody;
import au.com.dius.pact.consumer.dsl.PactDslJsonRootValue;
import au.com.dius.pact.consumer.dsl.PactDslWithProvider;
import au.com.dius.pact.model.PactFragment;
import components.services.ogels.applicable.ApplicableOgelServiceResult;
import exceptions.ServiceException;
import models.OgelActivityType;
import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import pact.PactConfig;
import play.libs.concurrent.HttpExecutionContext;
import play.libs.ws.WS;
import play.libs.ws.WSClient;
import uk.gov.bis.lite.ogel.api.view.OgelFullView;
import uk.gov.bis.lite.ogel.api.view.OgelFullView.OgelConditionSummary;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutionException;

public class OgelConsumerPact {
  private OgelServiceClient client;
  private WSClient ws;

  private static final String OGEL_ID = "OGL1";
  private static final String OGEL_NAME = "Military goods, software and technology: government or NATO end us";
  private static final String OGEL_DESC = "description";
  private static final String OGEL_LINK = "http://example.org";
  private static final String OGEL_CAN = "can";
  private static final String OGEL_CANT = "can't";
  private static final String OGEL_MUST = "must";
  private static final String OGEL_HOW_TO_USE = "how to use";

  @Rule
  public PactProviderRule mockProvider = new PactProviderRule(PactConfig.OGEL_SERVICE_PROVIDER, this);

  @Before
  public void setUp() throws Exception {
    ws = WS.newClient(mockProvider.getConfig().getPort());
    client = new OgelServiceClient(new HttpExecutionContext(Runnable::run), ws, mockProvider.getConfig().url(), 10000);
  }

  @After
  public void tearDown() throws Exception {
    ws.close();
  }

  @Pact(provider = PactConfig.OGEL_SERVICE_PROVIDER, consumer = PactConfig.CONSUMER)
  public PactFragment ogelExists(PactDslWithProvider builder) {
    PactDslJsonBody body = new PactDslJsonBody()
        .stringType("id", OGEL_ID)
        .stringType("name", OGEL_NAME)
        .stringType("description", OGEL_DESC)
        .stringType("link", OGEL_LINK)
        .object("summary")
          .minArrayLike("canList", 0, PactDslJsonRootValue.stringType(OGEL_CAN),3)
          .minArrayLike("cantList", 0, PactDslJsonRootValue.stringType(OGEL_CANT), 3)
          .minArrayLike("mustList", 0, PactDslJsonRootValue.stringType(OGEL_MUST), 3)
          .minArrayLike("howToUseList", 0, PactDslJsonRootValue.stringType(OGEL_HOW_TO_USE), 3)
        .closeObject()
        .asBody();

    Map<String, String> headers = new HashMap<>();
    headers.put("Content-Type", "application/json");

    return builder
        .given("requested ogel exists")
        .uponReceiving("a request for a given ogel id")
          .path("/ogels/" + OGEL_ID)
          .method("GET")
        .willRespondWith()
          .status(200)
          .headers(headers)
          .body(body)
        .toFragment();
  }

  @Pact(provider = PactConfig.OGEL_SERVICE_PROVIDER, consumer = PactConfig.CONSUMER)
  public PactFragment ogelDoesNotExist(PactDslWithProvider builder) {
    PactDslJsonBody body = new PactDslJsonBody()
        .integerType("code", 404)
        .stringType("message", "No Ogel Found With Given Ogel ID: " + OGEL_ID)
        .asBody();

    Map<String, String> headers = new HashMap<>();
    headers.put("Content-Type", "application/json");

    return builder
        .given("requested ogel does not exist")
        .uponReceiving("a request for a given ogel id")
          .path("/ogels/" + OGEL_ID)
          .method("GET")
        .willRespondWith()
        .status(404)
          .headers(headers)
          .body(body)
        .toFragment();
  }

  @Test
  @PactVerification(value = PactConfig.OGEL_SERVICE_PROVIDER, fragment = "ogelExists")
  public void ogelExistsTest() throws Exception {
    OgelFullView result;
    try {
      result = client.get(OGEL_ID).toCompletableFuture().get();
    }
    catch (InterruptedException | ExecutionException e) {
      throw new RuntimeException(e);
    }
    assertThat(result).isNotNull();
    assertThat(result.getId()).isEqualTo(OGEL_ID);
    assertThat(result.getName()).isEqualTo(OGEL_NAME);
    assertThat(result.getDescription()).isEqualTo(OGEL_DESC);
    assertThat(result.getLink()).isEqualTo(OGEL_LINK);
    OgelConditionSummary summary = result.getSummary();
    assertThat(summary).isNotNull();
    assertThat(summary.getCanList().size()).isEqualTo(3);
    assertThat(summary.getCanList().get(0)).isEqualTo(OGEL_CAN);
    assertThat(summary.getCantList().size()).isEqualTo(3);
    assertThat(summary.getCantList().get(0)).isEqualTo(OGEL_CANT);
    assertThat(summary.getMustList().size()).isEqualTo(3);
    assertThat(summary.getMustList().get(0)).isEqualTo(OGEL_MUST);
    assertThat(summary.getHowToUseList().size()).isEqualTo(3);
    assertThat(summary.getHowToUseList().get(0)).isEqualTo(OGEL_HOW_TO_USE);
  }

  @Test
  @PactVerification(value = PactConfig.OGEL_SERVICE_PROVIDER, fragment = "ogelDoesNotExist")
  public void ogelDoesNotExistTest() throws Exception {
    OgelFullView result = null;
    try {
      result = client.get(OGEL_ID).toCompletableFuture().get();
    }
    catch (InterruptedException | ExecutionException e) {
      assertThat(e)
          .isInstanceOf(ExecutionException.class)
          .hasCauseInstanceOf(ServiceException.class);
    }
    assertThat(result).isNull();
  }
}

