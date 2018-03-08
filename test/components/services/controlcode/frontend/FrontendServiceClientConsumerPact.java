package components.services.controlcode.frontend;

import static org.assertj.core.api.Assertions.assertThat;

import au.com.dius.pact.consumer.Pact;
import au.com.dius.pact.consumer.PactProviderRule;
import au.com.dius.pact.consumer.PactVerification;
import au.com.dius.pact.consumer.dsl.PactDslJsonBody;
import au.com.dius.pact.consumer.dsl.PactDslWithProvider;
import au.com.dius.pact.model.PactFragment;
import com.google.common.collect.ImmutableMap;
import exceptions.ServiceException;
import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import pact.PactConfig;
import play.libs.concurrent.HttpExecutionContext;
import play.libs.ws.WSClient;
import play.test.WSTestClient;
import uk.gov.bis.lite.controlcode.api.view.ControlCodeSummary;
import uk.gov.bis.lite.controlcode.api.view.FrontEndControlCodeView;
import uk.gov.bis.lite.controlcode.api.view.FrontEndControlCodeView.FrontEndControlCodeData;
import uk.gov.bis.lite.controlcode.api.view.FrontEndControlCodeView.FrontEndControlCodeData.Decontrol;
import uk.gov.bis.lite.controlcode.api.view.FrontEndControlCodeView.FrontEndControlCodeData.FormattedAdditionalSpecifications;
import uk.gov.bis.lite.controlcode.api.view.FrontEndControlCodeView.FrontEndControlCodeData.FormattedAdditionalSpecifications.AdditionalSpecificationText;

import java.util.Map;
import java.util.concurrent.ExecutionException;

public class FrontendServiceClientConsumerPact {

  // service:password
  private static final Map<String, String> AUTH_HEADERS = ImmutableMap.of("Authorization", "Basic c2VydmljZTpwYXNzd29yZA==");
  private static final Map<String, String> CONTENT_TYPE_HEADERS = ImmutableMap.of("Content-Type", "application/json");

  private FrontendServiceClient client;
  private WSClient ws;

  private static final String CONTROL_CODE = "ML1a";
  private static final String FREINDLY_DESCRIPTION = "Rifles and combination guns, handguns, machine, sub-machine and volley guns";
  private static final String TITLE = "Smooth-bore military weapons, components and accessories";
  private static final String TECHNICAL_NOTES = "<p>Motion simulators or rate tables here remain controlled whether or not slip rings.</p>";
  private static final String ALIAS = "ML1a";
  private static final String CLAUSE_TEXT = "<p>The motion simulators or rate tables here must have any of the following:</p>";
  private static final String SPECIFICATION_TEXT = "A positioning accuracy equal to or less better than 5 arc second";
  private static final String LINKED_CONTROL_CODE = "ML1";
  private static final String ORIGIN_CONTROL_CODE = "ML1";
  private static final String DECONTROLS_TEXT = "Firearms designed for dummy ammunition, incapable of discharging a projectile";
  private static final String LINEAGE_CONTROL_CODE = "ML1";
  private static final String LINEAGE_ALIAS = "ML1";
  private static final String LINEAGE_FRIENDLY_DESCRIPTION = "Smooth-bore weapons with a calibre of less than 20mm, other firearms and automatic weapons with a calibre of 12.7mm or less, and accessories and specially designed components";

  @Rule
  public PactProviderRule mockProvider = new PactProviderRule(PactConfig.CONTROL_CODE_SERVICE_PROVIDER, this);

