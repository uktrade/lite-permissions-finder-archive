package controllers.ogel;

import static play.mvc.Results.ok;

import com.google.inject.Inject;
import components.common.journey.JourneyManager;
import components.common.state.ContextParamManager;
import components.persistence.PermissionsFinderDao;
import components.services.ogels.conditions.OgelConditionsServiceClient;
import components.services.ogels.ogel.OgelServiceClient;
import exceptions.BusinessRuleException;
import exceptions.FormStateException;
import journey.Events;
import play.data.Form;
import play.data.FormFactory;
import play.libs.concurrent.HttpExecutionContext;
import play.mvc.Result;
import views.html.ogel.ogelSummary;

import java.util.concurrent.CompletionStage;
import java.util.function.Function;

public class OgelSummaryController {

  private final JourneyManager journeyManager;
  private final FormFactory formFactory;
  private final PermissionsFinderDao permissionsFinderDao;
  private final HttpExecutionContext httpExecutionContext;
  private final OgelServiceClient ogelServiceClient;
  private final OgelConditionsServiceClient ogelConditionsServiceClient;
  private final ContextParamManager contextParamManager;

  @Inject
  public OgelSummaryController(JourneyManager journeyManager,
                               FormFactory formFactory,
                               PermissionsFinderDao permissionsFinderDao,
                               HttpExecutionContext httpExecutionContext,
                               OgelServiceClient ogelServiceClient,
                               OgelConditionsServiceClient ogelConditionsServiceClient,
                               ContextParamManager contextParamManager) {
    this.journeyManager = journeyManager;
    this.formFactory = formFactory;
    this.permissionsFinderDao = permissionsFinderDao;
    this.httpExecutionContext = httpExecutionContext;
    this.ogelServiceClient = ogelServiceClient;
    this.ogelConditionsServiceClient = ogelConditionsServiceClient;
    this.contextParamManager = contextParamManager;
  }

  public CompletionStage<Result> renderForm() {
    return renderWithForm(formFactory.form(OgelSummaryForm.class));
  }

  public CompletionStage<Result> handleSubmit() {
    Form<OgelSummaryForm> form = formFactory.form(OgelSummaryForm.class).bindFromRequest();
    if (form.hasErrors()) {
      return renderWithForm(form);
    }
    String action = form.get().action;
    return ogelConditionsServiceClient.get(permissionsFinderDao.getOgelId(),
        permissionsFinderDao.getControlCodeForRegistration())
        .thenApplyAsync(conditionsResult -> {
          if ("register".equals(action)) {
            if (conditionsResult.isEmpty) {
              // No conditions apply
              return contextParamManager.addParamsAndRedirect(controllers.routes.SummaryController.renderForm());
            }
            else if (conditionsResult.isMissingControlCodes) {
              throw new BusinessRuleException("Can not apply for OGEL with missing control codes");
            }
            else if (!conditionsResult.isEmpty && OgelConditionsServiceClient.isItemAllowed(conditionsResult,
                permissionsFinderDao.getOgelConditionsApply().get())) {
              // ogelConditionsApply question should have been answered if this is the case
              return contextParamManager.addParamsAndRedirect(controllers.routes.SummaryController.renderForm());
            }
            else {
              // Should not be able to register when the item is not allowed
              throw new BusinessRuleException("Can not register for OGEL with applicable conditions");
            }
          }
          else if ("chooseAgain".equals(action)) {
            return journeyManager.performTransition(Events.OGEL_CHOOSE_AGAIN);
          }
          else {
            throw new FormStateException("Unhandled form state");
          }
        }, httpExecutionContext.current())
        .thenCompose(Function.identity());
  }

  public CompletionStage<Result> renderWithForm(Form<OgelSummaryForm> form) {
    String ogelId = permissionsFinderDao.getOgelId();
    String controlCode = permissionsFinderDao.getControlCodeForRegistration();

    return ogelConditionsServiceClient.get(ogelId, controlCode)
        .thenApplyAsync(conditionsResult -> {
          return ogelServiceClient.get(permissionsFinderDao.getOgelId())
              .thenApplyAsync(ogelResult -> {
                // True when no restriction service result, otherwise check with isItemAllowed.
                // Assume getOgelConditionsApply is empty if there is no result from the OGEL condition service or the re are missing control codes
                boolean allowedToProceed = conditionsResult.isEmpty || (!conditionsResult.isMissingControlCodes
                    && OgelConditionsServiceClient.isItemAllowed(conditionsResult, permissionsFinderDao.getOgelConditionsApply().get()));

                return ok(ogelSummary.render(form, ogelResult, controlCode, allowedToProceed));
              }, httpExecutionContext.current());
        }, httpExecutionContext.current())
        .thenCompose(Function.identity());
  }

  public static class OgelSummaryForm {

    public String action;

  }

}
