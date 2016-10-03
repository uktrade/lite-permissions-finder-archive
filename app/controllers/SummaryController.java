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
import play.data.Form;
import play.data.FormFactory;
import play.libs.concurrent.HttpExecutionContext;
import play.mvc.Result;
import views.html.summary;

import java.util.List;
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
      renderWithForm(form);
    }
    String action = form.get().action;
    if ("changeGoodsRating".equals(action)) {
      return journeyManager.performTransition(Events.CHANGE_CONTROL_CODE);
    }
    else if ("changeLicenceType".equals(action)) {
      return journeyManager.performTransition(Events.CHANGE_OGEL_TYPE);
    }
    else if ("changeDestinationCountries".equals(action)) {
      return journeyManager.performTransition(Events.CHANGE_DESTINATION_COUNTRIES);
    }
    else if ("register".equals(action)) {
      return redirectToRegistration();
    }
    else {
      return completedFuture(badRequest("Unknown value for action: \"" + action + "\""));
    }
  }

  public CompletionStage<Result> renderWithForm(Form<SummaryForm> form) {
    String physicalGoodControlCode = permissionsFinderDao.getPhysicalGoodControlCode();
    List<String> destinationCountries = permissionsFinderDao.getThroughDestinationCountries();

    // Add "primary" country to the first position
    destinationCountries.add(0, permissionsFinderDao.getFinalDestinationCountry());

    String ogelId = permissionsFinderDao.getOgelId();

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
          // TODO Refactor duplicate code out of here and redirectToRegistration
          return completedFuture(ok(summary.render(form, frontendServiceResponse.getFrontendServiceResult().controlCodeData,
              countryServiceResponse.getCountriesByRef(destinationCountries), ogelServiceResponse.getResult())));
        }, httpExecutionContext.current());
      }, httpExecutionContext.current());
    }, httpExecutionContext.current());
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

    public String action;

  }
}
