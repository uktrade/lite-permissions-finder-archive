package journey.helpers;

import com.google.inject.Inject;
import components.common.journey.JourneyManager;
import components.persistence.PermissionsFinderDao;
import journey.Events;
import models.ControlCodeFlowStage;
import models.GoodsType;
import models.controlcode.ControlCodeSubJourney;
import models.softtech.ApplicableSoftTechControls;
import models.softtech.ControlsRelatedToPhysicalGoodsFlow;
import models.softtech.SoftTechCategory;
import org.apache.commons.lang3.StringUtils;
import play.libs.concurrent.HttpExecutionContext;
import play.mvc.Result;

import java.util.concurrent.CompletionStage;
import java.util.function.Function;

public class ControlCodeSubJourneyHelper {

  private final JourneyManager journeyManager;
  private final SoftTechJourneyHelper softTechJourneyHelper;
  private final PermissionsFinderDao permissionsFinderDao;
  private final HttpExecutionContext httpExecutionContext;

  @Inject
  public ControlCodeSubJourneyHelper(JourneyManager journeyManager,
                                     SoftTechJourneyHelper softTechJourneyHelper,
                                     PermissionsFinderDao permissionsFinderDao,
                                     HttpExecutionContext httpExecutionContext) {
    this.journeyManager = journeyManager;
    this.softTechJourneyHelper = softTechJourneyHelper;
    this.permissionsFinderDao = permissionsFinderDao;
    this.httpExecutionContext = httpExecutionContext;
  }

  public CompletionStage<Result> notApplicableJourneyTransition(ControlCodeSubJourney controlCodeSubJourney) {
    if (models.controlcode.ControlCodeSubJourney.isPhysicalGoodsSearchVariant(controlCodeSubJourney)) {
      return journeyManager.performTransition(Events.CONTROL_CODE_FLOW_NEXT, ControlCodeFlowStage.NOT_APPLICABLE);
    }
    else if (models.controlcode.ControlCodeSubJourney.isSoftTechControlsVariant(controlCodeSubJourney)) {
      GoodsType goodsType = controlCodeSubJourney.getSoftTechGoodsType();
      SoftTechCategory softTechCategory = permissionsFinderDao.getSoftTechCategory(goodsType).get();
      return softTechJourneyHelper.checkSoftTechControls(goodsType, softTechCategory, false)
          .thenComposeAsync(asc ->
                  journeyManager.performTransition(Events.CONTROL_CODE_SOFT_TECH_CONTROLS_NOT_APPLICABLE, asc)
              , httpExecutionContext.current());
    }
    else if (controlCodeSubJourney == models.controlcode.ControlCodeSubJourney.SOFTWARE_CONTROLS_RELATED_TO_A_PHYSICAL_GOOD) {
      String controlCode = permissionsFinderDao.getSelectedControlCode(models.controlcode.ControlCodeSubJourney.PHYSICAL_GOODS_SEARCH_RELATED_TO_SOFTWARE);
      return softTechJourneyHelper.checkRelatedSoftwareControls(GoodsType.SOFTWARE, controlCode, false)
          .thenComposeAsync(asc ->
                  journeyManager.performTransition(Events.CONTROL_CODE_SOFT_TECH_CONTROLS_NOT_APPLICABLE, asc)
              , httpExecutionContext.current());
    }
    else if (controlCodeSubJourney == models.controlcode.ControlCodeSubJourney.TECHNOLOGY_CONTROLS_RELATED_TO_A_PHYSICAL_GOOD) {
      String controlCode = permissionsFinderDao.getSelectedControlCode(models.controlcode.ControlCodeSubJourney.PHYSICAL_GOODS_SEARCH_RELATED_TO_TECHNOLOGY);
      return softTechJourneyHelper.checkRelatedSoftwareControls(GoodsType.TECHNOLOGY, controlCode, false)
          .thenComposeAsync(asc ->
                  journeyManager.performTransition(Events.CONTROL_CODE_SOFT_TECH_CONTROLS_NOT_APPLICABLE, asc)
              , httpExecutionContext.current());
    }
    else if (models.controlcode.ControlCodeSubJourney.isSoftTechCatchallControlsVariant(controlCodeSubJourney)) {
      GoodsType goodsType = controlCodeSubJourney.getSoftTechGoodsType();
      SoftTechCategory softTechCategory = permissionsFinderDao.getSoftTechCategory(goodsType).get();
      return softTechJourneyHelper.checkCatchtallSoftwareControls(goodsType, softTechCategory, false)
          .thenComposeAsync(asc ->
                  journeyManager.performTransition(Events.CONTROL_CODE_SOFT_TECH_CONTROLS_NOT_APPLICABLE, asc)
              , httpExecutionContext.current());
    }
    else {
      throw new RuntimeException(String.format("Unexpected member of ControlCodeSubJourney enum: \"%s\""
          , controlCodeSubJourney.toString()));
    }
  }

