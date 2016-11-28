package journey.helpers;

import com.google.inject.Inject;
import components.common.journey.JourneyManager;
import components.persistence.PermissionsFinderDao;
import journey.Events;
import models.ControlCodeFlowStage;
import models.controlcode.ControlCodeJourney;
import models.software.ApplicableSoftwareControls;
import models.software.ControlsRelatedToPhysicalGoodsFlow;
import models.software.SoftwareCategory;
import play.libs.concurrent.HttpExecutionContext;
import play.mvc.Result;

import java.util.concurrent.CompletionStage;

public class ControlCodeJourneyHelper {

  private final JourneyManager journeyManager;
  private final SoftwareJourneyHelper softwareJourneyHelper;
  private final PermissionsFinderDao permissionsFinderDao;
  private final HttpExecutionContext httpExecutionContext;

  @Inject
  public ControlCodeJourneyHelper(JourneyManager journeyManager,
                                  SoftwareJourneyHelper softwareJourneyHelper,
                                  PermissionsFinderDao permissionsFinderDao,
                                  HttpExecutionContext httpExecutionContext) {
    this.journeyManager = journeyManager;
    this.softwareJourneyHelper = softwareJourneyHelper;
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
      return softwareJourneyHelper.checkSoftwareControls(softwareCategory)
          .thenComposeAsync(asc ->
                  journeyManager.performTransition(Events.CONTROL_CODE_SOFTWARE_CONTROLS_NOT_APPLICABLE, asc)
              , httpExecutionContext.current());
    }
    else if (controlCodeJourney == ControlCodeJourney.SOFTWARE_CONTROLS_RELATED_TO_A_PHYSICAL_GOOD) {
      String controlCode = permissionsFinderDao.getSelectedControlCode(ControlCodeJourney.PHYSICAL_GOODS_SEARCH_RELATED_TO_SOFTWARE);
      return softwareJourneyHelper.checkRelatedSoftwareControls(controlCode, false)
          .thenComposeAsync(asc ->
                  journeyManager.performTransition(Events.CONTROL_CODE_SOFTWARE_CONTROLS_NOT_APPLICABLE, asc)
              , httpExecutionContext.current());
    }
    else if (controlCodeJourney == ControlCodeJourney.SOFTWARE_CATCHALL_CONTROLS) {
      return softwareJourneyHelper.performCatchallSoftwareControlNotApplicableTransition();
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
      return softwareJourneyHelper.checkRelatedSoftwareControls(controlCode, true) // Save to DAO if one result returned
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

  private CompletionStage<Result> controlsRelatedToPhysicalGoodTransition(ApplicableSoftwareControls applicableSoftwareControls) {
    if (applicableSoftwareControls == ApplicableSoftwareControls.ZERO) {
      SoftwareCategory softwareCategory = permissionsFinderDao.getSoftwareCategory().get();
      return softwareJourneyHelper.checkCatchtallSoftwareControls(softwareCategory, false)
          .thenComposeAsync(controls -> {
            if (controls == ApplicableSoftwareControls.ZERO) {
              return journeyManager.performTransition(Events.CONTROLS_RELATED_PHYSICAL_GOOD,
                  ControlsRelatedToPhysicalGoodsFlow.SOFTWARE_CONTROL_CATCHALL_ZERO);
            }
            else if (controls == ApplicableSoftwareControls.ONE || controls == ApplicableSoftwareControls.GREATER_THAN_ONE) {
              return journeyManager.performTransition(Events.CONTROLS_RELATED_PHYSICAL_GOOD,
                  ControlsRelatedToPhysicalGoodsFlow.SOFTWARE_CONTROL_CATCHALL_CONTROL_GREATER_THAN_ZERO);
            }
            else {
              throw new RuntimeException(String.format("Unexpected member of ApplicableSoftwareControls enum: \"%s\""
                  , controls.toString()));
            }
          }, httpExecutionContext.current());
    }
    else if (applicableSoftwareControls == ApplicableSoftwareControls.ONE) {
      return journeyManager.performTransition(Events.CONTROLS_RELATED_PHYSICAL_GOOD,
          ControlsRelatedToPhysicalGoodsFlow.SOFTWARE_CONTROL_ONE);
    }
    else if (applicableSoftwareControls == ApplicableSoftwareControls.GREATER_THAN_ONE) {
      return journeyManager.performTransition(Events.CONTROLS_RELATED_PHYSICAL_GOOD,
          ControlsRelatedToPhysicalGoodsFlow.SOFTWARE_CONTROL_GREATER_THAN_ONE);
    }
    else {
      throw new RuntimeException(String.format("Unexpected member of ApplicableSoftwareControls enum: \"%s\""
          , applicableSoftwareControls.toString()));
    }
  }
}