  @Before
  public void setUp() throws Exception {
    ws = WSTestClient.newClient(mockProvider.getConfig().getPort());
    client = new FrontendServiceClient(new HttpExecutionContext(Runnable::run),
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
  public PactFragment existingControlCode(PactDslWithProvider builder) {
    PactDslJsonBody existing = new PactDslJsonBody()
        .object("controlCodeData")
          .stringType("controlCode", CONTROL_CODE)
          .stringType("friendlyDescription", FREINDLY_DESCRIPTION)
          .stringType("title", TITLE)
          .stringType("technicalNotes", TECHNICAL_NOTES)
          .stringType("alias", ALIAS)
          .object("additionalSpecifications")
            .stringType("clauseText", CLAUSE_TEXT)
            .eachLike("specificationText", 3)
              .stringType("text", SPECIFICATION_TEXT)
              .stringType("linkedControlCode", LINKED_CONTROL_CODE)
              .closeObject()
            .closeArray()
          .closeObject()
          .eachLike("decontrols", 3)
            .stringType("originControlCode", ORIGIN_CONTROL_CODE)
            .stringType("text", DECONTROLS_TEXT)
            .closeObject()
          .closeArray()
        .closeObject()
        .eachLike("lineage", 3)
          .stringType("controlCode", LINEAGE_CONTROL_CODE)
          .stringType("alias", LINEAGE_ALIAS)
          .stringType("friendlyDescription", LINEAGE_FRIENDLY_DESCRIPTION)
          .closeObject()
        .closeArray()
        .asBody();

    return builder
        .given("provided control code exists")
        .uponReceiving("a request for code control code " + CONTROL_CODE)
          .headers(AUTH_HEADERS)
          .path("/frontend-control-codes/" + CONTROL_CODE)
          .method("GET")
          .willRespondWith()
            .status(200)
            .headers(CONTENT_TYPE_HEADERS)
            .body(existing)
        .toFragment();
  }

  @Pact(provider = PactConfig.CONTROL_CODE_SERVICE_PROVIDER, consumer = PactConfig.CONSUMER)
  public PactFragment missingControlCode(PactDslWithProvider builder) {
    PactDslJsonBody missing = new PactDslJsonBody()
        .integerType("code", 404)
        .stringType("message")
        .asBody();

    return builder
        .given("provided control code does not exist")
        .uponReceiving("a request for code control code " + CONTROL_CODE)
        .headers(AUTH_HEADERS)
        .path("/frontend-control-codes/" + CONTROL_CODE)
        .method("GET")
        .willRespondWith()
            .status(404)
            .headers(CONTENT_TYPE_HEADERS)
            .body(missing)
        .toFragment();
  }

  @Test
  @PactVerification(value = PactConfig.CONTROL_CODE_SERVICE_PROVIDER, fragment = "existingControlCode")
  public void existingControlCode() throws Exception {
    FrontendServiceResult frontendServiceResult;
    try {
      frontendServiceResult = client.get(CONTROL_CODE).toCompletableFuture().get();
    }
    catch (InterruptedException | ExecutionException e) {
      throw new RuntimeException(e);
    }
    assertThat(frontendServiceResult).isNotNull();

    FrontEndControlCodeView frontEndControlCode = frontendServiceResult.getFrontendControlCode();
    FrontEndControlCodeData controlCode = frontEndControlCode.getControlCodeData();
    assertThat(controlCode.getControlCode()).isEqualTo(CONTROL_CODE);
    assertThat(controlCode.getFriendlyDescription()).isEqualTo(FREINDLY_DESCRIPTION);
    assertThat(controlCode.getTitle()).isEqualTo(TITLE);
    assertThat(controlCode.getTechnicalNotes()).isEqualTo(TECHNICAL_NOTES);
    assertThat(controlCode.getAlias()).isEqualTo(ALIAS);

    FormattedAdditionalSpecifications additionalSpecifications = controlCode.getAdditionalSpecifications();
    assertThat(additionalSpecifications.getClauseText()).isEqualTo(CLAUSE_TEXT);
    assertThat(additionalSpecifications.getSpecificationText().size()).isEqualTo(3);

    AdditionalSpecificationText additionalSpecificationText = additionalSpecifications.getSpecificationText().get(0);
    assertThat(additionalSpecificationText.getLinkedControlCode()).isEqualTo(LINKED_CONTROL_CODE);
    assertThat(additionalSpecificationText.getText()).isEqualTo(SPECIFICATION_TEXT);
    assertThat(controlCode.getDecontrols().size()).isEqualTo(3);

    Decontrol decontrol = controlCode.getDecontrols().get(0);
    assertThat(decontrol.getOriginControlCode()).isEqualTo(ORIGIN_CONTROL_CODE);
    assertThat(decontrol.getText()).isEqualTo(DECONTROLS_TEXT);
    assertThat(frontEndControlCode.getLineage().size()).isEqualTo(3);

    ControlCodeSummary summary = frontEndControlCode.getLineage().get(0);
    assertThat(summary.getControlCode()).isEqualTo(LINEAGE_CONTROL_CODE);
    assertThat(summary.getAlias()).isEqualTo(LINEAGE_ALIAS);
    assertThat(summary.getFriendlyDescription()).isEqualTo(LINEAGE_FRIENDLY_DESCRIPTION);

    assertThat(frontendServiceResult.canShowDecontrols()).isTrue();
    assertThat(frontendServiceResult.canShowAdditionalSpecifications()).isTrue();
    assertThat(frontendServiceResult.canShowTechnicalNotes()).isTrue();
  }

  @Test
  @PactVerification(value = PactConfig.CONTROL_CODE_SERVICE_PROVIDER, fragment = "missingControlCode")
  public void missingControlCode() {
    FrontendServiceResult frontendServiceResult = null;
    try {
      frontendServiceResult = client.get(CONTROL_CODE).toCompletableFuture().get();
    }
    catch (Exception e) {
      assertThat(e)
          .isInstanceOf(ExecutionException.class)
          .hasCauseInstanceOf(ServiceException.class);
    }
    assertThat(frontendServiceResult).isNull();
  }
}