  public CompletionStage<Result> confirmedJourneyTransition(ControlCodeSubJourney controlCodeSubJourney, String controlCode) {
    if (controlCodeSubJourney == models.controlcode.ControlCodeSubJourney.PHYSICAL_GOODS_SEARCH ||
        models.controlcode.ControlCodeSubJourney.isSoftTechControlsVariant(controlCodeSubJourney) ||
        models.controlcode.ControlCodeSubJourney.isSoftTechControlsRelatedToPhysicalGoodVariant(controlCodeSubJourney) ||
        models.controlcode.ControlCodeSubJourney.isSoftTechCatchallControlsVariant(controlCodeSubJourney)) {
      permissionsFinderDao.saveConfirmedControlCode(controlCode);
      return journeyManager.performTransition(Events.CONTROL_CODE_FLOW_NEXT, ControlCodeFlowStage.CONFIRMED);
    }
    else if (controlCodeSubJourney == models.controlcode.ControlCodeSubJourney.PHYSICAL_GOODS_SEARCH_RELATED_TO_SOFTWARE) {
      return softTechJourneyHelper.checkRelatedSoftwareControls(GoodsType.SOFTWARE, controlCode, true) // Save to DAO if one result returned
          .thenComposeAsync(applicableSoftTechControls -> controlsRelatedToPhysicalGoodTransition(GoodsType.SOFTWARE, applicableSoftTechControls), httpExecutionContext.current());
    }
    else if (controlCodeSubJourney == models.controlcode.ControlCodeSubJourney.PHYSICAL_GOODS_SEARCH_RELATED_TO_TECHNOLOGY) {
      return softTechJourneyHelper.checkRelatedSoftwareControls(GoodsType.TECHNOLOGY, controlCode, true) // Save to DAO if one result returned
          .thenComposeAsync(applicableSoftTechControls -> controlsRelatedToPhysicalGoodTransition(GoodsType.TECHNOLOGY, applicableSoftTechControls), httpExecutionContext.current());
    }
    else {
      throw new RuntimeException(String.format("Unexpected member of ControlCodeSubJourney enum: \"%s\""
          , controlCodeSubJourney.toString()));
    }
  }

  private CompletionStage<Result> controlsRelatedToPhysicalGoodTransition(GoodsType goodsType, ApplicableSoftTechControls applicableSoftTechControls) {
    if (applicableSoftTechControls == ApplicableSoftTechControls.ZERO) {
      SoftTechCategory softTechCategory = permissionsFinderDao.getSoftTechCategory(goodsType).get();
      return softTechJourneyHelper.checkCatchtallSoftwareControls(goodsType, softTechCategory, false)
          .thenComposeAsync(controls -> {
            if (controls == ApplicableSoftTechControls.ZERO) {
              return journeyManager.performTransition(Events.CONTROLS_RELATED_PHYSICAL_GOOD,
                  ControlsRelatedToPhysicalGoodsFlow.SOFT_TECH_CONTROL_CATCHALL_ZERO);
            }
            else if (controls == ApplicableSoftTechControls.ONE || controls == ApplicableSoftTechControls.GREATER_THAN_ONE) {
              return journeyManager.performTransition(Events.CONTROLS_RELATED_PHYSICAL_GOOD,
                  ControlsRelatedToPhysicalGoodsFlow.SOFT_TECH_CONTROL_CATCHALL_CONTROL_GREATER_THAN_ZERO);
            }
            else {
              throw new RuntimeException(String.format("Unexpected member of ApplicableSoftTechControls enum: \"%s\""
                  , controls.toString()));
            }
          }, httpExecutionContext.current());
    }
    else if (applicableSoftTechControls == ApplicableSoftTechControls.ONE) {
      return journeyManager.performTransition(Events.CONTROLS_RELATED_PHYSICAL_GOOD,
          ControlsRelatedToPhysicalGoodsFlow.SOFT_TECH_CONTROL_ONE);
    }
    else if (applicableSoftTechControls == ApplicableSoftTechControls.GREATER_THAN_ONE) {
      return journeyManager.performTransition(Events.CONTROLS_RELATED_PHYSICAL_GOOD,
          ControlsRelatedToPhysicalGoodsFlow.SOFT_TECH_CONTROL_GREATER_THAN_ONE);
    }
    else {
      throw new RuntimeException(String.format("Unexpected member of ApplicableSoftTechControls enum: \"%s\""
          , applicableSoftTechControls.toString()));
    }
  }

