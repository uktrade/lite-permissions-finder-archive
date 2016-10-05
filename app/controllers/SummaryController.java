package controllers;

import static java.util.concurrent.CompletableFuture.completedFuture;
import static play.mvc.Results.badRequest;
import static play.mvc.Results.ok;

import com.google.inject.Inject;
import components.common.client.CountryServiceClient;
import components.common.journey.JourneyManager;
import components.common.transaction.TransactionManager;
import components.persistence.PermissionsFinderDao;
import components.services.controlcode.frontend.FrontendServiceClient;
import components.services.ogels.ogel.OgelServiceClient;
import components.services.ogels.registration.OgelRegistrationServiceClient;
import journey.JourneyDefinitionNames;
import models.summary.Summary;
import models.summary.SummaryField;
import models.summary.SummaryFieldType;
import org.apache.commons.lang3.StringUtils;
import play.Logger;
import play.data.DynamicForm;
import play.data.FormFactory;
import play.libs.concurrent.HttpExecutionContext;
import play.mvc.Result;
import views.html.summary;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;

public class SummaryController {

  private static final String ACTION_FIELD = "action";
  private static final String CHANGE_FIELD = "change";

  private final TransactionManager transactionManager;
  private final JourneyManager journeyManager;
  private final FormFactory formFactory;
  private final PermissionsFinderDao permissionsFinderDao;
  private final HttpExecutionContext httpExecutionContext;
  private final FrontendServiceClient frontendServiceClient;
  private final CountryServiceClient countryServiceClient;
  private final OgelServiceClient ogelServiceClient;
  private final OgelRegistrationServiceClient ogelRegistrationServiceClient;

  @Inject
  public SummaryController(TransactionManager transactionManager,
                           JourneyManager journeyManager,
                           FormFactory formFactory,
                           PermissionsFinderDao permissionsFinderDao,
                           HttpExecutionContext httpExecutionContext,
                           FrontendServiceClient frontendServiceClient,
                           CountryServiceClient countryServiceClient,
                           OgelServiceClient ogelServiceClient,
                           OgelRegistrationServiceClient ogelRegistrationServiceClient) {
    this.transactionManager = transactionManager;
    this.journeyManager = journeyManager;
    this.formFactory = formFactory;
    this.permissionsFinderDao = permissionsFinderDao;
    this.httpExecutionContext = httpExecutionContext;
    this.frontendServiceClient = frontendServiceClient;
    this.countryServiceClient = countryServiceClient;
    this.ogelServiceClient = ogelServiceClient;
    this.ogelRegistrationServiceClient = ogelRegistrationServiceClient;
  }

  public CompletionStage<Result> renderForm() {
    return renderWithForm(formFactory.form(), false);
  }

  public CompletionStage<Result> renderFormContinue() {
    return renderWithForm(formFactory.form(), true);
  }

  public CompletionStage<Result> handleSubmit() {
    return processSubmit(false);
  }

  public CompletionStage<Result> handleSubmitContinue() {
    return processSubmit(true);
  }

  public CompletionStage<Result> processSubmit(boolean isResumedApplication) {
    DynamicForm form = formFactory.form().bindFromRequest();
    if (form.hasErrors()){
      return renderWithForm(form, isResumedApplication);
    }

    String action = form.field(ACTION_FIELD).valueOr(null);
    String change = form.field(CHANGE_FIELD).valueOr(null);

    if (StringUtils.isNoneBlank(change)) {
      Optional<SummaryFieldType> summaryFieldTypeOptional = SummaryFieldType.getMatchedByFieldValue(change);
      if (summaryFieldTypeOptional.isPresent()) {
        switch (summaryFieldTypeOptional.get()) {
          case CONTROL_CODE:
            return journeyManager.startJourney(JourneyDefinitionNames.CHANGE_CONTROL_CODE);
          case OGEL_TYPE:
            return journeyManager.startJourney(JourneyDefinitionNames.CHANGE_OGEL_TYPE);
          case DESTINATION_COUNTRIES:
            return journeyManager.startJourney(JourneyDefinitionNames.CHANGE_DESTINATION_COUNTRIES);
          default:
            throw new RuntimeException("Unhandled member of SummaryFieldType enum \"" +
                summaryFieldTypeOptional.get().name() + "\"");
        }
      }
      else {
        return completedFuture(badRequest("Unknown value for change: \"" + change + "\""));
      }
    }
    else if (isResumedApplication && StringUtils.equals("continue", action)) {
      if (journeyManager.isJourneySerialised()) {
        return journeyManager.restoreCurrentStage();
      }
      else {
        return journeyManager.startJourney(JourneyDefinitionNames.DEFAULT);
      }
    }
    else if (!isResumedApplication && StringUtils.equals("register", action)) {
      return redirectToRegistration();
    }
    else {
      return completedFuture(badRequest("Invalid form state"));
    }
  }

