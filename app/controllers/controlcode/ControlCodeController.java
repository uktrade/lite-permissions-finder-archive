package controllers.controlcode;

import static java.util.concurrent.CompletableFuture.completedFuture;

import com.google.inject.Inject;
import components.common.journey.JourneyManager;
import components.persistence.PermissionsFinderDao;
import components.services.controlcode.ControlCodeData;
import components.services.controlcode.FrontendServiceClient;
import components.services.controlcode.FrontendServiceResult;
import exceptions.FormStateException;
import journey.Events;
import journey.helpers.ControlCodeJourneyHelper;
import journey.helpers.SoftTechJourneyHelper;
import models.ControlCodeFlowStage;
import models.controlcode.ControlCodeDisplay;
import models.controlcode.ControlCodeJourney;
import play.data.Form;
import play.data.FormFactory;
import play.data.validation.Constraints.Required;
import play.libs.concurrent.HttpExecutionContext;
import play.mvc.Controller;
import play.mvc.Result;
import views.html.controlcode.controlCode;

import java.util.Optional;
import java.util.concurrent.CompletionStage;
import java.util.function.Function;

public class ControlCodeController extends Controller {

  private final JourneyManager journeyManager;
  private final FormFactory formFactory;
  private final PermissionsFinderDao permissionsFinderDao;
  private final HttpExecutionContext httpExecutionContext;
  private final FrontendServiceClient frontendServiceClient;
  private final ControlCodeJourneyHelper controlCodeJourneyHelper;

  @Inject
  public ControlCodeController(JourneyManager journeyManager,
                               FormFactory formFactory,
                               PermissionsFinderDao permissionsFinderDao,
                               HttpExecutionContext httpExecutionContext,
                               FrontendServiceClient frontendServiceClient,
                               ControlCodeJourneyHelper controlCodeJourneyHelper) {
    this.journeyManager = journeyManager;
    this.formFactory = formFactory;
    this.permissionsFinderDao = permissionsFinderDao;
    this.httpExecutionContext = httpExecutionContext;
    this.frontendServiceClient = frontendServiceClient;
    this.controlCodeJourneyHelper = controlCodeJourneyHelper;
  }


  private CompletionStage<Result> renderForm(ControlCodeJourney controlCodeJourney) {
    Optional<Boolean> controlCodeApplies = permissionsFinderDao.getControlCodeApplies(controlCodeJourney);
    ControlCodeForm templateForm = new ControlCodeForm();
    templateForm.couldDescribeItems = controlCodeApplies.isPresent() ? controlCodeApplies.get().toString() : "";
    return frontendServiceClient.get(permissionsFinderDao.getSelectedControlCode(controlCodeJourney))
        .thenApplyAsync(result ->
            ok(controlCode.render(formFactory.form(ControlCodeForm.class).fill(templateForm),
                new ControlCodeDisplay(controlCodeJourney, result))), httpExecutionContext.current());
  }

  public CompletionStage<Result> renderSearchForm() {
    return renderForm(ControlCodeJourney.PHYSICAL_GOODS_SEARCH);
  }

  public CompletionStage<Result> renderSearchRelatedToForm (String goodsTypeText) {
    return SoftTechJourneyHelper.validateGoodsTypeTextThenContinue(goodsTypeText, this::renderForm);
  }

  public CompletionStage<Result> renderSoftwareControlsForm() {
    return renderForm(ControlCodeJourney.SOFTWARE_CONTROLS);
  }

  public CompletionStage<Result> renderRelatedSoftwareControlsForm() {
    return renderForm(ControlCodeJourney.SOFTWARE_CONTROLS_RELATED_TO_A_PHYSICAL_GOOD);
  }

  public CompletionStage<Result> renderSoftwareCatchallControlsForm() {
    return renderForm(ControlCodeJourney.SOFTWARE_CATCHALL_CONTROLS);
  }

