package controllers.controlcode;

import static java.util.concurrent.CompletableFuture.completedFuture;

import com.google.inject.Inject;
import components.common.journey.JourneyManager;
import components.common.journey.StandardEvents;
import components.persistence.PermissionsFinderDao;
import components.services.controlcode.FrontendServiceClient;
import exceptions.FormStateException;
import journey.Events;
import journey.helpers.ControlCodeSubJourneyHelper;
import models.ControlCodeFlowStage;
import models.controlcode.ControlCodeSummaryDisplay;
import models.controlcode.ControlCodeSubJourney;
import play.data.Form;
import play.data.FormFactory;
import play.data.validation.Constraints.Required;
import play.libs.concurrent.HttpExecutionContext;
import play.mvc.Controller;
import play.mvc.Result;
import views.html.controlcode.controlCodeSummary;

import java.util.Optional;
import java.util.concurrent.CompletionStage;
import java.util.function.Function;

public class ControlCodeSummaryController extends Controller {

  private final JourneyManager journeyManager;
  private final FormFactory formFactory;
  private final PermissionsFinderDao permissionsFinderDao;
  private final HttpExecutionContext httpExecutionContext;
  private final FrontendServiceClient frontendServiceClient;
  private final ControlCodeSubJourneyHelper controlCodeSubJourneyHelper;

  @Inject
  public ControlCodeSummaryController(JourneyManager journeyManager,
                                      FormFactory formFactory,
                                      PermissionsFinderDao permissionsFinderDao,
                                      HttpExecutionContext httpExecutionContext,
                                      FrontendServiceClient frontendServiceClient,
                                      ControlCodeSubJourneyHelper controlCodeSubJourneyHelper) {
    this.journeyManager = journeyManager;
    this.formFactory = formFactory;
    this.permissionsFinderDao = permissionsFinderDao;
    this.httpExecutionContext = httpExecutionContext;
    this.frontendServiceClient = frontendServiceClient;
    this.controlCodeSubJourneyHelper = controlCodeSubJourneyHelper;
  }

  public CompletionStage<Result> renderForm(String controlCodeVariantText, String goodsTypeText) {
   return ControlCodeSubJourneyHelper.resolveUrlToSubJourneyAndUpdateContext(controlCodeVariantText, goodsTypeText, this::renderFormInternal);
  }

  private CompletionStage<Result> renderFormInternal(ControlCodeSubJourney controlCodeSubJourney) {
    Optional<Boolean> controlCodeApplies = permissionsFinderDao.getControlCodeApplies(controlCodeSubJourney);
    ControlCodeForm templateForm = new ControlCodeForm();
    templateForm.couldDescribeItems = controlCodeApplies.isPresent() ? controlCodeApplies.get().toString() : "";
    return frontendServiceClient.get(permissionsFinderDao.getSelectedControlCode(controlCodeSubJourney))
        .thenApplyAsync(frontendServiceResult ->
            ok(controlCodeSummary.render(formFactory.form(ControlCodeForm.class).fill(templateForm),
                new ControlCodeSummaryDisplay(controlCodeSubJourney, frontendServiceResult)))
        , httpExecutionContext.current());
  }

  public CompletionStage<Result> renderSearchRelatedToForm (String goodsTypeText) {
    return ControlCodeSubJourneyHelper.getSearchRelatedToPhysicalGoodsResult(goodsTypeText, this::renderFormInternal);
  }

  public CompletionStage<Result> renderControlsForm(String goodsTypeText) {
    return ControlCodeSubJourneyHelper.getControlsResult(goodsTypeText, this::renderFormInternal);
  }

  public CompletionStage<Result> renderRelatedControlsForm(String goodsTypeText) {
    return ControlCodeSubJourneyHelper.getRelatedControlsResult(goodsTypeText, this::renderFormInternal);
  }

  public CompletionStage<Result> renderCatchallControlsForm(String goodsTypeText) {
    return ControlCodeSubJourneyHelper.getCatchAllControlsResult(goodsTypeText, this::renderFormInternal);
  }

  public CompletionStage<Result> handleSubmit() {
    return ControlCodeSubJourneyHelper.resolveContextToSubJourney(this::handleSubmitInternal);
  }