  public CompletionStage<Result> renderWithForm(DynamicForm form, boolean isResumedApplication) {
    return composeSummary()
        .thenComposeAsync(summaryDetails -> completedFuture(ok(summary.render(form, summaryDetails, isResumedApplication))
        ), httpExecutionContext.current())
        .handleAsync((result, error) -> {
          if (error != null) {
            Logger.error(error.getMessage(), error);
            return badRequest("Invalid service response");
          }
          else {
            return result;
          }
        });
  }

  public CompletionStage<Summary> composeSummary() {
    String physicalGoodControlCode = permissionsFinderDao.getPhysicalGoodControlCode();
    List<String> throughDestinationCountries = permissionsFinderDao.getThroughDestinationCountries();
    String finalDestinationCountry = permissionsFinderDao.getFinalDestinationCountry();
    String ogelId = permissionsFinderDao.getOgelId();

    List<String> destinationCountries = new ArrayList<>(throughDestinationCountries);
    if (StringUtils.isNoneBlank(finalDestinationCountry)) {
      destinationCountries.add(0, finalDestinationCountry);
    }

    // TODO Drive fields to show by the journey history, not the dao
    CompletionStage<Summary> summaryCompletionStage = CompletableFuture.completedFuture(new Summary());

    if(StringUtils.isNoneBlank(physicalGoodControlCode)) {
      CompletionStage<FrontendServiceClient.Response> frontendStage = frontendServiceClient.get(physicalGoodControlCode);
      summaryCompletionStage = summaryCompletionStage.thenCombineAsync(frontendStage, (summary, response)
          -> summary.addSummaryField(SummaryField.fromFrontendServiceResult(response.getFrontendServiceResult())
      ), httpExecutionContext.current());
    }

    if (destinationCountries.size() > 0) {
      CompletionStage<CountryServiceClient.CountryServiceResponse> countryStage = countryServiceClient.getCountries();
      summaryCompletionStage = summaryCompletionStage.thenCombineAsync(countryStage, (summary, response)
          -> summary.addSummaryField(SummaryField.fromDestinationCountryList(response.getCountriesByRef(destinationCountries))
      ), httpExecutionContext.current());
    }

    if (StringUtils.isNoneBlank(ogelId)) {
      CompletionStage<OgelServiceClient.Response> ogelStage = ogelServiceClient.get(ogelId);
      summaryCompletionStage = summaryCompletionStage.thenCombineAsync(ogelStage, (summary, response)
          -> summary.addSummaryField(SummaryField.fromOgelServiceResult(response.getResult())
      ), httpExecutionContext.current());
    }

    return summaryCompletionStage;
  }

  public CompletionStage<Result> redirectToRegistration() {
    String physicalGoodControlCode = permissionsFinderDao.getPhysicalGoodControlCode();
    List<String> destinationCountries = permissionsFinderDao.getThroughDestinationCountries();

    // Add "primary" country to the first position
    destinationCountries.add(0, permissionsFinderDao.getFinalDestinationCountry());

    String ogelId = permissionsFinderDao.getOgelId();
    String transactionId = transactionManager.getTransactionId();

    return frontendServiceClient.get(physicalGoodControlCode).thenComposeAsync(frontendServiceResponse -> {
      if (!frontendServiceResponse.isOk()) {
        return completedFuture(badRequest("Bad control code front end service response"));
      }
      return countryServiceClient.getCountries().thenComposeAsync(countryServiceResponse -> {
        if (!countryServiceResponse.isOk()) {
          return completedFuture(badRequest("Bad country service response"));
        }
        return ogelServiceClient.get(ogelId).thenComposeAsync(ogelServiceResponse -> {
          if (!ogelServiceResponse.isOk()) {
            return completedFuture(badRequest("Bad OGEL service response"));
          }
          return ogelRegistrationServiceClient.handOffToOgelRegistration(transactionId, ogelServiceResponse.getResult(),
              countryServiceResponse.getCountriesByRef(destinationCountries),
              frontendServiceResponse.getFrontendServiceResult().controlCodeData);
        }, httpExecutionContext.current());
      }, httpExecutionContext.current());
    }, httpExecutionContext.current());
  }

}
