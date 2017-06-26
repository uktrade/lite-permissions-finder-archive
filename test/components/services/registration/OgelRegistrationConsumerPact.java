package components.services.registration;


import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.when;

import au.com.dius.pact.consumer.Pact;
import au.com.dius.pact.consumer.PactProviderRule;
import au.com.dius.pact.consumer.PactVerification;
import au.com.dius.pact.consumer.dsl.PactDslJsonBody;
import au.com.dius.pact.consumer.dsl.PactDslWithProvider;
import au.com.dius.pact.model.PactFragment;
import components.persistence.PermissionsFinderDao;
import exceptions.ServiceException;
import models.summary.Summary;
import models.summary.SummaryField;
import models.summary.SummaryFieldType;
import models.summary.SummaryService;
import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnit;
import org.mockito.junit.MockitoRule;
import pact.PactConfig;
import play.libs.concurrent.HttpExecutionContext;
import play.libs.ws.WS;
import play.libs.ws.WSClient;
import play.mvc.Result;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;

//@RunWith(MockitoJUnitRunner.class)
public class OgelRegistrationConsumerPact {
  private OgelRegistrationServiceClient client;
  private WSClient ws;

  @Mock
  private PermissionsFinderDao permissionsFinderDao;

  @Mock
  private SummaryService summaryService;

  private static final String SHARED_SECRET = "secret";
  private static final String APPLICATION_CODE = "ABCD-1234";
  private static final String TRANSACTION_ID = "12345";
  private static final String REDIRECT_URL = "http://localhost/redirect";
  private static final String OK_STATUS = "ok";
  private static final String ERROR_STATUS = "error";

  @Rule
  public MockitoRule rule = MockitoJUnit.rule();

  @Rule
  public PactProviderRule mockProvider = new PactProviderRule(PactConfig.OGEL_REGISTRATION_PROVIDER, this);

  @Before
  public void setUp() throws Exception {
    ws = WS.newClient(mockProvider.getConfig().getPort());
    client = new OgelRegistrationServiceClient(ws, mockProvider.getConfig().url(), 10000, SHARED_SECRET, permissionsFinderDao, new HttpExecutionContext(Runnable::run), summaryService);
  }

  @After
  public void tearDown() throws Exception {
    ws.close();
  }

  private Summary buildTestSummary() {
    SummaryField summaryField1 = new SummaryField(SummaryFieldType.OGEL_TYPE, "some heading 1", "some content 1", "some data 1", "http://localhost/edit/1", false, true);
    SummaryField summaryField2 = new SummaryField(SummaryFieldType.CONTROL_CODE, "some heading 2", "some content 2", "some data 2", "http://localhost/edit/2", false, true);
    SummaryField summaryField3 = new SummaryField(SummaryFieldType.DESTINATION_COUNTRIES, "some heading 3", "some content 3", "some data 3", "http://localhost/edit/3", false, true);
    Summary summary = new Summary(APPLICATION_CODE)
        .addSummaryField(summaryField1)
        .addSummaryField(summaryField2)
        .addSummaryField(summaryField3);
    return summary;
  }

  @Pact(provider = PactConfig.OGEL_REGISTRATION_PROVIDER, consumer = PactConfig.CONSUMER)
  public PactFragment validTransactionData(PactDslWithProvider builder) {
    PactDslJsonBody body = new PactDslJsonBody()
        .stringType("status", OK_STATUS)
        .stringType("redirectUrl", REDIRECT_URL)
        .asBody();

    Map<String, String> headers = new HashMap<>();
    headers.put("Content-Type", "application/json");

    return builder
        .given("provided transaction data is valid")
        .uponReceiving("a request to update a transaction")
          .path("/update-transaction")
          .query("securityToken=" + SHARED_SECRET)
          .method("POST")
        .willRespondWith()
          .status(200)
          .headers(headers)
          .body(body)
        .toFragment();
  }


  @Pact(provider = PactConfig.OGEL_REGISTRATION_PROVIDER, consumer = PactConfig.CONSUMER)
  public PactFragment invalidTransactionData(PactDslWithProvider builder) {
    PactDslJsonBody body = new PactDslJsonBody()
        .stringType("status", ERROR_STATUS)
        .stringType("errorMessage", "Invalid request transaction data.")
        .asBody();

    Map<String, String> headers = new HashMap<>();
    headers.put("Content-Type", "application/json");

    return builder
        .given("provided transaction data is invalid")
        .uponReceiving("a request to update a transaction")
          .path("/update-transaction")
          .query("securityToken=" + SHARED_SECRET)
          .method("POST")
        .willRespondWith()
          .status(400)
          .headers(headers)
          .body(body)
        .toFragment();
  }

  @Test
  @PactVerification(value = PactConfig.OGEL_REGISTRATION_PROVIDER, fragment = "validTransactionData")
  public void validTransactionDataTest() throws Exception {
    Summary summary = buildTestSummary();

    when(summaryService.composeSummary()).thenReturn(CompletableFuture.completedFuture(summary));

    Result result;
    try {
      result = client.updateTransactionAndRedirect(TRANSACTION_ID).toCompletableFuture().get();
    }
    catch (InterruptedException | ExecutionException e) {
      throw new RuntimeException(e);
    }

    assertThat(result.status()).isEqualTo(303);
    assertThat(result.redirectLocation().isPresent()).isTrue();
    assertThat(result.redirectLocation().get()).isEqualTo(REDIRECT_URL);
  }

  @Test
  @PactVerification(value = PactConfig.OGEL_REGISTRATION_PROVIDER, fragment = "invalidTransactionData")
  public void invalidTransactionDataTest() throws Exception {
    Summary summary = buildTestSummary();

    when(summaryService.composeSummary()).thenReturn(CompletableFuture.completedFuture(summary));

    assertThatThrownBy(() ->  client.updateTransactionAndRedirect(TRANSACTION_ID).toCompletableFuture().get())
        .hasRootCauseInstanceOf(ServiceException.class)
        .hasMessageContaining("Unexpected HTTP status code from OGEL Registration service /update-transaction: 400");
  }

}
