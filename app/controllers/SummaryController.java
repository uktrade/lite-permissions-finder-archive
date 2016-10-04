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
import journey.Events;
import models.summary.Summary;
import models.summary.SummaryField;
import models.summary.SummaryFieldType;
import org.apache.commons.lang3.StringUtils;
import play.data.Form;
import play.data.FormFactory;
import play.libs.concurrent.HttpExecutionContext;
import play.mvc.Result;
import views.html.summary;

import java.util.List;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;

public class SummaryController {

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
    return renderWithForm(formFactory.form(SummaryForm.class));
  }

  public CompletionStage<Result> handleSubmit() {
    Form<SummaryForm> form = formFactory.form(SummaryForm.class).bindFromRequest();
    if (form.hasErrors()){
      return renderWithForm(form);
    }

    Optional<String> actionOptional = form.get().action;
    Optional<String> changeOptional = form.get().change;

    if (changeOptional.isPresent()) {
      String change = changeOptional.get();
      Optional<SummaryFieldType> summaryFieldTypeOptional = SummaryFieldType.getMatchedByFieldValue(change);
      if (summaryFieldTypeOptional.isPresent()) {
        switch (summaryFieldTypeOptional.get()) {
          case CONTROL_CODE:
            return journeyManager.performTransition(Events.CHANGE_CONTROL_CODE);
          case OGEL_TYPE:
            return journeyManager.performTransition(Events.CHANGE_OGEL_TYPE);
          case DESTINATION_COUNTRIES:
            return journeyManager.performTransition(Events.CHANGE_DESTINATION_COUNTRIES);
          default:
            throw new RuntimeException("Unhandled member of SummaryFieldType enum \"" +
                summaryFieldTypeOptional.get().name() + "\"");
        }
      }
      else {
        return completedFuture(badRequest("Unknown value for change: \"" + change + "\""));
      }
    }
    else if (actionOptional.isPresent() && "register".equals(actionOptional.get())) {
      return redirectToRegistration();
    }
    else {
      return completedFuture(badRequest("Invalid form state"));
    }
  }

  public CompletionStage<Result> renderWithForm(Form<SummaryForm> form) {
    return composeSummary()
        .thenComposeAsync(s -> completedFuture(ok(summary.render(form, s))), httpExecutionContext.current())
        .handleAsync((result, error) -> error != null ? badRequest("Invalid service response") : result);
  }

  public CompletionStage<Summary> composeSummary() {
    String physicalGoodControlCode = permissionsFinderDao.getPhysicalGoodControlCode();
    List<String> destinationCountries = permissionsFinderDao.getThroughDestinationCountries();

    // Add "primary" country to the first position
    destinationCountries.add(0, permissionsFinderDao.getFinalDestinationCountry());

    String ogelId = permissionsFinderDao.getOgelId();

    // TODO Lookup whether stage is in history or not, this should drive the
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

  public static class SummaryForm {

    public Optional<String> action;

    public Optional<String> change;

  }
}
