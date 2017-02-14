package models.summary;

import components.common.cache.CountryProvider;
import components.common.state.ContextParamManager;
import components.persistence.PermissionsFinderDao;
import components.services.controlcode.FrontendServiceClient;
import components.services.controlcode.FrontendServiceResult;
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

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;

public class Summary {

  public final String applicationCode;

  public final List<SummaryField> summaryFields;

  public Summary(String applicationCode) {
    this.applicationCode = applicationCode;
    this.summaryFields = new ArrayList<>();
  }

  public Summary addSummaryField(SummaryField summaryField){
    this.summaryFields.add(summaryField);
    return this;
  }

  public Optional<SummaryField> findSummaryField(SummaryFieldType summaryFieldType) {
    if (!summaryFields.isEmpty()) {
      return summaryFields.stream().filter(field -> field.summaryFieldType == summaryFieldType).findFirst();
    }
    else {
      return Optional.empty();
    }
  }

  public boolean isValid() {
    return this.summaryFields.stream().allMatch(f -> f.isValid);
  }

  public static CompletionStage<Summary> composeSummary(ContextParamManager contextParamManager,
                                                        PermissionsFinderDao permissionsFinderDao,
                                                        HttpExecutionContext httpExecutionContext,
                                                        FrontendServiceClient frontendServiceClient,
                                                        OgelServiceClient ogelServiceClient,
                                                        ApplicableOgelServiceClient applicableOgelServiceClient,
                                                        CountryProvider countryProviderExport) {
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
          contextParamManager.addParamsToCall(routes.ChangeController.changeControlCode()))
      ), httpExecutionContext.current());
    }

    if (destinationCountries.size() > 0) {
      List<Country> countries = CountryUtils.getFilteredCountries(new ArrayList<>(countryProviderExport.getCountries()), destinationCountries);
      newSummary.addSummaryField(SummaryField.fromDestinationCountryList(countries, contextParamManager.addParamsToCall(routes.ChangeController.changeDestinationCountries())));
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

      CompletionStage<ValidatedOgel> validatedStage = ogelStage.thenCombine(applicableOgelStage,
          (ogelResult, applicableOgelResult) ->
              new ValidatedOgel(ogelResult, applicableOgelResult.findResultById(ogelResult.getId()).isPresent()));

      summaryCompletionStage = summaryCompletionStage
          .thenCombineAsync(validatedStage, (summary, validatedOgel)
              -> summary.addSummaryField(SummaryField.fromOgelServiceResult(validatedOgel.ogelServiceResult,
              contextParamManager.addParamsToCall(routes.ChangeController.changeOgelType()), validatedOgel.isValid)
          ), httpExecutionContext.current());
    }

    return summaryCompletionStage;
  }

  private static class ValidatedOgel {
    public final OgelFullView ogelServiceResult;
    public final boolean isValid;

    public ValidatedOgel(OgelFullView ogelServiceResult, boolean isValid) {
      this.ogelServiceResult = ogelServiceResult;
      this.isValid = isValid;
    }
  }

}
