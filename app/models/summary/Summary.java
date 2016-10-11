package models.summary;

import components.common.client.CountryServiceClient;
import components.common.state.ContextParamManager;
import components.persistence.PermissionsFinderDao;
import components.services.controlcode.frontend.FrontendServiceClient;
import components.services.controlcode.frontend.FrontendServiceResult;
import components.services.ogels.applicable.ApplicableOgelServiceClient;
import components.services.ogels.applicable.ApplicableOgelServiceResult;
import components.services.ogels.ogel.OgelServiceClient;
import components.services.ogels.ogel.OgelServiceResult;
import controllers.ogel.OgelQuestionsController;
import controllers.routes;
import org.apache.commons.lang3.StringUtils;
import play.libs.concurrent.HttpExecutionContext;
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

  public static CompletionStage<Summary> composeSummary(ContextParamManager contextParamManager,
                                                        PermissionsFinderDao permissionsFinderDao,
                                                        HttpExecutionContext httpExecutionContext,
                                                        FrontendServiceClient frontendServiceClient,
                                                        CountryServiceClient countryServiceClient,
                                                        OgelServiceClient ogelServiceClient,
                                                        ApplicableOgelServiceClient applicableOgelServiceClient) {
    String applicationCode = permissionsFinderDao.getApplicationCode();
    String physicalGoodControlCode = permissionsFinderDao.getPhysicalGoodControlCode();
    String ogelId = permissionsFinderDao.getOgelId();
    List<String> destinationCountries = CountryUtils.getDestinationCountries(
        permissionsFinderDao.getFinalDestinationCountry(), permissionsFinderDao.getThroughDestinationCountries());

    // TODO Drive fields to shown by the journey history, not the dao
    CompletionStage<Summary> summaryCompletionStage = CompletableFuture.completedFuture(new Summary(applicationCode));

    if(StringUtils.isNoneBlank(physicalGoodControlCode)) {
      CompletionStage<FrontendServiceResult> frontendStage = frontendServiceClient.get(physicalGoodControlCode);

      summaryCompletionStage = summaryCompletionStage.thenCombineAsync(frontendStage, (summary, result)
          -> summary.addSummaryField(SummaryField.fromFrontendServiceResult(result,
          contextParamManager.addParamsToCall(routes.ChangeController.changeControlCode()))
      ), httpExecutionContext.current());
    }

    if (destinationCountries.size() > 0) {
      CompletionStage<CountryServiceClient.CountryServiceResponse> countryStage = countryServiceClient.getCountries();

      summaryCompletionStage = summaryCompletionStage.thenCombineAsync(countryStage, (summary, response)
          -> summary.addSummaryField(SummaryField.fromDestinationCountryList(response.getCountriesByRef(destinationCountries),
          contextParamManager.addParamsToCall(routes.ChangeController.changeDestinationCountries()))
      ), httpExecutionContext.current());
    }

    if (StringUtils.isNoneBlank(ogelId)) {
      String sourceCountry = permissionsFinderDao.getSourceCountry();

      CompletionStage<OgelServiceClient.Response> ogelStage = ogelServiceClient.get(ogelId);

      List<String> ogelActivities = OgelQuestionsController.OgelQuestionsForm.formToActivityTypes(permissionsFinderDao.getOgelQuestionsForm());

      CompletionStage<ApplicableOgelServiceResult> applicableOgelStage = applicableOgelServiceClient.get(
          physicalGoodControlCode, sourceCountry, destinationCountries, ogelActivities);

      CompletionStage<ValidatedOgel> validatedStage = ogelStage.thenCombine(applicableOgelStage,
          (ogelResponse, applicableOgelResponse) ->
              new ValidatedOgel(ogelResponse.getResult(),
                  applicableOgelResponse.findResultById(ogelResponse.getResult().id).isPresent()));

      summaryCompletionStage = summaryCompletionStage
          .thenCombineAsync(validatedStage, (summary, validatedOgel)
              -> summary.addSummaryField(SummaryField.fromOgelServiceResult(validatedOgel.ogelServiceResult,
              contextParamManager.addParamsToCall(routes.ChangeController.changeOgelType()), validatedOgel.isValid)
          ), httpExecutionContext.current());
    }

    return summaryCompletionStage;
  }

  private static class ValidatedOgel {
    public final OgelServiceResult ogelServiceResult;
    public final boolean isValid;

    public ValidatedOgel(OgelServiceResult ogelServiceResult, boolean isValid) {
      this.ogelServiceResult = ogelServiceResult;
      this.isValid = isValid;
    }
  }
  
}
