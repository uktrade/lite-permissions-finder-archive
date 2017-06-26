package models.summary;

import com.google.inject.Inject;
import com.google.inject.name.Named;
import components.common.cache.CountryProvider;
import components.common.state.ContextParamManager;
import components.persistence.PermissionsFinderDao;
import components.services.controlcode.frontend.FrontendServiceClient;
import components.services.controlcode.frontend.FrontendServiceResult;
import components.services.ogels.applicable.ApplicableOgelServiceClient;
import components.services.ogels.applicable.ApplicableOgelServiceResult;
import components.services.ogels.ogel.OgelServiceClient;
import controllers.ogel.OgelQuestionsController;
import controllers.routes;
import models.common.Country;
import org.apache.commons.lang3.StringUtils;
import play.libs.concurrent.HttpExecutionContext;
import uk.gov.bis.lite.ogel.api.view.OgelFullView;
import utils.CountryUtils;

import java.util.List;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;

public class SummaryServiceImpl implements SummaryService {
  private final ContextParamManager contextParamManager;
  private final PermissionsFinderDao permissionsFinderDao;
  private final HttpExecutionContext httpExecutionContext;
  private final FrontendServiceClient frontendServiceClient;
  private final OgelServiceClient ogelServiceClient;
  private final ApplicableOgelServiceClient applicableOgelServiceClient;
  private final CountryProvider countryProviderExport;

  @Inject
  public SummaryServiceImpl(ContextParamManager contextParamManager,
                            PermissionsFinderDao permissionsFinderDao,
                            HttpExecutionContext httpExecutionContext,
                            FrontendServiceClient frontendServiceClient,
                            OgelServiceClient ogelServiceClient,
                            ApplicableOgelServiceClient applicableOgelServiceClient,
                            @Named("countryProviderExport") CountryProvider countryProviderExport) {
    this.contextParamManager = contextParamManager;
    this.permissionsFinderDao = permissionsFinderDao;
    this.httpExecutionContext = httpExecutionContext;
    this.frontendServiceClient = frontendServiceClient;
    this.ogelServiceClient = ogelServiceClient;
    this.applicableOgelServiceClient = applicableOgelServiceClient;
    this.countryProviderExport = countryProviderExport;
  }

  @Override
  public CompletionStage<Summary> composeSummary() {
    String applicationCode = permissionsFinderDao.getApplicationCode();
    String controlCode = permissionsFinderDao.getControlCodeForRegistration();
    String ogelId = permissionsFinderDao.getOgelId();
    List<String> destinationCountries = CountryUtils.getDestinationCountries(
        permissionsFinderDao.getFinalDestinationCountry(), permissionsFinderDao.getThroughDestinationCountries());

    Summary newSummary = new Summary(applicationCode);

    // TODO Drive fields to shown by the journey history, not the dao
    CompletionStage<Summary> summaryCompletionStage = CompletableFuture.completedFuture(newSummary);

    if(StringUtils.isNoneBlank(controlCode)) {
      CompletionStage<FrontendServiceResult> frontendStage = frontendServiceClient.get(controlCode);

      summaryCompletionStage = summaryCompletionStage.thenCombineAsync(frontendStage, (summary, result)
          -> summary.addSummaryField(SummaryField.fromFrontendServiceResult(result,
          contextParamManager.addParamsToCall(controllers.routes.ChangeController.changeControlCode()))
      ), httpExecutionContext.current());
    }

    if (!destinationCountries.isEmpty()) {
      List<Country> sortedCountries = CountryUtils.getSortedCountries(countryProviderExport.getCountries());
      List<Country> filteredCountries = CountryUtils.getFilteredCountries(sortedCountries, destinationCountries);
      newSummary.addSummaryField(SummaryField.fromDestinationCountryList(filteredCountries, contextParamManager.addParamsToCall(routes.ChangeController.changeDestinationCountries())));
    }

    if (StringUtils.isNoneBlank(ogelId)) {
      String sourceCountry = permissionsFinderDao.getSourceCountry();

      CompletionStage<OgelFullView> ogelStage = ogelServiceClient.get(ogelId);

      Optional<OgelQuestionsController.OgelQuestionsForm> ogelQuestionsFormOptional =
          permissionsFinderDao.getOgelQuestionsForm();

      List<String> ogelActivities =
          OgelQuestionsController.OgelQuestionsForm.formToActivityTypes(ogelQuestionsFormOptional);

      boolean isGoodHistoric = OgelQuestionsController.OgelQuestionsForm.isGoodHistoric(ogelQuestionsFormOptional);

      CompletionStage<ApplicableOgelServiceResult> applicableOgelStage = applicableOgelServiceClient.get(
          controlCode, sourceCountry, destinationCountries, ogelActivities, isGoodHistoric);

      CompletionStage<Summary.ValidatedOgel> validatedStage = ogelStage.thenCombineAsync(applicableOgelStage,
          (ogelResult, applicableOgelResult) ->
              new Summary.ValidatedOgel(ogelResult, applicableOgelResult.findResultById(ogelResult.getId()).isPresent())
          , httpExecutionContext.current());

      summaryCompletionStage = summaryCompletionStage
          .thenCombineAsync(validatedStage, (summary, validatedOgel)
              -> summary.addSummaryField(SummaryField.fromOgelServiceResult(validatedOgel.ogelServiceResult,
              contextParamManager.addParamsToCall(routes.ChangeController.changeOgelType()), validatedOgel.isValid)
          ), httpExecutionContext.current());
    }

    return summaryCompletionStage;
  }
}
