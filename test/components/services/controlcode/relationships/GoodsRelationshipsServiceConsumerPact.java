package components.services.controlcode.relationships;


import static org.assertj.core.api.Assertions.assertThat;

import au.com.dius.pact.consumer.Pact;
import au.com.dius.pact.consumer.PactProviderRule;
import au.com.dius.pact.consumer.PactVerification;
import au.com.dius.pact.consumer.dsl.PactDslJsonArray;
import au.com.dius.pact.consumer.dsl.PactDslWithProvider;
import au.com.dius.pact.model.PactFragment;
import models.GoodsType;
import models.softtech.SoftTechCategory;
import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import pact.PactConfig;
import play.libs.concurrent.HttpExecutionContext;
import play.libs.ws.WS;
import play.libs.ws.WSClient;
import uk.gov.bis.lite.controlcode.api.view.ControlCodeType;
import uk.gov.bis.lite.controlcode.api.view.GoodsRelationshipFullView;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ExecutionException;

public class GoodsRelationshipsServiceConsumerPact {
  private GoodsRelationshipsServiceClient client;
  private WSClient ws;

  private final static String CONTROL_ENTRY_HEADING = "software to develop other military software";
  private final static String CONTROL_ENTRY_QUESTION = "Is your software specially designed or modified for the development, production, operation or maintenance of other software with military applications?";
  private final static String CONTROL_ENTRY_CONTENT = "software for the test, inspection and production of telecommunications and information security systems and equipment";
  private final static String CONTROL_CODE = "ML1a";


  @Rule
  public PactProviderRule mockProvider = new PactProviderRule(PactConfig.CONTROL_CODE_SERVICE_PROVIDER, this);

  @Before
  public void setUp() throws Exception {
    ws = WS.newClient(mockProvider.getConfig().getPort());
    client = new GoodsRelationshipsServiceClient(new HttpExecutionContext(Runnable::run), ws, mockProvider.getConfig().url(), 10000);
  }

  @After
  public void tearDown() throws Exception {
    ws.close();
  }

  @Pact(provider = PactConfig.CONTROL_CODE_SERVICE_PROVIDER, consumer = PactConfig.CONSUMER)
  public PactFragment softwareRelatedToMilitarySoftwareExists(PactDslWithProvider builder) {
    PactDslJsonArray software = PactDslJsonArray.arrayMinLike(1,3)
        .stringType("controlType", ControlCodeType.SOFTWARE.toString())
        .stringType("relatedToControlType", ControlCodeType.SOFTWARE.toString())
        .stringType("controlEntryHeading", CONTROL_ENTRY_HEADING)
        .stringType("controlEntryQuestion", CONTROL_ENTRY_QUESTION)
        .stringType("controlEntryContent", CONTROL_ENTRY_CONTENT)
        .stringType("controlCode", CONTROL_CODE)
        .stringType("categoryType", SoftTechCategory.MILITARY.toString())
        .stringType("category", null)
        .closeObject()
        .asArray();

    Map<String, String> headers = new HashMap<>();
    headers.put("Content-Type", "application/json");

    return builder
        .given("software control code related to military software exists")
        .uponReceiving("a request for software control codes related to military software")
          .path("/goods-relationships/software/for/software/military")
          .method("GET")
          .willRespondWith()
            .status(200)
            .headers(headers)
            .body(software)
        .toFragment();
  }

  @Pact(provider = PactConfig.CONTROL_CODE_SERVICE_PROVIDER, consumer = PactConfig.CONSUMER)
  public PactFragment softwareRelatedToMilitarySoftwareDoNotExist(PactDslWithProvider builder) {
    Map<String, String> headers = new HashMap<>();
    headers.put("Content-Type", "application/json");

    return builder
        .given("software control code related to military software do not exist")
        .uponReceiving("a request for software control codes related to military software")
          .path("/goods-relationships/software/for/software/military")
          .method("GET")
          .willRespondWith()
            .status(200)
            .headers(headers)
            .body(new PactDslJsonArray())
        .toFragment();
  }

  @Pact(provider = PactConfig.CONTROL_CODE_SERVICE_PROVIDER, consumer = PactConfig.CONSUMER)
  public PactFragment softwareRelatedToMilitaryTechnologyExists(PactDslWithProvider builder) {
    PactDslJsonArray software = PactDslJsonArray.arrayMinLike(1,3)
        .stringType("controlType", ControlCodeType.SOFTWARE.toString())
        .stringType("relatedToControlType", ControlCodeType.TECHNOLOGY.toString())
        .stringType("controlEntryHeading", CONTROL_ENTRY_HEADING)
        .stringType("controlEntryQuestion", CONTROL_ENTRY_QUESTION)
        .stringType("controlEntryContent", CONTROL_ENTRY_CONTENT)
        .stringType("controlCode", CONTROL_CODE)
        .stringType("categoryType", SoftTechCategory.MILITARY.toString())
        .stringType("category", null)
        .closeObject()
        .asArray();

    Map<String, String> headers = new HashMap<>();
    headers.put("Content-Type", "application/json");

    return builder
        .given("software control code related to military technology exists")
        .uponReceiving("a request for software control codes related to military technology")
          .path("/goods-relationships/software/for/technology/military")
          .method("GET")
          .willRespondWith()
            .status(200)
            .headers(headers)
            .body(software)
        .toFragment();
  }

