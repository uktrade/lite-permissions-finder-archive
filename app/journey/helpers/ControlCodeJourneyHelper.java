package journey.helpers;

import static java.util.concurrent.CompletableFuture.completedFuture;

import com.google.inject.Inject;
import components.common.journey.JourneyManager;
import components.persistence.PermissionsFinderDao;
import journey.Events;
import models.ControlCodeFlowStage;
import models.GoodsType;
import models.controlcode.ControlCodeJourney;
import models.softtech.ApplicableSoftTechControls;
import models.softtech.ControlsRelatedToPhysicalGoodsFlow;
import models.softtech.SoftTechCategory;
import org.apache.commons.lang3.StringUtils;
import play.libs.concurrent.HttpExecutionContext;
import play.mvc.Result;

import java.util.concurrent.CompletionStage;
import java.util.function.Function;

public class ControlCodeJourneyHelper {

  private final JourneyManager journeyManager;
  private final SoftTechJourneyHelper softTechJourneyHelper;
  private final PermissionsFinderDao permissionsFinderDao;
  private final HttpExecutionContext httpExecutionContext;

  @Inject
  public ControlCodeJourneyHelper(JourneyManager journeyManager,
                                  SoftTechJourneyHelper softTechJourneyHelper,
                                  PermissionsFinderDao permissionsFinderDao,
                                  HttpExecutionContext httpExecutionContext) {
    this.journeyManager = journeyManager;
    this.softTechJourneyHelper = softTechJourneyHelper;
    this.permissionsFinderDao = permissionsFinderDao;
    this.httpExecutionContext = httpExecutionContext;
  }

  public CompletionStage<Result> notApplicableJourneyTransition(ControlCodeJourney controlCodeJourney) {
    if (ControlCodeJourney.isPhysicalGoodsSearchVariant(controlCodeJourney)) {
      return journeyManager.performTransition(Events.CONTROL_CODE_FLOW_NEXT, ControlCodeFlowStage.NOT_APPLICABLE);
    }
    else if (ControlCodeJourney.isSoftTechControlsVariant(controlCodeJourney)) {
      GoodsType goodsType = controlCodeJourney == ControlCodeJourney.SOFTWARE_CONTROLS
          ? GoodsType.SOFTWARE
          : GoodsType.TECHNOLOGY;
      SoftTechCategory softTechCategory = permissionsFinderDao.getSoftTechCategory(goodsType).get();
      return softTechJourneyHelper.checkSoftTechControls(goodsType, softTechCategory, false)
          .thenComposeAsync(asc ->
                  journeyManager.performTransition(Events.CONTROL_CODE_SOFT_TECH_CONTROLS_NOT_APPLICABLE, asc)
              , httpExecutionContext.current());
    }
    else if (controlCodeJourney == ControlCodeJourney.SOFTWARE_CONTROLS_RELATED_TO_A_PHYSICAL_GOOD) {
      String controlCode = permissionsFinderDao.getSelectedControlCode(ControlCodeJourney.PHYSICAL_GOODS_SEARCH_RELATED_TO_SOFTWARE);
      return softTechJourneyHelper.checkRelatedSoftwareControls(GoodsType.SOFTWARE, controlCode, false)
          .thenComposeAsync(asc ->
                  journeyManager.performTransition(Events.CONTROL_CODE_SOFT_TECH_CONTROLS_NOT_APPLICABLE, asc)
              , httpExecutionContext.current());
    }
    else if (controlCodeJourney == ControlCodeJourney.TECHNOLOGY_CONTROLS_RELATED_TO_A_PHYSICAL_GOOD) {
      String controlCode = permissionsFinderDao.getSelectedControlCode(ControlCodeJourney.PHYSICAL_GOODS_SEARCH_RELATED_TO_TECHNOLOGY);
      return softTechJourneyHelper.checkRelatedSoftwareControls(GoodsType.TECHNOLOGY, controlCode, false)
          .thenComposeAsync(asc ->
                  journeyManager.performTransition(Events.CONTROL_CODE_SOFT_TECH_CONTROLS_NOT_APPLICABLE, asc)
              , httpExecutionContext.current());
    }
    else if (ControlCodeJourney.isSoftTechCatchallControlsVariant(controlCodeJourney)) {
      GoodsType goodsType = controlCodeJourney == ControlCodeJourney.SOFTWARE_CATCHALL_CONTROLS
          ? GoodsType.SOFTWARE
          : GoodsType.TECHNOLOGY;
      return softTechJourneyHelper.performCatchallSoftTechControlNotApplicableTransition(goodsType);
    }
    else {
      throw new RuntimeException(String.format("Unexpected member of ControlCodeJourney enum: \"%s\""
          , controlCodeJourney.toString()));
    }
  }

