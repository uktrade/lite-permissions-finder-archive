package controllers.controlcode;

import static java.util.concurrent.CompletableFuture.completedFuture;
import static play.mvc.Results.ok;

import com.google.inject.Inject;
import components.common.journey.JourneyManager;
import components.common.journey.StandardEvents;
import components.persistence.PermissionsFinderDao;
import components.services.controlcode.FrontendServiceClient;
import exceptions.FormStateException;
import journey.helpers.ControlCodeJourneyHelper;
import models.controlcode.ControlCodeSubJourney;
import models.controlcode.DecontrolsDisplay;
import play.data.Form;
import play.data.FormFactory;
import play.data.validation.Constraints.Required;
import play.libs.concurrent.HttpExecutionContext;
import play.mvc.Result;
import views.html.controlcode.decontrols;

import java.util.Optional;
import java.util.concurrent.CompletionStage;
import java.util.function.Function;

public class DecontrolsController {

  private final JourneyManager journeyManager;
  private final FormFactory formFactory;
  private final PermissionsFinderDao permissionsFinderDao;
  private final HttpExecutionContext httpExecutionContext;
  private final FrontendServiceClient frontendServiceClient;
  private final ControlCodeJourneyHelper controlCodeJourneyHelper;

  @Inject
  public DecontrolsController(JourneyManager journeyManager,
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

  private CompletionStage<Result> renderForm(ControlCodeSubJourney controlCodeSubJourney) {
    Optional<Boolean> decontrolsApply = permissionsFinderDao.getControlCodeDecontrolsApply(controlCodeSubJourney);
    DecontrolsForm templateForm = new DecontrolsForm();
    templateForm.decontrolsDescribeItem = decontrolsApply.isPresent() ? decontrolsApply.get().toString() : "";
    return frontendServiceClient.get(permissionsFinderDao.getSelectedControlCode(controlCodeSubJourney))
        .thenApplyAsync(result -> ok(decontrols.render(formFactory.form(DecontrolsForm.class).fill(templateForm),
            new DecontrolsDisplay(controlCodeSubJourney, result))), httpExecutionContext.current());
  }

  public CompletionStage<Result> renderSearchForm() {
    return renderForm(models.controlcode.ControlCodeSubJourney.PHYSICAL_GOODS_SEARCH);
  }

  public CompletionStage<Result> renderSearchRelatedToForm(String goodsTypeText) {
    return ControlCodeJourneyHelper.getSearchRelatedToPhysicalGoodsResult(goodsTypeText, this::renderForm);
  }

  public CompletionStage<Result> renderControlsForm(String goodsTypeText) {
    return ControlCodeJourneyHelper.getControlsResult(goodsTypeText, this::renderForm);
  }

  public CompletionStage<Result> renderRelatedControlsForm(String goodsTypeText) {
    return ControlCodeJourneyHelper.getRelatedControlsResult(goodsTypeText, this::renderForm);
  }

  public CompletionStage<Result> renderCatchallControlsForm(String goodsTypeText) {
    return ControlCodeJourneyHelper.getCatchAllControlsResult(goodsTypeText, this::renderForm);
  }

  private CompletionStage<Result> handleSubmit(ControlCodeSubJourney controlCodeSubJourney){
    Form<DecontrolsForm> form = formFactory.form(DecontrolsForm.class).bindFromRequest();
    String controlCode = permissionsFinderDao.getSelectedControlCode(controlCodeSubJourney);
    return frontendServiceClient.get(controlCode)
        .thenApplyAsync(result -> {
          if (form.hasErrors()) {
            return completedFuture(ok(decontrols.render(form, new DecontrolsDisplay(controlCodeSubJourney, result))));
          }
          else {
            String decontrolsDescribeItem = form.get().decontrolsDescribeItem;
            if("true".equals(decontrolsDescribeItem)) {
              permissionsFinderDao.saveControlCodeDecontrolsApply(controlCodeSubJourney, true);
              return controlCodeJourneyHelper.notApplicableJourneyTransition(controlCodeSubJourney);
            }
            else if ("false".equals(decontrolsDescribeItem)) {
              permissionsFinderDao.saveControlCodeDecontrolsApply(controlCodeSubJourney, false);
              if (result.controlCodeData.canShowTechnicalNotes()) {
                return journeyManager.performTransition(StandardEvents.NEXT); //journeyManager.performTransition(Events.CONTROL_CODE_FLOW_NEXT, ControlCodeFlowStage.TECHNICAL_NOTES);
              }
              else {
                return controlCodeJourneyHelper.confirmedJourneyTransition(controlCodeSubJourney, controlCode);
              }
            }
            else {
              throw new FormStateException("Unhandled form state");
            }
          }
        }, httpExecutionContext.current()).thenCompose(Function.identity());
  }

  public CompletionStage<Result> handleSearchSubmit() {
    return handleSubmit(ControlCodeSubJourney.PHYSICAL_GOODS_SEARCH);
  }

  public CompletionStage<Result> handleSearchRelatedToSubmit(String goodsTypeText) {
    return ControlCodeJourneyHelper.getSearchRelatedToPhysicalGoodsResult(goodsTypeText, this::handleSubmit);
  }

  public CompletionStage<Result> handleControlsSubmit(String goodsTypeText) {
    return ControlCodeJourneyHelper.getControlsResult(goodsTypeText, this::handleSubmit);
  }

  public CompletionStage<Result> handleRelatedControlsSubmit(String goodsTypeText) {
    return ControlCodeJourneyHelper.getRelatedControlsResult(goodsTypeText, this::handleSubmit);
  }

  public CompletionStage<Result> handleCatchallControlsSubmit(String goodsTypeText) {
    return ControlCodeJourneyHelper.getCatchAllControlsResult(goodsTypeText, this::handleSubmit);
  }

  public static class DecontrolsForm {

    @Required(message = "You must answer this question")
    public String decontrolsDescribeItem;

  }

}
