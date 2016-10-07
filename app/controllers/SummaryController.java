package controllers;

import static java.util.concurrent.CompletableFuture.completedFuture;
import static play.mvc.Results.badRequest;
import static play.mvc.Results.ok;

import com.google.inject.Inject;
import components.common.client.CountryServiceClient;
import components.common.journey.JourneyManager;
import components.common.state.ContextParamManager;
import components.common.transaction.TransactionManager;
import components.persistence.PermissionsFinderDao;
import components.services.controlcode.frontend.FrontendServiceClient;
import components.services.ogels.ogel.OgelServiceClient;
import components.services.ogels.registration.OgelRegistrationServiceClient;
import journey.JourneyDefinitionNames;
import models.summary.Summary;
import org.apache.commons.lang3.StringUtils;
import play.Logger;
import play.data.Form;
import play.data.FormFactory;
import play.libs.concurrent.HttpExecutionContext;
import play.mvc.Result;
import views.html.summary;

import java.util.Optional;
import java.util.concurrent.CompletionStage;

public class SummaryController {

  private final TransactionManager transactionManager;
  private final ContextParamManager contextParamManager;
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
                           ContextParamManager contextParamManager,
                           JourneyManager journeyManager,
                           FormFactory formFactory,
                           PermissionsFinderDao permissionsFinderDao,
                           HttpExecutionContext httpExecutionContext,
                           FrontendServiceClient frontendServiceClient,
                           CountryServiceClient countryServiceClient,
                           OgelServiceClient ogelServiceClient,
                           OgelRegistrationServiceClient ogelRegistrationServiceClient
  ) {
    this.transactionManager = transactionManager;
    this.contextParamManager = contextParamManager;
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
    return renderWithForm(formFactory.form(SummaryForm.class), false);
  }

  public CompletionStage<Result> renderFormContinue() {
    return renderWithForm(formFactory.form(SummaryForm.class), true);
  }

  public CompletionStage<Result> handleSubmit() {
    return processSubmit(false);
  }

  public CompletionStage<Result> handleSubmitContinue() {
    return processSubmit(true);
  }

  public CompletionStage<Result> processSubmit(boolean isResumedApplication) {
    Form<SummaryForm> form = formFactory.form(SummaryForm.class).bindFromRequest();
    if (form.hasErrors()){
      return renderWithForm(form, isResumedApplication);
    }

    String action = form.get().action;

    if (isResumedApplication && StringUtils.equals("continue", action)) {
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

  public CompletionStage<Result> renderWithForm(Form<SummaryForm> form, boolean isResumedApplication) {
    return Summary.composeSummary(contextParamManager, permissionsFinderDao, httpExecutionContext,
        frontendServiceClient, countryServiceClient, ogelServiceClient)
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

  public CompletionStage<Result> redirectToRegistration() {
    Optional<Boolean> ogelTransactionExists = permissionsFinderDao.getOgelRegistrationServiceTransactionExists();
    String transactionId = transactionManager.getTransactionId();

    if (ogelTransactionExists.isPresent() && ogelTransactionExists.get()) {
      return ogelRegistrationServiceClient.updateTransaction(transactionId);
    }
    else {
      return ogelRegistrationServiceClient.createTransaction(transactionId);
    }
  }

  public static class SummaryForm {

    public String action;

  }

}
