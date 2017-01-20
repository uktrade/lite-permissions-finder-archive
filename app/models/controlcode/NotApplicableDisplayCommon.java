package models.controlcode;

import components.services.controlcode.FrontendControlCode;
import models.softtech.ApplicableSoftTechControls;

public class NotApplicableDisplayCommon {

  public final ControlCodeSubJourney controlCodeSubJourney;
  public final FrontendControlCode frontendControlCode;
  public final ApplicableSoftTechControls applicableSoftTechControls;

  public static class ActionButton {
    public final String value;
    public final String content;

    public ActionButton(String value, String content) {
      this.value = value;
      this.content = content;
    }

  }

  public NotApplicableDisplayCommon(ControlCodeSubJourney controlCodeSubJourney, FrontendControlCode frontendControlCode, ApplicableSoftTechControls applicableSoftTechControls) {
    this.controlCodeSubJourney = controlCodeSubJourney;
    this.frontendControlCode = frontendControlCode;
    this.applicableSoftTechControls = applicableSoftTechControls;
  }

  public static boolean canPickAgain(ApplicableSoftTechControls applicableSoftTechControls) {
    if (applicableSoftTechControls == ApplicableSoftTechControls.ONE) {
      return false;
    }
    else if (applicableSoftTechControls == ApplicableSoftTechControls.GREATER_THAN_ONE) {
      return true;
    }
    else {
      throw new RuntimeException(String.format("Unexpected member of ApplicableSoftTechControls enum: \"%s\""
          , applicableSoftTechControls.toString()));
    }
  }

}
