package controllers.controlcode.notapplicable;

import com.google.inject.Inject;
import components.persistence.PermissionsFinderDao;
import components.services.controlcode.FrontendServiceClient;
import components.services.controlcode.FrontendServiceResult;
import journey.helpers.SoftTechJourneyHelper;
import models.GoodsType;
import models.controlcode.ControlCodeSubJourney;
import models.controlcode.NotApplicableDisplayCommon;
import models.softtech.SoftTechCategory;
import play.libs.concurrent.HttpExecutionContext;
import play.mvc.Result;

import java.util.concurrent.CompletionStage;
import java.util.function.Function;

public class NotApplicableControllerHelper {
  public final PermissionsFinderDao permissionsFinderDao;
  public final SoftTechJourneyHelper softTechJourneyHelper;
  public final FrontendServiceClient frontendServiceClient;
  public final HttpExecutionContext httpExecutionContext;

  @Inject
  public NotApplicableControllerHelper(PermissionsFinderDao permissionsFinderDao,
                                       SoftTechJourneyHelper softTechJourneyHelper,
                                       FrontendServiceClient frontendServiceClient,
                                       HttpExecutionContext httpExecutionContext) {
    this.permissionsFinderDao = permissionsFinderDao;
    this.softTechJourneyHelper = softTechJourneyHelper;
    this.frontendServiceClient = frontendServiceClient;
    this.httpExecutionContext = httpExecutionContext;
  }

  public CompletionStage<Result> physicalGoodsSearchVariant(ControlCodeSubJourney controlCodeSubJourney, Function<NotApplicableDisplayCommon, Result> renderFunction) {
    String selectedControlCode = permissionsFinderDao.getSelectedControlCode(controlCodeSubJourney);

    return frontendServiceClient
        .get(selectedControlCode)
        .thenApplyAsync(result ->
                renderFunction.apply(new NotApplicableDisplayCommon(controlCodeSubJourney, result.getFrontendControlCode(), null))
            , httpExecutionContext.current());
  }

  public CompletionStage<Result> softTechControlsVariantHandleSubmit(ControlCodeSubJourney controlCodeSubJourney, Function<NotApplicableDisplayCommon, Result> renderFunction) {
    GoodsType goodsType = controlCodeSubJourney.getSoftTechGoodsType();
    // If on the software control journey, check the amount of applicable controls. This feeds into the display logic
    SoftTechCategory softTechCategory = permissionsFinderDao.getSoftTechCategory(goodsType).get();

    String selectedControlCode = permissionsFinderDao.getSelectedControlCode(controlCodeSubJourney);
    CompletionStage<FrontendServiceResult> frontendStage = frontendServiceClient.get(selectedControlCode);

    return softTechJourneyHelper.checkSoftTechControls(goodsType, softTechCategory)
        .thenCombineAsync(frontendStage, (controls, result) ->
                renderFunction.apply(new NotApplicableDisplayCommon(controlCodeSubJourney, result.getFrontendControlCode(), controls))
            , httpExecutionContext.current());
  }

  public CompletionStage<Result> softTechControlsRelatedToPhysicalGoodVariant(ControlCodeSubJourney controlCodeSubJourney, Function<NotApplicableDisplayCommon, Result> renderFunction) {
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
        .thenCombineAsync(frontendStage, (controls, result) ->
                renderFunction.apply(new NotApplicableDisplayCommon(controlCodeSubJourney, result.getFrontendControlCode(), controls))
            , httpExecutionContext.current());
  }

  public CompletionStage<Result> softTechCatchallControlsVariant(ControlCodeSubJourney controlCodeSubJourney, Function<NotApplicableDisplayCommon, Result> renderFunction) {
    GoodsType goodsType = controlCodeSubJourney.getSoftTechGoodsType();
    // If on the software control journey, check the amount of applicable controls. This feeds into the display logic
    String selectedControlCode = permissionsFinderDao.getSelectedControlCode(controlCodeSubJourney);
    CompletionStage<FrontendServiceResult> frontendStage = frontendServiceClient.get(selectedControlCode);

    SoftTechCategory softTechCategory = permissionsFinderDao.getSoftTechCategory(goodsType).get();

    return softTechJourneyHelper.checkCatchtallSoftwareControls(goodsType, softTechCategory)
        .thenCombineAsync(frontendStage, (controls, result) ->
                renderFunction.apply(new NotApplicableDisplayCommon(controlCodeSubJourney, result.getFrontendControlCode(), controls))
            , httpExecutionContext.current());
  }

  public CompletionStage<Result> renderFormInternal(ControlCodeSubJourney controlCodeSubJourney, Function<NotApplicableDisplayCommon, Result> renderFunction) {
    if (controlCodeSubJourney.isPhysicalGoodsSearchVariant()) {
      return physicalGoodsSearchVariant(controlCodeSubJourney, renderFunction);
    }
    else if (controlCodeSubJourney.isSoftTechControlsVariant()) {
      return softTechControlsVariantHandleSubmit(controlCodeSubJourney, renderFunction);
    }
    else if (controlCodeSubJourney.isSoftTechControlsRelatedToPhysicalGoodVariant()) {
      return softTechControlsRelatedToPhysicalGoodVariant(controlCodeSubJourney, renderFunction);
    }
    else if (controlCodeSubJourney.isSoftTechCatchallControlsVariant()) {
      return softTechCatchallControlsVariant(controlCodeSubJourney, renderFunction);
    }
    else {
      throw new RuntimeException(String.format("Unexpected member of ControlCodeSubJourney enum: \"%s\""
          , controlCodeSubJourney.toString()));
    }
  }
}
