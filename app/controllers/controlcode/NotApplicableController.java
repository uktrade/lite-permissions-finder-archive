package controllers.controlcode;

import static play.mvc.Results.ok;

import com.google.inject.Inject;
import components.common.journey.JourneyManager;
import components.persistence.PermissionsFinderDao;
import components.services.controlcode.FrontendServiceClient;
import components.services.controlcode.FrontendServiceResult;
import exceptions.FormStateException;
import journey.Events;
import journey.helpers.ControlCodeJourneyHelper;
import journey.helpers.SoftTechJourneyHelper;
import models.ControlCodeFlowStage;
import models.GoodsType;
import models.controlcode.ControlCodeJourney;
import models.controlcode.NotApplicableDisplay;
import models.softtech.ApplicableSoftTechControls;
import models.softtech.SoftTechCatchallControlsNotApplicableFlow;
import models.softtech.SoftTechCategory;
import models.softtech.SoftTechControlsNotApplicableFlow;
import play.data.Form;
import play.data.FormFactory;
import play.libs.concurrent.HttpExecutionContext;
import play.mvc.Result;
import views.html.controlcode.notApplicable;

import java.util.concurrent.CompletionStage;
import java.util.function.Function;

public class NotApplicableController {

  private final FormFactory formFactory;
  private final JourneyManager journeyManager;
  private final PermissionsFinderDao permissionsFinderDao;
  private final HttpExecutionContext httpExecutionContext;
  private final FrontendServiceClient frontendServiceClient;
  private final SoftTechJourneyHelper softTechJourneyHelper;

  @Inject
  public NotApplicableController(FormFactory formFactory,
                                 JourneyManager journeyManager,
                                 PermissionsFinderDao permissionsFinderDao,
                                 HttpExecutionContext httpExecutionContext,
                                 FrontendServiceClient frontendServiceClient,
                                 SoftTechJourneyHelper softTechJourneyHelper) {
    this.formFactory = formFactory;
    this.journeyManager = journeyManager;
    this.permissionsFinderDao = permissionsFinderDao;
    this.httpExecutionContext = httpExecutionContext;
    this.frontendServiceClient = frontendServiceClient;
    this.softTechJourneyHelper = softTechJourneyHelper;
  }

  private CompletionStage<Result> renderForm(ControlCodeJourney controlCodeJourney, String showExtendedContent) {
    if (controlCodeJourney == ControlCodeJourney.PHYSICAL_GOODS_SEARCH ||
        controlCodeJourney == ControlCodeJourney.PHYSICAL_GOODS_SEARCH_RELATED_TO_SOFTWARE) {
      return frontendServiceClient
          .get(permissionsFinderDao.getSelectedControlCode(controlCodeJourney))
          .thenApplyAsync(result ->
                  ok(notApplicable.render(new NotApplicableDisplay(controlCodeJourney,
                      formFactory.form(NotApplicableForm.class),
                      result.controlCodeData.alias,
                      Boolean.parseBoolean(showExtendedContent))))
              , httpExecutionContext.current());
    }
    else if (controlCodeJourney == ControlCodeJourney.SOFTWARE_CONTROLS ||
        controlCodeJourney == ControlCodeJourney.TECHNOLOGY_CONTROLS) {

      GoodsType goodsType = controlCodeJourney == ControlCodeJourney.SOFTWARE_CONTROLS
          ? GoodsType.SOFTWARE
          : GoodsType.TECHNOLOGY;

      // If on the software control journey, check the amount of applicable controls. This feeds into the display logic
      SoftTechCategory softTechCategory = permissionsFinderDao.getSoftTechCategory(goodsType).get();
      String selectedControlCode = permissionsFinderDao.getSelectedControlCode(controlCodeJourney);
      CompletionStage<FrontendServiceResult> frontendStage = frontendServiceClient.get(selectedControlCode);
      return softTechJourneyHelper.checkSoftTechControls(goodsType, softTechCategory, false)
          .thenCombineAsync(frontendStage, (controls, result) -> ok(
              notApplicable.render(
                  new NotApplicableDisplay(controlCodeJourney, formFactory.form(NotApplicableForm.class),
                      result.controlCodeData.alias, Boolean.parseBoolean(showExtendedContent), controls))
          ), httpExecutionContext.current());
    }
    else if (controlCodeJourney == ControlCodeJourney.SOFTWARE_CONTROLS_RELATED_TO_A_PHYSICAL_GOOD) {
      // If on the software control journey, check the amount of applicable controls. This feeds into the display logic
      String selectedControlCode = permissionsFinderDao.getSelectedControlCode(controlCodeJourney);
      CompletionStage<FrontendServiceResult> frontendStage = frontendServiceClient.get(selectedControlCode);
      return softTechJourneyHelper.checkRelatedSoftwareControls(selectedControlCode, false)
          .thenCombineAsync(frontendStage, (controls, result) -> ok(
              notApplicable.render(
                  new NotApplicableDisplay(controlCodeJourney, formFactory.form(NotApplicableForm.class),
                      result.controlCodeData.alias, Boolean.parseBoolean(showExtendedContent), controls))
          ), httpExecutionContext.current());
    }
    else if (controlCodeJourney == ControlCodeJourney.SOFTWARE_CATCHALL_CONTROLS ||
        controlCodeJourney == ControlCodeJourney.TECHNOLOGY_CATCHALL_CONTROLS) {

      GoodsType goodsType = controlCodeJourney == ControlCodeJourney.SOFTWARE_CATCHALL_CONTROLS
          ? GoodsType.SOFTWARE
          : GoodsType.TECHNOLOGY;

      // If on the software control journey, check the amount of applicable controls. This feeds into the display logic
      String selectedControlCode = permissionsFinderDao.getSelectedControlCode(controlCodeJourney);
      CompletionStage<FrontendServiceResult> frontendStage = frontendServiceClient.get(selectedControlCode);
      SoftTechCategory softTechCategory = permissionsFinderDao.getSoftTechCategory(goodsType).get();
      return softTechJourneyHelper.checkCatchtallSoftwareControls(goodsType, softTechCategory, false)
          .thenCombineAsync(frontendStage, (controls, result) -> ok(
              notApplicable.render(
                  new NotApplicableDisplay(controlCodeJourney, formFactory.form(NotApplicableForm.class),
                      result.controlCodeData.alias, Boolean.parseBoolean(showExtendedContent), controls))
          ), httpExecutionContext.current());
    }
    else {
      throw new RuntimeException(String.format("Unexpected member of ControlCodeJourney enum: \"%s\""
          , controlCodeJourney.toString()));
    }
  }

