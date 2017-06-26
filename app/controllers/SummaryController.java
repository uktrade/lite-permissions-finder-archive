package controllers;

import static java.util.concurrent.CompletableFuture.completedFuture;
import static play.mvc.Results.ok;

import com.google.inject.Inject;
import components.common.cache.CountryProvider;
import components.common.journey.JourneyManager;
import components.common.state.ContextParamManager;
import components.common.transaction.TransactionManager;
import components.persistence.PermissionsFinderDao;
import components.services.controlcode.frontend.FrontendServiceClient;
import components.services.ogels.applicable.ApplicableOgelServiceClient;
import components.services.ogels.ogel.OgelServiceClient;
import components.services.registration.OgelRegistrationServiceClient;
import exceptions.FormStateException;
import journey.JourneyDefinitionNames;
import models.summary.Summary;
import models.summary.SummaryService;
import org.apache.commons.lang3.StringUtils;
import play.data.Form;
import play.data.FormFactory;
import play.libs.concurrent.HttpExecutionContext;
import play.mvc.Result;
import views.html.summary;

import java.util.concurrent.CompletionStage;

import javax.inject.Named;

public class SummaryController {

  private final TransactionManager transactionManager;
  private final ContextParamManager contextParamManager;
  private final JourneyManager journeyManager;
  private final FormFactory formFactory;
  private final HttpExecutionContext httpExecutionContext;
  private final OgelRegistrationServiceClient ogelRegistrationServiceClient;
  private final SummaryService summaryService;

  @Inject
  public SummaryController(TransactionManager transactionManager,
                           ContextParamManager contextParamManager,
                           JourneyManager journeyManager,
                           FormFactory formFactory,
                           HttpExecutionContext httpExecutionContext,
                           OgelRegistrationServiceClient ogelRegistrationServiceClient,
                           SummaryService summaryService
  ) {
    this.transactionManager = transactionManager;
    this.contextParamManager = contextParamManager;
    this.journeyManager = journeyManager;
    this.formFactory = formFactory;
    this.httpExecutionContext = httpExecutionContext;
    this.ogelRegistrationServiceClient = ogelRegistrationServiceClient;
    this.summaryService = summaryService;
  }

  public CompletionStage<Result> renderForm() {
    journeyManager.hideBackLink();
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
      // The journeyName to check for is not known, check and restore whatever is saved
      if (journeyManager.isJourneySerialised(null)) {
        return journeyManager.restoreCurrentStage(null);
      }
      else {
        // If there is no journey yet, start at the trade types page as in ContinueApplicationController
        return contextParamManager.addParamsAndRedirect(routes.TradeTypeController.renderForm());
      }
    }
    else if (!isResumedApplication && StringUtils.equals("register", action)) {
      return redirectToRegistration();
    }
    else {
      throw new FormStateException("Unhandled form state");
    }
  }

  public CompletionStage<Result> renderWithForm(Form<SummaryForm> form, boolean isResumedApplication) {
    return summaryService.composeSummary()
        .thenComposeAsync(summaryDetails -> completedFuture(ok(summary.render(form, summaryDetails, isResumedApplication))
        ), httpExecutionContext.current());
  }

  private CompletionStage<Result> redirectToRegistration() {
    return summaryService.composeSummary()
        .thenComposeAsync(summary -> {
          if (summary.isValid()) {
            String transactionId = transactionManager.getTransactionId();
            return ogelRegistrationServiceClient.updateTransactionAndRedirect(transactionId);
          }
          else {
            throw new RuntimeException("Summary invalid, cannot redirect to registration");
          }
        }, httpExecutionContext.current());
  }

  public static class SummaryForm {

    public String action;

  }

}
