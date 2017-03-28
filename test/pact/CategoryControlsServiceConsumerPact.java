package pact;

import static org.assertj.core.api.Assertions.assertThat;

import au.com.dius.pact.consumer.Pact;
import au.com.dius.pact.consumer.PactProviderRule;
import au.com.dius.pact.consumer.PactVerification;
import au.com.dius.pact.consumer.dsl.PactDslJsonArray;
import au.com.dius.pact.consumer.dsl.PactDslWithProvider;
import au.com.dius.pact.model.PactFragment;
import components.services.controlcode.category.CategoryControlsServiceClient;
import components.services.controlcode.category.CategoryControlsServiceResult;
import models.GoodsType;
import models.softtech.SoftTechCategory;
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

public class CategoryControlsServiceConsumerPact {

  private CategoryControlsServiceClient client;
  private WSClient ws;

  private final static String PROVIDER = "lite-control-code-service#specific-controls";
  private final static String CONSUMER = "lite-permissions-finder";

  @Rule
  public PactProviderRule mockProvider = new PactProviderRule(PROVIDER, this);

  @Pact(provider = PROVIDER, consumer = CONSUMER)
  public PactFragment createFragment(PactDslWithProvider builder) {
    PactDslJsonArray existing = new PactDslJsonArray().asArray();

    Map<String, String> headers = new HashMap<>();
    headers.put("Content-Type", "application/json");

    return builder
        .uponReceiving("Existing frontend control code request")
          .path("/specific-controls/software/military")
          .method("GET")
          .willRespondWith()
            .status(200)
            .headers(headers)
            .body(existing)
        .toFragment();
  }

  @Before
  public void setUp() throws Exception {
    ws = WS.newClient(mockProvider.getConfig().getPort());
    client = new CategoryControlsServiceClient(new HttpExecutionContext(Runnable::run), ws, mockProvider.getConfig().url(), 10000);
  }

  @Test
  @PactVerification(PROVIDER)
  public void testControlCodeServicePact() throws Exception {
    CategoryControlsServiceResult result;
    try {
      result = client.get(GoodsType.SOFTWARE, SoftTechCategory.MILITARY).toCompletableFuture().get();
    }
    catch (InterruptedException | ExecutionException e) {
      throw new RuntimeException(e);
    }
    assertThat(result).isNotNull();
  }


  @After
  public void tearDown() throws Exception {
    ws.close();
  }
}