  public CompletionStage<Result> confirmedJourneyTransition(ControlCodeJourney controlCodeJourney, String controlCode) {
    if (controlCodeJourney == ControlCodeJourney.PHYSICAL_GOODS_SEARCH ||
        ControlCodeJourney.isSoftTechControlsVariant(controlCodeJourney) ||
        ControlCodeJourney.isSoftTechControlsRelatedToPhysicalGoodVariant(controlCodeJourney) ||
        ControlCodeJourney.isSoftTechCatchallControlsVariant(controlCodeJourney)) {
      permissionsFinderDao.saveConfirmedControlCode(controlCode);
      return journeyManager.performTransition(Events.CONTROL_CODE_FLOW_NEXT, ControlCodeFlowStage.CONFIRMED);
    }
    else if (controlCodeJourney == ControlCodeJourney.PHYSICAL_GOODS_SEARCH_RELATED_TO_SOFTWARE) {
      return softTechJourneyHelper.checkRelatedSoftwareControls(GoodsType.SOFTWARE, controlCode, true) // Save to DAO if one result returned
          .thenComposeAsync(applicableSoftTechControls -> controlsRelatedToPhysicalGoodTransition(GoodsType.SOFTWARE, applicableSoftTechControls), httpExecutionContext.current());
    }
    else if (controlCodeJourney == ControlCodeJourney.PHYSICAL_GOODS_SEARCH_RELATED_TO_TECHNOLOGY) {
      return softTechJourneyHelper.checkRelatedSoftwareControls(GoodsType.TECHNOLOGY, controlCode, true) // Save to DAO if one result returned
          .thenComposeAsync(applicableSoftTechControls -> controlsRelatedToPhysicalGoodTransition(GoodsType.TECHNOLOGY, applicableSoftTechControls), httpExecutionContext.current());
    }
    else {
      throw new RuntimeException(String.format("Unexpected member of ControlCodeJourney enum: \"%s\""
          , controlCodeJourney.toString()));
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
                                                                       ControlCodeJourney softwareJourney,
                                                                       ControlCodeJourney technologyJourney,
                                                                       Function<ControlCodeJourney, CompletionStage<Result>> resultFunc) {
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

  public static CompletionStage<Result> getSearchRelatedToPhysicalGoodsResult(String goodsTypeText, Function<ControlCodeJourney, CompletionStage<Result>> resultFunc) {
    return validateGoodsTypeAndGetResult(goodsTypeText, ControlCodeJourney.PHYSICAL_GOODS_SEARCH_RELATED_TO_SOFTWARE,
        ControlCodeJourney.PHYSICAL_GOODS_SEARCH_RELATED_TO_TECHNOLOGY, resultFunc);
  }

  public static CompletionStage<Result> getCatchAllControlsResult(String goodsTypeText, Function<ControlCodeJourney, CompletionStage<Result>> resultFunc) {
    return validateGoodsTypeAndGetResult(goodsTypeText, ControlCodeJourney.SOFTWARE_CATCHALL_CONTROLS,
        ControlCodeJourney.TECHNOLOGY_CATCHALL_CONTROLS, resultFunc);
  }

  public static CompletionStage<Result> getControlsResult(String goodsTypeText, Function<ControlCodeJourney, CompletionStage<Result>> resultFunc) {
    return validateGoodsTypeAndGetResult(goodsTypeText, ControlCodeJourney.SOFTWARE_CONTROLS,
        ControlCodeJourney.TECHNOLOGY_CONTROLS, resultFunc);
  }

  public static CompletionStage<Result> getRelatedControlsResult(String goodsTypeText, Function<ControlCodeJourney, CompletionStage<Result>> resultFunc) {
    return validateGoodsTypeAndGetResult(goodsTypeText, ControlCodeJourney.SOFTWARE_CONTROLS_RELATED_TO_A_PHYSICAL_GOOD,
        ControlCodeJourney.TECHNOLOGY_CONTROLS_RELATED_TO_A_PHYSICAL_GOOD, resultFunc);
  }

  /**
   * Checks the number of results for a given control code journey to determine whether a results screen has been shown
   * and is able to be transitioned back too. If there is more than one result, this function returns true.
   * @param controlCodeJourney the control code journey variant
   * @return whether there is a prior results screen in which the user can traverse back too
   */
  public CompletionStage<Boolean> getCanPickFromResultsAgain(ControlCodeJourney controlCodeJourney){
    if (controlCodeJourney == ControlCodeJourney.PHYSICAL_GOODS_SEARCH ||
        controlCodeJourney == ControlCodeJourney.PHYSICAL_GOODS_SEARCH_RELATED_TO_SOFTWARE ||
        controlCodeJourney == ControlCodeJourney.PHYSICAL_GOODS_SEARCH_RELATED_TO_TECHNOLOGY) {
      return completedFuture(true);
    }
    else if (controlCodeJourney == ControlCodeJourney.SOFTWARE_CONTROLS ||
        controlCodeJourney == ControlCodeJourney.TECHNOLOGY_CONTROLS) {
      GoodsType goodsType = controlCodeJourney == ControlCodeJourney.SOFTWARE_CONTROLS
          ? GoodsType.SOFTWARE
          : GoodsType.TECHNOLOGY;
      SoftTechCategory softTechCategory = permissionsFinderDao.getSoftTechCategory(goodsType).get();
      return softTechJourneyHelper.checkSoftTechControls(goodsType, softTechCategory, false)
          .thenApplyAsync(this::canPickFromResultsAgain);
    }
    else if (controlCodeJourney == ControlCodeJourney.SOFTWARE_CONTROLS_RELATED_TO_A_PHYSICAL_GOOD ||
        controlCodeJourney == ControlCodeJourney.TECHNOLOGY_CONTROLS_RELATED_TO_A_PHYSICAL_GOOD) {
      GoodsType goodsType = controlCodeJourney == ControlCodeJourney.SOFTWARE_CONTROLS_RELATED_TO_A_PHYSICAL_GOOD
          ? GoodsType.SOFTWARE
          : GoodsType.TECHNOLOGY;
      String controlCode = permissionsFinderDao.getSelectedControlCode(controlCodeJourney);
      return softTechJourneyHelper.checkRelatedSoftwareControls(goodsType, controlCode, false)
          .thenApplyAsync(this::canPickFromResultsAgain);
    }
    else if (controlCodeJourney == ControlCodeJourney.SOFTWARE_CATCHALL_CONTROLS ||
        controlCodeJourney == ControlCodeJourney.TECHNOLOGY_CATCHALL_CONTROLS) {
      GoodsType goodsType = controlCodeJourney == ControlCodeJourney.SOFTWARE_CATCHALL_CONTROLS
          ? GoodsType.SOFTWARE
          : GoodsType.TECHNOLOGY;
      SoftTechCategory softTechCategory = permissionsFinderDao.getSoftTechCategory(goodsType).get();
      return softTechJourneyHelper.checkCatchtallSoftwareControls(goodsType, softTechCategory, false)
          .thenApplyAsync(this::canPickFromResultsAgain, httpExecutionContext.current());
    }
    else {
      throw new RuntimeException(String.format("Unexpected member of ControlCodeJourney enum: \"%s\""
          , controlCodeJourney.toString()));
    }
  }

  private Boolean canPickFromResultsAgain (ApplicableSoftTechControls applicableSoftTechControls) {
    switch (applicableSoftTechControls) {
      case ONE:
        return false;
      case GREATER_THAN_ONE:
        return true;
      default:
        // ZERO or null
        throw new RuntimeException(String.format("Unexpected member of ApplicableSoftTechControls enum: \"%s\""
            , applicableSoftTechControls.toString()));
    }
  }

}
