package models.summary;

import components.common.client.CountryServiceClient;
import components.common.state.ContextParamManager;
import components.persistence.PermissionsFinderDao;
import components.services.controlcode.frontend.FrontendServiceClient;
import components.services.ogels.ogel.OgelServiceClient;
import controllers.routes;
import org.apache.commons.lang3.StringUtils;
import play.libs.concurrent.HttpExecutionContext;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;

public class Summary {

  public final List<SummaryField> summaryFields;

  public Summary() {
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
                                                        OgelServiceClient ogelServiceClient) {
    String physicalGoodControlCode = permissionsFinderDao.getPhysicalGoodControlCode();
    List<String> throughDestinationCountries = permissionsFinderDao.getThroughDestinationCountries();
    String finalDestinationCountry = permissionsFinderDao.getFinalDestinationCountry();
    String ogelId = permissionsFinderDao.getOgelId();

    List<String> destinationCountries = new ArrayList<>(throughDestinationCountries);
    if (StringUtils.isNoneBlank(finalDestinationCountry)) {
      destinationCountries.add(0, finalDestinationCountry);
    }

    // TODO Drive fields to shown by the journey history, not the dao
    CompletionStage<Summary> summaryCompletionStage = CompletableFuture.completedFuture(new Summary());

    if(StringUtils.isNoneBlank(physicalGoodControlCode)) {
      CompletionStage<FrontendServiceClient.Response> frontendStage = frontendServiceClient.get(physicalGoodControlCode);

      summaryCompletionStage = summaryCompletionStage.thenCombineAsync(frontendStage, (summary, response)
          -> summary.addSummaryField(SummaryField.fromFrontendServiceResult(response.getFrontendServiceResult(),
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
      CompletionStage<OgelServiceClient.Response> ogelStage = ogelServiceClient.get(ogelId);

      summaryCompletionStage = summaryCompletionStage
          .thenCombineAsync(ogelStage, (summary, response)
              -> summary.addSummaryField(SummaryField.fromOgelServiceResult(response.getResult(),
              contextParamManager.addParamsToCall(routes.ChangeController.changeOgelType()))
          ), httpExecutionContext.current());
    }

    return summaryCompletionStage;
  }
  
}
