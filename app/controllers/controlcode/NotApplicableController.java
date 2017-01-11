package controllers.controlcode;

import static play.mvc.Results.ok;

import com.google.inject.Inject;
import components.common.journey.JourneyManager;
import components.common.journey.StandardEvents;
import components.persistence.PermissionsFinderDao;
import components.services.controlcode.FrontendServiceClient;
import components.services.controlcode.FrontendServiceResult;
import exceptions.FormStateException;
import journey.Events;
import journey.helpers.ControlCodeSubJourneyHelper;
import journey.helpers.SoftTechJourneyHelper;
import models.GoodsType;
import models.controlcode.BackType;
import models.controlcode.ControlCodeSubJourney;
import models.controlcode.NotApplicableDisplay;
import models.softtech.SoftTechCategory;
import play.data.Form;
import play.data.FormFactory;
import play.libs.concurrent.HttpExecutionContext;
import play.mvc.Result;
import views.html.controlcode.notApplicable;

import java.util.concurrent.CompletionStage;

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

  public CompletionStage<Result> renderForm(String controlCodeVariantText, String goodsTypeText, String showExtendedContent) {
    return ControlCodeSubJourneyHelper.resolveUrlToSubJourneyAndUpdateContext(controlCodeVariantText, goodsTypeText,
        controlCodeSubJourney -> renderFormInternal(controlCodeSubJourney, showExtendedContent));
  }

  private CompletionStage<Result> renderFormInternal(ControlCodeSubJourney controlCodeSubJourney, String showExtendedContent) {
    if (controlCodeSubJourney.isPhysicalGoodsSearchVariant()) {
      return frontendServiceClient
          .get(permissionsFinderDao.getSelectedControlCode(controlCodeSubJourney))
          .thenApplyAsync(result ->
                  ok(notApplicable.render(new NotApplicableDisplay(controlCodeSubJourney,
                      formFactory.form(NotApplicableForm.class),
                      result.getControlCodeData().alias,
                      Boolean.parseBoolean(showExtendedContent))))
              , httpExecutionContext.current());
    }
    else if (controlCodeSubJourney.isSoftTechControlsVariant()) {
      GoodsType goodsType = controlCodeSubJourney.getSoftTechGoodsType();
      // If on the software control journey, check the amount of applicable controls. This feeds into the display logic
      SoftTechCategory softTechCategory = permissionsFinderDao.getSoftTechCategory(goodsType).get();
      String selectedControlCode = permissionsFinderDao.getSelectedControlCode(controlCodeSubJourney);
      CompletionStage<FrontendServiceResult> frontendStage = frontendServiceClient.get(selectedControlCode);
      return softTechJourneyHelper.checkSoftTechControls(goodsType, softTechCategory)
          .thenCombineAsync(frontendStage, (controls, result) -> ok(
              notApplicable.render(
                  new NotApplicableDisplay(controlCodeSubJourney, formFactory.form(NotApplicableForm.class),
                      result.getControlCodeData().alias, Boolean.parseBoolean(showExtendedContent), controls))
          ), httpExecutionContext.current());
    }
    else if (controlCodeSubJourney.isSoftTechControlsRelatedToPhysicalGoodVariant()) {
      GoodsType goodsType = controlCodeSubJourney.getSoftTechGoodsType();
      ControlCodeSubJourney physicalControlCodeSubJourney;
      if (controlCodeSubJourney == ControlCodeSubJourney.SOFTWARE_CONTROLS_RELATED_TO_A_PHYSICAL_GOOD) {
        physicalControlCodeSubJourney = ControlCodeSubJourney.PHYSICAL_GOODS_SEARCH_RELATED_TO_SOFTWARE;
      }
      else {
        // ControlCodeSubJourney.TECHNOLOGY_CONTROLS_RELATED_TO_A_PHYSICAL_GOOD
        physicalControlCodeSubJourney = ControlCodeSubJourney.PHYSICAL_GOODS_SEARCH_RELATED_TO_TECHNOLOGY;
      }
      // If on the software control journey, check the amount of applicable controls. This feeds into the display logic
      String physicalGoodControlCode = permissionsFinderDao.getSelectedControlCode(physicalControlCodeSubJourney);
      String selectedControlCode = permissionsFinderDao.getSelectedControlCode(controlCodeSubJourney);
      CompletionStage<FrontendServiceResult> frontendStage = frontendServiceClient.get(selectedControlCode);
      return softTechJourneyHelper.checkRelatedSoftwareControls(goodsType, physicalGoodControlCode)
          .thenCombineAsync(frontendStage, (controls, result) -> ok(
              notApplicable.render(
                  new NotApplicableDisplay(controlCodeSubJourney, formFactory.form(NotApplicableForm.class),
                      result.getControlCodeData().alias, Boolean.parseBoolean(showExtendedContent), controls))
          ), httpExecutionContext.current());
    }
    else if (controlCodeSubJourney.isSoftTechCatchallControlsVariant()) {
      GoodsType goodsType = controlCodeSubJourney.getSoftTechGoodsType();
      // If on the software control journey, check the amount of applicable controls. This feeds into the display logic
      String selectedControlCode = permissionsFinderDao.getSelectedControlCode(controlCodeSubJourney);
      CompletionStage<FrontendServiceResult> frontendStage = frontendServiceClient.get(selectedControlCode);
      SoftTechCategory softTechCategory = permissionsFinderDao.getSoftTechCategory(goodsType).get();
      return softTechJourneyHelper.checkCatchtallSoftwareControls(goodsType, softTechCategory)
          .thenCombineAsync(frontendStage, (controls, result) -> ok(
              notApplicable.render(
                  new NotApplicableDisplay(controlCodeSubJourney, formFactory.form(NotApplicableForm.class),
                      result.getControlCodeData().alias, Boolean.parseBoolean(showExtendedContent), controls))
          ), httpExecutionContext.current());
    }
    else {
      throw new RuntimeException(String.format("Unexpected member of ControlCodeSubJourney enum: \"%s\""
          , controlCodeSubJourney.toString()));
    }
  }

  public CompletionStage<Result> handleSubmit() {
    return ControlCodeSubJourneyHelper.resolveContextToSubJourney(this::handleSubmitInternal);
  }

  private CompletionStage<Result> handleSubmitInternal(ControlCodeSubJourney controlCodeSubJourney) {
    Form<NotApplicableForm> form = formFactory.form(NotApplicableForm.class).bindFromRequest();
    if (!form.hasErrors()) {
      String action = form.get().action;
      if (controlCodeSubJourney.isPhysicalGoodsSearchVariant() ||
          controlCodeSubJourney.isSoftTechControlsVariant() ||
          controlCodeSubJourney.isSoftTechControlsRelatedToPhysicalGoodVariant() ||
          controlCodeSubJourney.isSoftTechCatchallControlsVariant()) {
        if ("backToSearch".equals(action)) {
          return journeyManager.performTransition(Events.BACK, BackType.SEARCH);
        }
        else if ("backToResults".equals(action)) {
          return journeyManager.performTransition(Events.BACK, BackType.RESULTS);
        }
        else if ("backToMatches".equals(action)) {
          return journeyManager.performTransition(Events.BACK, BackType.MATCHES);
        }
        else if ("continue".equals(action)) {
          return journeyManager.performTransition(StandardEvents.NEXT);
        }
        else {
          throw new FormStateException("Unknown value for action: \"" + action + "\"");
        }
      }
      else {
        throw new RuntimeException(String.format("Unexpected member of ControlCodeSubJourney enum: \"%s\""
            , controlCodeSubJourney.toString()));
      }
    }
    throw new FormStateException("Unhandled form state");
  }

  public static class NotApplicableForm {

    public String action;

  }
}
