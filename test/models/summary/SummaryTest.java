package models.summary;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.anyBoolean;
import static org.mockito.Mockito.anyList;
import static org.mockito.Mockito.anyString;
import static org.mockito.Mockito.when;

import com.fasterxml.jackson.databind.JsonNode;
import components.common.cache.CountryProvider;
import components.common.state.ContextParamManager;
import components.persistence.PermissionsFinderDao;
import components.services.controlcode.frontend.FrontendServiceClient;
import components.services.controlcode.frontend.FrontendServiceResult;
import components.services.ogels.applicable.ApplicableOgelServiceClient;
import components.services.ogels.applicable.ApplicableOgelServiceResult;
import components.services.ogels.ogel.OgelServiceClient;
import controllers.ogel.OgelQuestionsController;
import models.common.Country;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import play.libs.Json;
import play.libs.concurrent.HttpExecutionContext;
import play.mvc.Call;
import uk.gov.bis.lite.ogel.api.view.ApplicableOgelView;
import uk.gov.bis.lite.ogel.api.view.OgelFullView;

import java.util.Collections;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;

@RunWith(MockitoJUnitRunner.class)
public class SummaryTest {
  private static final String APPLICATION_CODE = "RV8K-ZR17";
  private static final String DESTINATION_COUNTRY = "CTRY3";
  private static final String SOURCE_COUNTRY = "CTRY0";
  private static final String CONTROL_CODE = "ML12b";
  private static final String OGEL_ID = "OGL8";

  @Mock
  private ContextParamManager cpm;
  @Mock
  private PermissionsFinderDao dao;
  @Mock
  private FrontendServiceClient frontendServiceClient;
  @Mock
  private OgelServiceClient ogelServiceClient;
  @Mock
  private ApplicableOgelServiceClient applicableOgelServiceClient;
  @Mock
  private CountryProvider countryProvider;

  @Before
  public void setUp() throws Exception {
    JsonNode frontendControlCodesJson = Json.parse(this.getClass().getClassLoader().getResourceAsStream("models/summary/frontend-control-codes.json"));
    FrontendServiceResult frontendServiceResult = new FrontendServiceResult(frontendControlCodesJson);

    JsonNode applicableOgelsJson = Json.parse(this.getClass().getClassLoader().getResourceAsStream("models/summary/applicable-ogels.json"));
    ApplicableOgelServiceResult applicableOgelServiceResult = new ApplicableOgelServiceResult(Json.fromJson(applicableOgelsJson, ApplicableOgelView[].class), false);

    JsonNode ogelsJson = Json.parse(this.getClass().getClassLoader().getResourceAsStream("models/summary/ogels.json"));
    OgelFullView ogelFullView = Json.fromJson(ogelsJson, OgelFullView.class);

    Country country = new Country() {
      @Override
      public String getCountryRef() {
        return DESTINATION_COUNTRY;
      }
    };

    OgelQuestionsController.OgelQuestionsForm ogelQuestionsForm = new OgelQuestionsController.OgelQuestionsForm();
    ogelQuestionsForm.before1897upto35k = "true";
    ogelQuestionsForm.forExhibitionDemonstration = "true";
    ogelQuestionsForm.forRepairReplacement = "true";

    when(cpm.addParamsToCall(any(Call.class))).thenReturn("http://some-url");
    when(dao.getApplicationCode()).thenReturn(APPLICATION_CODE);
    when(dao.getOgelId()).thenReturn(OGEL_ID);
    when(dao.getControlCodeForRegistration()).thenReturn(CONTROL_CODE);
    when(dao.getFinalDestinationCountry()).thenReturn(DESTINATION_COUNTRY);
    when(dao.getThroughDestinationCountries()).thenReturn(Collections.emptyList());
    when(dao.getSourceCountry()).thenReturn(SOURCE_COUNTRY);
    when(dao.getOgelQuestionsForm()).thenReturn(Optional.of(ogelQuestionsForm));

    when(frontendServiceClient.get(anyString())).thenReturn(CompletableFuture.completedFuture(frontendServiceResult));

    when(ogelServiceClient.get(anyString())).thenReturn(CompletableFuture.completedFuture(ogelFullView));

    when(applicableOgelServiceClient.get(anyString(), anyString(), anyList(), anyList(), anyBoolean()))
        .thenReturn(CompletableFuture.completedFuture(applicableOgelServiceResult));

    when(countryProvider.getCountries()).thenReturn(Collections.singletonList(country));
  }

  @Test
  public void summaryIsGenerated() throws Exception {
    Summary summary = Summary.composeSummary(cpm, dao, new HttpExecutionContext(Runnable::run), frontendServiceClient, ogelServiceClient, applicableOgelServiceClient, countryProvider).toCompletableFuture().get();
    assertThat(summary.applicationCode).isEqualTo(APPLICATION_CODE);
    assertThat(summary.isValid()).isTrue();
    assertThat(summary.summaryFields.isEmpty()).isFalse();
    assertThat(summary.summaryFields.size()).isEqualTo(3);
  }

  @Test
  public void ogelSummaryFieldIsGenerated() throws Exception {
    Summary summary = Summary.composeSummary(cpm, dao, new HttpExecutionContext(Runnable::run), frontendServiceClient, ogelServiceClient, applicableOgelServiceClient, countryProvider).toCompletableFuture().get();
    Optional<SummaryField> field = summary.findSummaryField(SummaryFieldType.OGEL_TYPE);
    assertThat(field.isPresent()).isTrue();
  }

  @Test
  public void destinationCountriesFieldIsGenerated() throws Exception {
    Summary summary = Summary.composeSummary(cpm, dao, new HttpExecutionContext(Runnable::run), frontendServiceClient, ogelServiceClient, applicableOgelServiceClient, countryProvider).toCompletableFuture().get();
    Optional<SummaryField> field = summary.findSummaryField(SummaryFieldType.DESTINATION_COUNTRIES);
    assertThat(field.isPresent()).isTrue();
  }

  @Test
  public void controlCodeFieldIsGenerated() throws Exception {
    Summary summary = Summary.composeSummary(cpm, dao, new HttpExecutionContext(Runnable::run), frontendServiceClient, ogelServiceClient, applicableOgelServiceClient, countryProvider).toCompletableFuture().get();
    Optional<SummaryField> field = summary.findSummaryField(SummaryFieldType.CONTROL_CODE);
    assertThat(field.isPresent()).isTrue();
  }

}