  private static CompletionStage<Result> validateGoodsTypeAndGetResult(String goodsTypeText,
                                                                       ControlCodeSubJourney softwareJourney,
                                                                       ControlCodeSubJourney technologyJourney,
                                                                       Function<ControlCodeSubJourney, CompletionStage<Result>> resultFunc) {
    if (StringUtils.isNotEmpty(goodsTypeText)) {
      GoodsType goodsType = GoodsType.valueOf(goodsTypeText.toUpperCase());
      if (goodsType == GoodsType.SOFTWARE) {
        return resultFunc.apply(softwareJourney);
      }
      else if (goodsType == GoodsType.TECHNOLOGY) {
        return resultFunc.apply(technologyJourney);
      }
      else {
        throw new RuntimeException(String.format("Unexpected member of GoodsType enum: \"%s\""
            , goodsType.toString()));
      }
    }
    else {
      throw new RuntimeException(String.format("Expected goodsTypeText to not be empty"));
    }
  }

  public static CompletionStage<Result> getSearchRelatedToPhysicalGoodsResult(String goodsTypeText, Function<ControlCodeSubJourney, CompletionStage<Result>> resultFunc) {
    return validateGoodsTypeAndGetResult(goodsTypeText, models.controlcode.ControlCodeSubJourney.PHYSICAL_GOODS_SEARCH_RELATED_TO_SOFTWARE,
        models.controlcode.ControlCodeSubJourney.PHYSICAL_GOODS_SEARCH_RELATED_TO_TECHNOLOGY, resultFunc);
  }

  public static CompletionStage<Result> getCatchAllControlsResult(String goodsTypeText, Function<ControlCodeSubJourney, CompletionStage<Result>> resultFunc) {
    return validateGoodsTypeAndGetResult(goodsTypeText, models.controlcode.ControlCodeSubJourney.SOFTWARE_CATCHALL_CONTROLS,
        models.controlcode.ControlCodeSubJourney.TECHNOLOGY_CATCHALL_CONTROLS, resultFunc);
  }

  public static CompletionStage<Result> getControlsResult(String goodsTypeText, Function<ControlCodeSubJourney, CompletionStage<Result>> resultFunc) {
    return validateGoodsTypeAndGetResult(goodsTypeText, models.controlcode.ControlCodeSubJourney.SOFTWARE_CONTROLS,
        models.controlcode.ControlCodeSubJourney.TECHNOLOGY_CONTROLS, resultFunc);
  }

  public static CompletionStage<Result> getRelatedControlsResult(String goodsTypeText, Function<ControlCodeSubJourney, CompletionStage<Result>> resultFunc) {
    return validateGoodsTypeAndGetResult(goodsTypeText, models.controlcode.ControlCodeSubJourney.SOFTWARE_CONTROLS_RELATED_TO_A_PHYSICAL_GOOD,
        models.controlcode.ControlCodeSubJourney.TECHNOLOGY_CONTROLS_RELATED_TO_A_PHYSICAL_GOOD, resultFunc);
  }

}