  private CompletionStage<Result> handleSubmit(ControlCodeJourney controlCodeJourney) {
    Form<ControlCodeForm> form = formFactory.form(ControlCodeForm.class).bindFromRequest();
    String code = permissionsFinderDao.getSelectedControlCode(controlCodeJourney);
    return frontendServiceClient.get(code)
        .thenApplyAsync(result -> {

          // Outside of form binding to preserve @Required validation for couldDescribeItems
          String action = form.field("action").value();
          if (action != null && !action.isEmpty()) {
            if ("backToSearch".equals(action)) {
              return journeyManager.performTransition(Events.CONTROL_CODE_FLOW_NEXT, ControlCodeFlowStage.BACK_TO_SEARCH);
            }
            else if ("backToSearchResults".equals(action)) {
              return journeyManager.performTransition(Events.CONTROL_CODE_FLOW_NEXT, ControlCodeFlowStage.BACK_TO_SEARCH_RESULTS);
            }
            else {
              throw new FormStateException("Invalid value for action: \"" + action + "\"");
            }
          }

          if (form.hasErrors()) {
            return completedFuture(ok(controlCode.render(form, new ControlCodeDisplay(controlCodeJourney, result))));
          }

          String couldDescribeItems = form.get().couldDescribeItems;
          if("true".equals(couldDescribeItems)) {
            permissionsFinderDao.saveControlCodeApplies(controlCodeJourney, true);
            return nextScreenTrue(controlCodeJourney, result);
          }
          else if ("false".equals(couldDescribeItems)) {
            permissionsFinderDao.saveControlCodeApplies(controlCodeJourney, false);
            return controlCodeJourneyHelper.notApplicableJourneyTransition(controlCodeJourney);
          }
          else {
            throw new FormStateException("Unhandled form state");
          }
        }, httpExecutionContext.current()).thenCompose(Function.identity());
  }

  public CompletionStage<Result> handleSearchSubmit() {
    return handleSubmit(ControlCodeJourney.PHYSICAL_GOODS_SEARCH);
  }

  public CompletionStage<Result> handleSearchRelatedToSubmit (String goodsTypeText) {
    return SoftTechJourneyHelper.validateGoodsTypeTextThenContinue(goodsTypeText, this::handleSubmit);
  }

  public CompletionStage<Result> handleSoftwareControlsSubmit() {
    return handleSubmit(ControlCodeJourney.SOFTWARE_CONTROLS);
  }

  public CompletionStage<Result> handleRelatedSoftwareControlsSubmit() {
    return handleSubmit(ControlCodeJourney.SOFTWARE_CONTROLS_RELATED_TO_A_PHYSICAL_GOOD);
  }

  public CompletionStage<Result> handleSoftwareCatchallControlsSubmit() {
    return handleSubmit(ControlCodeJourney.SOFTWARE_CATCHALL_CONTROLS);
  }

  public CompletionStage<Result> nextScreenTrue(ControlCodeJourney controlCodeJourney, FrontendServiceResult frontendServiceResult) {
    ControlCodeData controlCodeData = frontendServiceResult.controlCodeData;
    if (controlCodeData.canShow()) {
      if (controlCodeData.canShowAdditionalSpecifications()) {
        return journeyManager.performTransition(Events.CONTROL_CODE_FLOW_NEXT, ControlCodeFlowStage.ADDITIONAL_SPECIFICATIONS);
      }
      else if (controlCodeData.canShowDecontrols()) {
        return journeyManager.performTransition(Events.CONTROL_CODE_FLOW_NEXT, ControlCodeFlowStage.DECONTROLS);
      }
      else {
        return journeyManager.performTransition(Events.CONTROL_CODE_FLOW_NEXT, ControlCodeFlowStage.TECHNICAL_NOTES);
      }
    }
    else {
      return controlCodeJourneyHelper.confirmedJourneyTransition(controlCodeJourney, controlCodeData.controlCode);
    }
  }

  public static class ControlCodeForm {

    @Required(message = "You must answer this question")
    public String couldDescribeItems;

    public String action;

  }

}