  private CompletionStage<Result> handleSubmitInternal(ControlCodeSubJourney controlCodeSubJourney) {
    Form<ControlCodeForm> form = formFactory.form(ControlCodeForm.class).bindFromRequest();
    String code = permissionsFinderDao.getSelectedControlCode(controlCodeSubJourney);
    return frontendServiceClient.get(code)
        .thenApplyAsync(frontendServiceResult -> {
          // Outside of form binding to preserve @Required validation for couldDescribeItems
          String action = form.field("action").value();
          if (action != null && !action.isEmpty()) {
            if ("backToSearch".equals(action) && models.controlcode.ControlCodeSubJourney.isPhysicalGoodsSearchVariant(controlCodeSubJourney)) {
              return journeyManager.performTransition(Events.CONTROL_CODE_FLOW_NEXT, ControlCodeFlowStage.BACK_TO_SEARCH);
            }
            else if ("backToResults".equals(action) && models.controlcode.ControlCodeSubJourney.isPhysicalGoodsSearchVariant(controlCodeSubJourney)) {
              return journeyManager.performTransition(Events.CONTROL_CODE_FLOW_NEXT, ControlCodeFlowStage.BACK_TO_RESULTS);
            }
            else if ("backToMatches".equals(action) && (models.controlcode.ControlCodeSubJourney.isSoftTechControlsVariant(controlCodeSubJourney)
                || models.controlcode.ControlCodeSubJourney.isSoftTechControlsRelatedToPhysicalGoodVariant(controlCodeSubJourney)
                || models.controlcode.ControlCodeSubJourney.isSoftTechCatchallControlsVariant(controlCodeSubJourney))) {
              return journeyManager.performTransition(Events.CONTROL_CODE_FLOW_NEXT, ControlCodeFlowStage.BACK_TO_MATCHES);
            }
            else if ("backToCategory".equals(action) && models.controlcode.ControlCodeSubJourney.isSoftTechCatchallControlsVariant(controlCodeSubJourney)) {
              return journeyManager.performTransition(Events.CONTROL_CODE_FLOW_NEXT, ControlCodeFlowStage.BACK_TO_CATEGORY);
            }
            else {
              throw new FormStateException(String.format("Invalid combination of action: \"%s\" and " +
                  "controlCodeSubJourney: \"%s\"", action, controlCodeSubJourney.toString()));
            }
          }

          if (form.hasErrors()) {
            return completedFuture(ok(controlCodeSummary.render(form, new ControlCodeSummaryDisplay(controlCodeSubJourney, frontendServiceResult))));
          }

          String couldDescribeItems = form.get().couldDescribeItems;
          if("true".equals(couldDescribeItems)) {
            permissionsFinderDao.saveControlCodeApplies(controlCodeSubJourney, true);
            return journeyManager.performTransition(StandardEvents.NEXT);
          }
          else if ("false".equals(couldDescribeItems)) {
            permissionsFinderDao.saveControlCodeApplies(controlCodeSubJourney, false);
            return controlCodeSubJourneyHelper.notApplicableJourneyTransition(controlCodeSubJourney);
          }
          else {
            throw new FormStateException("Unhandled form state");
          }
        }, httpExecutionContext.current()).thenCompose(Function.identity());
  }

  public CompletionStage<Result> handleSearchRelatedToSubmit (String goodsTypeText) {
    return ControlCodeSubJourneyHelper.getSearchRelatedToPhysicalGoodsResult(goodsTypeText, this::handleSubmitInternal);
  }

  public CompletionStage<Result> handleControlsSubmit(String goodsTypeText) {
    return ControlCodeSubJourneyHelper.getControlsResult(goodsTypeText, this::handleSubmitInternal);
  }

  public CompletionStage<Result> handleRelatedControlsSubmit(String goodsTypeText) {
    return ControlCodeSubJourneyHelper.getRelatedControlsResult(goodsTypeText, this::handleSubmitInternal);
  }

  public CompletionStage<Result> handleCatchallControlsSubmit(String goodsTypeText) {
    return ControlCodeSubJourneyHelper.getCatchAllControlsResult(goodsTypeText, this::handleSubmitInternal);
  }

  public static class ControlCodeForm {

    @Required(message = "You must answer this question")
    public String couldDescribeItems;

    public String action;

  }

}