  public CompletionStage<Result> renderSearchForm(String showExtendedContent) {
    return renderForm(ControlCodeJourney.PHYSICAL_GOODS_SEARCH, showExtendedContent);
  }

  public CompletionStage<Result> renderSearchRelatedToForm(String goodsTypeText, String showExtendedContent) {
    return ControlCodeJourneyHelper.getSearchRelatedToPhysicalGoodsResult(goodsTypeText,
        controlCodeJourney -> this.renderForm(controlCodeJourney, showExtendedContent));
  }

  public CompletionStage<Result> renderControlsForm(String goodsTypeText, String showExtendedContent) {
    return ControlCodeJourneyHelper.getControlsResult(goodsTypeText,
        controlCodeJourney -> renderForm(controlCodeJourney, showExtendedContent));
  }

  public CompletionStage<Result> renderRelatedControlsForm(String goodsTypeText, String showExtendedContent) {
    return ControlCodeJourneyHelper.getRelatedControlsResult(goodsTypeText,
        controlCodeJourney -> renderForm(controlCodeJourney, showExtendedContent));
  }

  public CompletionStage<Result> renderCatchallControlsForm(String goodsTypeText, String showExtendedContent) {
    return ControlCodeJourneyHelper.getCatchAllControlsResult(goodsTypeText,
        controlCodeJourney -> renderForm(controlCodeJourney, showExtendedContent));
  }

  private CompletionStage<Result> handleSubmit(ControlCodeJourney controlCodeJourney) {
    Form<NotApplicableForm> form = formFactory.form(NotApplicableForm.class).bindFromRequest();
    if (!form.hasErrors()) {
      String action = form.get().action;
      if (controlCodeJourney == ControlCodeJourney.PHYSICAL_GOODS_SEARCH
          || controlCodeJourney == ControlCodeJourney.PHYSICAL_GOODS_SEARCH_RELATED_TO_SOFTWARE) {
        if ("backToSearch".equals(action)) {
          return journeyManager.performTransition(Events.CONTROL_CODE_FLOW_NEXT, ControlCodeFlowStage.BACK_TO_SEARCH);
        }
        else if ("backToSearchResults".equals(action)) {
          return journeyManager.performTransition(Events.CONTROL_CODE_FLOW_NEXT, ControlCodeFlowStage.BACK_TO_SEARCH_RESULTS);
        }
        else {
          throw new FormStateException("Unknown value for action: \"" + action + "\"");
        }
      }
      else if (controlCodeJourney == ControlCodeJourney.SOFTWARE_CONTROLS ||
          controlCodeJourney == ControlCodeJourney.TECHNOLOGY_CONTROLS) {

        GoodsType goodsType = controlCodeJourney == ControlCodeJourney.SOFTWARE_CATCHALL_CONTROLS
            ? GoodsType.SOFTWARE
            : GoodsType.TECHNOLOGY;

        // A different action is expected for each valid member of ApplicableSoftTechControls
        SoftTechCategory softTechCategory = permissionsFinderDao.getSoftTechCategory(goodsType).get();
        return softTechJourneyHelper.checkSoftTechControls(goodsType, softTechCategory, false)
            .thenApplyAsync(controls -> softTechControls(controls, action),
                httpExecutionContext.current()).thenCompose(Function.identity());
      }
      else if (controlCodeJourney == ControlCodeJourney.SOFTWARE_CONTROLS_RELATED_TO_A_PHYSICAL_GOOD) {
        // A different action is expected for each valid member of ApplicableSoftTechControls
        String controlCode = permissionsFinderDao.getSelectedControlCode(ControlCodeJourney.SOFTWARE_CONTROLS_RELATED_TO_A_PHYSICAL_GOOD);
        return softTechJourneyHelper.checkRelatedSoftwareControls(controlCode, false)
            .thenApplyAsync(controls -> softwareControlsRelatedToPhysicalGood(controls, action),
                httpExecutionContext.current()).thenCompose(Function.identity());
      }
      else if (controlCodeJourney == ControlCodeJourney.SOFTWARE_CATCHALL_CONTROLS ||
          controlCodeJourney == ControlCodeJourney.TECHNOLOGY_CATCHALL_CONTROLS) {

        GoodsType goodsType = controlCodeJourney == ControlCodeJourney.SOFTWARE_CATCHALL_CONTROLS
            ? GoodsType.SOFTWARE
            : GoodsType.TECHNOLOGY;

        // A different action is expected for each valid member of ApplicableSoftTechControls
        SoftTechCategory softTechCategory = permissionsFinderDao.getSoftTechCategory(goodsType).get();
        return softTechJourneyHelper.checkCatchtallSoftwareControls(goodsType, softTechCategory, false)
            .thenApplyAsync(controls -> softTechCatcallControls(controls, action),
                httpExecutionContext.current()).thenCompose(Function.identity());
      }
      else {
        throw new RuntimeException(String.format("Unexpected member of ControlCodeJourney enum: \"%s\""
            , controlCodeJourney.toString()));
      }
    }
    throw new FormStateException("Unhandled form state");
  }