  @Pact(provider = PactConfig.CONTROL_CODE_SERVICE_PROVIDER, consumer = PactConfig.CONSUMER)
  public PactFragment softwareRelatedToMilitaryTechnologyDoNotExist(PactDslWithProvider builder) {
    Map<String, String> headers = new HashMap<>();
    headers.put("Content-Type", "application/json");

    return builder
        .given("software control code related to military technology do not exist")
        .uponReceiving("a request for software control codes related to military technology")
          .path("/goods-relationships/software/for/technology/military")
          .method("GET")
          .willRespondWith()
            .status(200)
            .headers(headers)
            .body(new PactDslJsonArray())
        .toFragment();
  }

  @Pact(provider = PactConfig.CONTROL_CODE_SERVICE_PROVIDER, consumer = PactConfig.CONSUMER)
  public PactFragment softwareRelatedToDualUseTelecomsSoftwareExists(PactDslWithProvider builder) {
    PactDslJsonArray software = PactDslJsonArray.arrayMinLike(1,3)
        .stringType("controlType", ControlCodeType.SOFTWARE.toString())
        .stringType("relatedToControlType", ControlCodeType.SOFTWARE.toString())
        .stringType("controlEntryHeading", CONTROL_ENTRY_HEADING)
        .stringType("controlEntryQuestion", CONTROL_ENTRY_QUESTION)
        .stringType("controlEntryContent", CONTROL_ENTRY_CONTENT)
        .stringType("controlCode", CONTROL_CODE)
        .stringType("categoryType", "DUAL_USE")
        .stringType("category", SoftTechCategory.TELECOMS.toUrlString())
        .closeObject()
        .asArray();

    Map<String, String> headers = new HashMap<>();
    headers.put("Content-Type", "application/json");

    return builder
        .given("software control code related to dual use telecoms software exists")
        .uponReceiving("a request for software control codes related to dual use telecoms software")
          .path("/goods-relationships/software/for/software/dual-use/telecoms")
          .method("GET")
          .willRespondWith()
            .status(200)
            .headers(headers)
            .body(software)
        .toFragment();
  }

  @Pact(provider = PactConfig.CONTROL_CODE_SERVICE_PROVIDER, consumer = PactConfig.CONSUMER)
  public PactFragment softwareRelatedToDualUseTelecomsSoftwareDoNotExist(PactDslWithProvider builder) {
    Map<String, String> headers = new HashMap<>();
    headers.put("Content-Type", "application/json");

    return builder
        .given("software control code related to dual use telecoms software do not exist")
        .uponReceiving("a request for software control codes related to dual use telecoms software")
        .path("/goods-relationships/software/for/software/dual-use/telecoms")
          .method("GET")
          .willRespondWith()
            .status(200)
            .headers(headers)
            .body(new PactDslJsonArray())
        .toFragment();
  }

  @Test
  @PactVerification(value = PactConfig.CONTROL_CODE_SERVICE_PROVIDER, fragment = "softwareRelatedToMilitarySoftwareExists")
  public void verifySoftwareForSoftwareExist() throws Exception {
    GoodsRelationshipsServiceResult result;
    try {
      result = client.get(GoodsType.SOFTWARE, GoodsType.SOFTWARE, SoftTechCategory.MILITARY).toCompletableFuture().get();
    }
    catch (InterruptedException |ExecutionException e) {
      throw new RuntimeException(e);
    }
    assertThat(result.relationshipsExist()).isTrue();
    assertThat(result.relationships.size()).isEqualTo(3);
    assertThat(result.isValidRelationshipIndex(0)).isTrue();
    GoodsRelationshipFullView relationship = result.getRelationship(0);
    assertThat(relationship).isNotNull();
    assertThat(relationship.getControlType()).isEqualTo(ControlCodeType.SOFTWARE);
    assertThat(relationship.getRelatedToControlType()).isEqualTo(ControlCodeType.SOFTWARE);
    assertThat(relationship.getControlEntryHeading()).isEqualTo(CONTROL_ENTRY_HEADING);
    assertThat(relationship.getControlEntryContent()).isEqualTo(CONTROL_ENTRY_CONTENT);
    assertThat(relationship.getControlCode()).isEqualTo(CONTROL_CODE);
  }

