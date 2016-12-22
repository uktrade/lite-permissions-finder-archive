package controllers.controlcode;

import static java.util.concurrent.CompletableFuture.completedFuture;

import com.google.inject.Inject;
import components.common.journey.JourneyManager;
import components.common.journey.StandardEvents;
import components.persistence.PermissionsFinderDao;
import components.services.controlcode.FrontendServiceClient;
import exceptions.FormStateException;
import journey.Events;
import journey.SubJourneyContextParamProvider;
import journey.helpers.ControlCodeJourneyHelper;
import models.ControlCodeFlowStage;
import models.controlcode.ControlCodeDisplay;
import models.controlcode.ControlCodeJourney;
import play.Logger;
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
  private final SubJourneyContextParamProvider subJourneyContextParamProvider;

  @Inject
  public ControlCodeController(JourneyManager journeyManager,
                               FormFactory formFactory,
                               PermissionsFinderDao permissionsFinderDao,
                               HttpExecutionContext httpExecutionContext,
                               FrontendServiceClient frontendServiceClient,
                               ControlCodeJourneyHelper controlCodeJourneyHelper,
                               SubJourneyContextParamProvider subJourneyContextParamProvider) {
    this.journeyManager = journeyManager;
    this.formFactory = formFactory;
    this.permissionsFinderDao = permissionsFinderDao;
    this.httpExecutionContext = httpExecutionContext;
    this.frontendServiceClient = frontendServiceClient;
    this.controlCodeJourneyHelper = controlCodeJourneyHelper;
    this.subJourneyContextParamProvider = subJourneyContextParamProvider;
  }

  public CompletionStage<Result> renderForm(String controlCodeVariantText, String goodsTypeText) {
    @SuppressWarnings("OptionalGetWithoutIsPresent")
    ControlCodeJourney controlCodeJourney = ControlCodeJourney.getMatched(controlCodeVariantText, goodsTypeText).get();
    subJourneyContextParamProvider.updateSubJourneyValueOnContext(controlCodeJourney);
    return renderFormInternal(controlCodeJourney);
  }

  private CompletionStage<Result> renderFormInternal(ControlCodeJourney controlCodeJourney) {
    Optional<Boolean> controlCodeApplies = permissionsFinderDao.getControlCodeApplies(controlCodeJourney);
    ControlCodeForm templateForm = new ControlCodeForm();
    templateForm.couldDescribeItems = controlCodeApplies.isPresent() ? controlCodeApplies.get().toString() : "";
    return frontendServiceClient.get(permissionsFinderDao.getSelectedControlCode(controlCodeJourney))
        .thenApplyAsync(frontendServiceResult -> {
          return ok(controlCode.render(formFactory.form(ControlCodeForm.class).fill(templateForm),
              new ControlCodeDisplay(controlCodeJourney, frontendServiceResult)));
        }, httpExecutionContext.current());
  }

  public CompletionStage<Result> renderSearchForm() {
    return renderFormInternal(ControlCodeJourney.PHYSICAL_GOODS_SEARCH);
  }

  public CompletionStage<Result> renderSearchRelatedToForm (String goodsTypeText) {
    return ControlCodeJourneyHelper.getSearchRelatedToPhysicalGoodsResult(goodsTypeText, this::renderFormInternal);
  }

  public CompletionStage<Result> renderControlsForm(String goodsTypeText) {
    return ControlCodeJourneyHelper.getControlsResult(goodsTypeText, this::renderFormInternal);
  }

  public CompletionStage<Result> renderRelatedControlsForm(String goodsTypeText) {
    return ControlCodeJourneyHelper.getRelatedControlsResult(goodsTypeText, this::renderFormInternal);
  }

  public CompletionStage<Result> renderCatchallControlsForm(String goodsTypeText) {
    return ControlCodeJourneyHelper.getCatchAllControlsResult(goodsTypeText, this::renderFormInternal);
  }

  public CompletionStage<Result> handleSubmit() {
    ControlCodeJourney controlCodeJourney = subJourneyContextParamProvider.getSubJourneyValueFromRequest();
    return handleSubmitInternal(controlCodeJourney);
  }

  private CompletionStage<Result> handleSubmitInternal(ControlCodeJourney controlCodeJourney) {
    Form<ControlCodeForm> form = formFactory.form(ControlCodeForm.class).bindFromRequest();
    String code = permissionsFinderDao.getSelectedControlCode(controlCodeJourney);
    return frontendServiceClient.get(code)
        .thenApplyAsync(frontendServiceResult -> {
          // Outside of form binding to preserve @Required validation for couldDescribeItems
          String action = form.field("action").value();
          if (action != null && !action.isEmpty()) {
            if ("backToSearch".equals(action) && ControlCodeJourney.isPhysicalGoodsSearchVariant(controlCodeJourney)) {
              return journeyManager.performTransition(Events.CONTROL_CODE_FLOW_NEXT, ControlCodeFlowStage.BACK_TO_SEARCH);
            }
            else if ("backToResults".equals(action) && ControlCodeJourney.isPhysicalGoodsSearchVariant(controlCodeJourney)) {
              return journeyManager.performTransition(Events.CONTROL_CODE_FLOW_NEXT, ControlCodeFlowStage.BACK_TO_RESULTS);
            }
            else if ("backToMatches".equals(action) && (ControlCodeJourney.isSoftTechControlsVariant(controlCodeJourney)
                || ControlCodeJourney.isSoftTechControlsRelatedToPhysicalGoodVariant(controlCodeJourney)
                || ControlCodeJourney.isSoftTechCatchallControlsVariant(controlCodeJourney))) {
              return journeyManager.performTransition(Events.CONTROL_CODE_FLOW_NEXT, ControlCodeFlowStage.BACK_TO_MATCHES);
            }
            else if ("backToCategory".equals(action) && ControlCodeJourney.isSoftTechCatchallControlsVariant(controlCodeJourney)) {
              return journeyManager.performTransition(Events.CONTROL_CODE_FLOW_NEXT, ControlCodeFlowStage.BACK_TO_CATEGORY);
            }
            else {
              throw new FormStateException(String.format("Invalid combination of action: \"%s\" and " +
                  "controlCodeJourney: \"%s\"", action, controlCodeJourney.toString()));
            }
          }

          if (form.hasErrors()) {
            return completedFuture(ok(controlCode.render(form, new ControlCodeDisplay(controlCodeJourney, frontendServiceResult))));
          }

          String couldDescribeItems = form.get().couldDescribeItems;
          if("true".equals(couldDescribeItems)) {
            permissionsFinderDao.saveControlCodeApplies(controlCodeJourney, true);
            return journeyManager.performTransition(StandardEvents.NEXT);
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

  public CompletionStage<Result> handleSearchRelatedToSubmit (String goodsTypeText) {
    return ControlCodeJourneyHelper.getSearchRelatedToPhysicalGoodsResult(goodsTypeText, this::handleSubmitInternal);
  }

  public CompletionStage<Result> handleControlsSubmit(String goodsTypeText) {
    return ControlCodeJourneyHelper.getControlsResult(goodsTypeText, this::handleSubmitInternal);
  }

  public CompletionStage<Result> handleRelatedControlsSubmit(String goodsTypeText) {
    return ControlCodeJourneyHelper.getRelatedControlsResult(goodsTypeText, this::handleSubmitInternal);
  }

  public CompletionStage<Result> handleCatchallControlsSubmit(String goodsTypeText) {
    return ControlCodeJourneyHelper.getCatchAllControlsResult(goodsTypeText, this::handleSubmitInternal);
  }

  public static class ControlCodeForm {

    @Required(message = "You must answer this question")
    public String couldDescribeItems;

    public String action;

  }

}