  private CompletionStage<Result> softTechControls(ApplicableSoftTechControls applicableSoftTechControls, String action) {
    // TODO remove duplicate code
    if (applicableSoftTechControls == ApplicableSoftTechControls.ONE) {
      if ("continue".equals(action)) {
        return journeyManager.performTransition(Events.CONTROL_CODE_SOFT_TECH_CONTROLS_NOT_APPLICABLE_FLOW,
            SoftTechControlsNotApplicableFlow.CONTINUE_NO_CONTROLS);
      }
      else {
        throw new FormStateException("Unknown value for action: \"" + action + "\"");
      }
    }
    else if (applicableSoftTechControls == ApplicableSoftTechControls.GREATER_THAN_ONE) {
      if ("returnToControls".equals(action)) {
        return journeyManager.performTransition(Events.CONTROL_CODE_SOFT_TECH_CONTROLS_NOT_APPLICABLE_FLOW,
            SoftTechControlsNotApplicableFlow.RETURN_TO_SOFT_TECH_CONTROLS);
      }
      else {
        throw new FormStateException("Unknown value for action: \"" + action + "\"");
      }
    }
    else {
      throw new RuntimeException(String.format("Unexpected member of ApplicableSoftTechControls enum: \"%s\""
          , applicableSoftTechControls.toString()));
    }
  }

  private CompletionStage<Result> softwareControlsRelatedToPhysicalGood(ApplicableSoftTechControls applicableSoftTechControls, String action) {
    // TODO remove duplicate code
    if (applicableSoftTechControls == ApplicableSoftTechControls.ONE) {
      if ("continue".equals(action)) {
        return softTechJourneyHelper.performCatchallSoftTechControlsTransition(GoodsType.SOFTWARE); // TODO TECHNOLOGY
      }
      else {
        throw new FormStateException("Unknown value for action: \"" + action + "\"");
      }
    }
    else if (applicableSoftTechControls == ApplicableSoftTechControls.GREATER_THAN_ONE) {
      if ("returnToControls".equals(action)) {
        return journeyManager.performTransition(Events.CONTROL_CODE_SOFT_TECH_CONTROLS_NOT_APPLICABLE_FLOW,
            SoftTechControlsNotApplicableFlow.RETURN_TO_SOFT_TECH_CONTROLS);
      }
      else {
        throw new FormStateException("Unknown value for action: \"" + action + "\"");
      }
    }
    else {
      throw new RuntimeException(String.format("Unexpected member of ApplicableSoftTechControls enum: \"%s\""
          , applicableSoftTechControls.toString()));
    }
  }

  private CompletionStage<Result> softTechCatcallControls(ApplicableSoftTechControls applicableSoftTechControls, String action) {
    if (applicableSoftTechControls == ApplicableSoftTechControls.GREATER_THAN_ONE) {
      if ("returnToControls".equals(action)) {
        return journeyManager.performTransition(Events.CONTROL_CODE_SOFT_TECH_CATCHALL_CONTROLS_NOT_APPLICABLE_FLOW,
            SoftTechCatchallControlsNotApplicableFlow.RETURN_TO_SOFT_TECH_CATCHALL_CONTROLS);
      }
      else {
        throw new FormStateException("Unknown value for action: \"" + action + "\"");
      }
    }
    else {
      throw new RuntimeException(String.format("Unexpected member of ApplicableSoftTechControls enum: \"%s\""
          , applicableSoftTechControls.toString()));
    }
  }

  public CompletionStage<Result> handleSearchSubmit() {
    return handleSubmit(ControlCodeJourney.PHYSICAL_GOODS_SEARCH);
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

  public static class NotApplicableForm {

    public String action;

  }
}
