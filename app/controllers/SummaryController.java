package controllers;

import static components.common.journey.JourneyManager.JOURNEY_SUPPRESS_BACK_LINK_CONTEXT_PARAM;
import static java.util.concurrent.CompletableFuture.completedFuture;
import static play.mvc.Controller.ctx;
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
  private final PermissionsFinderDao permissionsFinderDao;
  private final HttpExecutionContext httpExecutionContext;
  private final FrontendServiceClient frontendServiceClient;
  private final CountryProvider countryProviderExport;
  private final OgelServiceClient ogelServiceClient;
  private final ApplicableOgelServiceClient applicableOgelServiceClient;
  private final OgelRegistrationServiceClient ogelRegistrationServiceClient;

  @Inject
  public SummaryController(TransactionManager transactionManager,
                           ContextParamManager contextParamManager,
                           JourneyManager journeyManager,
                           FormFactory formFactory,
                           PermissionsFinderDao permissionsFinderDao,
                           HttpExecutionContext httpExecutionContext,
                           FrontendServiceClient frontendServiceClient,
                           @Named("countryProviderExport") CountryProvider countryProviderExport,
                           OgelServiceClient ogelServiceClient,
                           ApplicableOgelServiceClient applicableOgelServiceClient,
                           OgelRegistrationServiceClient ogelRegistrationServiceClient
  ) {
    this.transactionManager = transactionManager;
    this.contextParamManager = contextParamManager;
    this.journeyManager = journeyManager;
    this.formFactory = formFactory;
    this.permissionsFinderDao = permissionsFinderDao;
    this.httpExecutionContext = httpExecutionContext;
    this.frontendServiceClient = frontendServiceClient;
    this.countryProviderExport = countryProviderExport;
    this.ogelServiceClient = ogelServiceClient;
    this.applicableOgelServiceClient = applicableOgelServiceClient;
    this.ogelRegistrationServiceClient = ogelRegistrationServiceClient;
  }

  public CompletionStage<Result> renderForm() {
    // TODO remove the flag once back link handling is overhauled
    ctx().args.put(JOURNEY_SUPPRESS_BACK_LINK_CONTEXT_PARAM, true);
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
      if (journeyManager.isJourneySerialised(JourneyDefinitionNames.EXPORT)) {
        return journeyManager.restoreCurrentStage(JourneyDefinitionNames.EXPORT);
      }
      else {
        return journeyManager.startJourney(JourneyDefinitionNames.EXPORT);
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
    return Summary.composeSummary(contextParamManager, permissionsFinderDao, httpExecutionContext,
        frontendServiceClient, ogelServiceClient, applicableOgelServiceClient, countryProviderExport)
        .thenComposeAsync(summaryDetails -> completedFuture(ok(summary.render(form, summaryDetails, isResumedApplication))
        ), httpExecutionContext.current());
  }

  private CompletionStage<Result> redirectToRegistration() {
    return Summary.composeSummary(contextParamManager, permissionsFinderDao, httpExecutionContext,
        frontendServiceClient, ogelServiceClient, applicableOgelServiceClient, countryProviderExport)
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
