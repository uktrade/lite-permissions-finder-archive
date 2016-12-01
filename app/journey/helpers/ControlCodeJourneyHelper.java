package journey.helpers;

import com.google.inject.Inject;
import components.common.journey.JourneyManager;
import components.persistence.PermissionsFinderDao;
import journey.Events;
import models.ControlCodeFlowStage;
import models.controlcode.ControlCodeJourney;
import models.softtech.ApplicableSoftTechControls;
import models.softtech.ControlsRelatedToPhysicalGoodsFlow;
import models.softtech.SoftwareCategory;
import play.libs.concurrent.HttpExecutionContext;
import play.mvc.Result;

import java.util.concurrent.CompletionStage;

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
    if (controlCodeJourney == ControlCodeJourney.PHYSICAL_GOODS_SEARCH) {
      return journeyManager.performTransition(Events.CONTROL_CODE_FLOW_NEXT, ControlCodeFlowStage.NOT_APPLICABLE);
    }
    else if (controlCodeJourney == ControlCodeJourney.PHYSICAL_GOODS_SEARCH_RELATED_TO_SOFTWARE) {
      return journeyManager.performTransition(Events.CONTROL_CODE_FLOW_NEXT, ControlCodeFlowStage.NOT_APPLICABLE);
    }
    else if (controlCodeJourney == ControlCodeJourney.SOFTWARE_CONTROLS) {
      SoftwareCategory softwareCategory = permissionsFinderDao.getSoftwareCategory().get();
      return softTechJourneyHelper.checkSoftwareControls(softwareCategory)
          .thenComposeAsync(asc ->
                  journeyManager.performTransition(Events.CONTROL_CODE_SOFTWARE_CONTROLS_NOT_APPLICABLE, asc)
              , httpExecutionContext.current());
    }
    else if (controlCodeJourney == ControlCodeJourney.SOFTWARE_CONTROLS_RELATED_TO_A_PHYSICAL_GOOD) {
      String controlCode = permissionsFinderDao.getSelectedControlCode(ControlCodeJourney.PHYSICAL_GOODS_SEARCH_RELATED_TO_SOFTWARE);
      return softTechJourneyHelper.checkRelatedSoftwareControls(controlCode, false)
          .thenComposeAsync(asc ->
                  journeyManager.performTransition(Events.CONTROL_CODE_SOFTWARE_CONTROLS_NOT_APPLICABLE, asc)
              , httpExecutionContext.current());
    }
    else if (controlCodeJourney == ControlCodeJourney.SOFTWARE_CATCHALL_CONTROLS) {
      return softTechJourneyHelper.performCatchallSoftwareControlNotApplicableTransition();
    }
    else {
      throw new RuntimeException(String.format("Unexpected member of ControlCodeJourney enum: \"%s\""
          , controlCodeJourney.toString()));
    }
  }

  public CompletionStage<Result> confirmedJourneyTransition(ControlCodeJourney controlCodeJourney, String controlCode) {
    if (controlCodeJourney == ControlCodeJourney.PHYSICAL_GOODS_SEARCH) {
      permissionsFinderDao.saveConfirmedControlCode(controlCode);
      return journeyManager.performTransition(Events.CONTROL_CODE_FLOW_NEXT, ControlCodeFlowStage.CONFIRMED);
    }
    else if (controlCodeJourney == ControlCodeJourney.PHYSICAL_GOODS_SEARCH_RELATED_TO_SOFTWARE) {
      return softTechJourneyHelper.checkRelatedSoftwareControls(controlCode, true) // Save to DAO if one result returned
          .thenComposeAsync(this::controlsRelatedToPhysicalGoodTransition, httpExecutionContext.current());
    }
    else if (controlCodeJourney == ControlCodeJourney.SOFTWARE_CONTROLS) {
      permissionsFinderDao.saveConfirmedControlCode(controlCode);
      return journeyManager.performTransition(Events.CONTROL_CODE_FLOW_NEXT, ControlCodeFlowStage.CONFIRMED);
    }
    else if (controlCodeJourney == ControlCodeJourney.SOFTWARE_CONTROLS_RELATED_TO_A_PHYSICAL_GOOD) {
      permissionsFinderDao.saveConfirmedControlCode(controlCode);
      return journeyManager.performTransition(Events.CONTROL_CODE_FLOW_NEXT, ControlCodeFlowStage.CONFIRMED);
    }
    else if (controlCodeJourney == ControlCodeJourney.SOFTWARE_CATCHALL_CONTROLS) {
      permissionsFinderDao.saveConfirmedControlCode(controlCode);
      return journeyManager.performTransition(Events.CONTROL_CODE_FLOW_NEXT, ControlCodeFlowStage.CONFIRMED);
    }
    else {
      throw new RuntimeException(String.format("Unexpected member of ControlCodeJourney enum: \"%s\""
          , controlCodeJourney.toString()));
    }
  }

  private CompletionStage<Result> controlsRelatedToPhysicalGoodTransition(ApplicableSoftTechControls applicableSoftTechControls) {
    if (applicableSoftTechControls == ApplicableSoftTechControls.ZERO) {
      SoftwareCategory softwareCategory = permissionsFinderDao.getSoftwareCategory().get();
      return softTechJourneyHelper.checkCatchtallSoftwareControls(softwareCategory, false)
          .thenComposeAsync(controls -> {
            if (controls == ApplicableSoftTechControls.ZERO) {
              return journeyManager.performTransition(Events.CONTROLS_RELATED_PHYSICAL_GOOD,
                  ControlsRelatedToPhysicalGoodsFlow.SOFTWARE_CONTROL_CATCHALL_ZERO);
            }
            else if (controls == ApplicableSoftTechControls.ONE || controls == ApplicableSoftTechControls.GREATER_THAN_ONE) {
              return journeyManager.performTransition(Events.CONTROLS_RELATED_PHYSICAL_GOOD,
                  ControlsRelatedToPhysicalGoodsFlow.SOFTWARE_CONTROL_CATCHALL_CONTROL_GREATER_THAN_ZERO);
            }
            else {
              throw new RuntimeException(String.format("Unexpected member of ApplicableSoftTechControls enum: \"%s\""
                  , controls.toString()));
            }
          }, httpExecutionContext.current());
    }
    else if (applicableSoftTechControls == ApplicableSoftTechControls.ONE) {
      return journeyManager.performTransition(Events.CONTROLS_RELATED_PHYSICAL_GOOD,
          ControlsRelatedToPhysicalGoodsFlow.SOFTWARE_CONTROL_ONE);
    }
    else if (applicableSoftTechControls == ApplicableSoftTechControls.GREATER_THAN_ONE) {
      return journeyManager.performTransition(Events.CONTROLS_RELATED_PHYSICAL_GOOD,
          ControlsRelatedToPhysicalGoodsFlow.SOFTWARE_CONTROL_GREATER_THAN_ONE);
    }
    else {
      throw new RuntimeException(String.format("Unexpected member of ApplicableSoftTechControls enum: \"%s\""
          , applicableSoftTechControls.toString()));
    }
  }
}