  @Test
  @PactVerification(value = PactConfig.CONTROL_CODE_SERVICE_PROVIDER, fragment = "softwareRelatedToMilitarySoftwareDoNotExist")
  public void verifySoftwareForSoftwareDoNotExist() throws Exception {
    GoodsRelationshipsServiceResult result;
    try {
      result = client.get(GoodsType.SOFTWARE, GoodsType.SOFTWARE, SoftTechCategory.MILITARY).toCompletableFuture().get();
    }
    catch (InterruptedException |ExecutionException e) {
      throw new RuntimeException(e);
    }
    assertThat(result.relationships.isEmpty()).isTrue();
    assertThat(result.relationshipsExist()).isFalse();
  }

  @Test
  @PactVerification(value = PactConfig.CONTROL_CODE_SERVICE_PROVIDER, fragment = "softwareRelatedToMilitaryTechnologyExists")
  public void verifySoftwareForTechnologyExist() throws Exception {
    GoodsRelationshipsServiceResult result;
    try {
      result = client.get(GoodsType.SOFTWARE, GoodsType.TECHNOLOGY, SoftTechCategory.MILITARY).toCompletableFuture().get();
    }
    catch (InterruptedException |ExecutionException e) {
      throw new RuntimeException(e);
    }
    assertThat(result.relationshipsExist()).isTrue();
    assertThat(result.relationships.size()).isEqualTo(3);
    assertThat(result.isValidRelationshipIndex(0)).isTrue();
    GoodsRelationshipFullView relationship = result.getRelationship(0);
    assertThat(relationship).isNotNull();
    assertThat(relationship.getControlType()).isEqualTo(ControlCodeType.SOFTWARE);
    assertThat(relationship.getRelatedToControlType()).isEqualTo(ControlCodeType.TECHNOLOGY);
    assertThat(relationship.getControlEntryHeading()).isEqualTo(CONTROL_ENTRY_HEADING);
    assertThat(relationship.getControlEntryContent()).isEqualTo(CONTROL_ENTRY_CONTENT);
    assertThat(relationship.getControlCode()).isEqualTo(CONTROL_CODE);
  }

  @Test
  @PactVerification(value = PactConfig.CONTROL_CODE_SERVICE_PROVIDER, fragment = "softwareRelatedToMilitaryTechnologyDoNotExist")
  public void verifySoftwareForTechnologyDoNotExist() throws Exception {
    GoodsRelationshipsServiceResult result;
    try {
      result = client.get(GoodsType.SOFTWARE, GoodsType.TECHNOLOGY, SoftTechCategory.MILITARY).toCompletableFuture().get();
    }
    catch (InterruptedException |ExecutionException e) {
      throw new RuntimeException(e);
    }
    assertThat(result.relationships.isEmpty()).isTrue();
    assertThat(result.relationshipsExist()).isFalse();
  }

  @Test
  @PactVerification(value = PactConfig.CONTROL_CODE_SERVICE_PROVIDER, fragment = "softwareRelatedToDualUseTelecomsSoftwareExists")
  public void verifySoftwareForDualUseTelecomsSoftwareExist() throws Exception {
    GoodsRelationshipsServiceResult result;
    try {
      result = client.get(GoodsType.SOFTWARE, GoodsType.SOFTWARE, SoftTechCategory.TELECOMS).toCompletableFuture().get();
    }
    catch (InterruptedException |ExecutionException e) {
      throw new RuntimeException(e);
    }
    assertThat(result.relationshipsExist()).isTrue();
    assertThat(result.relationships.size()).isEqualTo(3);
    assertThat(result.isValidRelationshipIndex(0)).isTrue();
    GoodsRelationshipFullView relationship = result.getRelationship(0);
    assertThat(relationship).isNotNull();
    assertThat(relationship.getControlType()).isEqualTo(ControlCodeType.SOFTWARE);
    assertThat(relationship.getRelatedToControlType()).isEqualTo(ControlCodeType.SOFTWARE);
    assertThat(relationship.getControlEntryHeading()).isEqualTo(CONTROL_ENTRY_HEADING);
    assertThat(relationship.getControlEntryContent()).isEqualTo(CONTROL_ENTRY_CONTENT);
    assertThat(relationship.getControlCode()).isEqualTo(CONTROL_CODE);
  }

  @Test
  @PactVerification(value = PactConfig.CONTROL_CODE_SERVICE_PROVIDER, fragment = "softwareRelatedToDualUseTelecomsSoftwareDoNotExist")
  public void verifySoftwareForDualUseTelecomsSoftwareDoNotExist() throws Exception {
    GoodsRelationshipsServiceResult result;
    try {
      result = client.get(GoodsType.SOFTWARE, GoodsType.SOFTWARE, SoftTechCategory.TELECOMS).toCompletableFuture().get();
    }
    catch (InterruptedException |ExecutionException e) {
      throw new RuntimeException(e);
    }
    assertThat(result.relationships.isEmpty()).isTrue();
    assertThat(result.relationshipsExist()).isFalse();
  }
}
