package models.softtech.controls;

import components.services.controlcode.controls.ControlCode;
import controllers.softtech.controls.routes;

import java.util.List;

public class SoftTechControlsDisplay {

  public final String formAction;
  public final String pageTitle;
  public List<ControlCode> controlCodes;

  public SoftTechControlsDisplay(SoftTechControlsJourney softTechControlsJourney, List<ControlCode> controlCodes) {
    if (softTechControlsJourney == SoftTechControlsJourney.SOFTWARE_CATEGORY) {
      this.formAction = routes.SoftTechControlsController.handleSoftwareCategorySubmit().url();
      this.pageTitle = "Showing controls related to software category";
    }
    else if (softTechControlsJourney == SoftTechControlsJourney.SOFTWARE_RELATED_TO_A_PHYSICAL_GOOD) {
      this.formAction = routes.SoftTechControlsController.handleRelatedToPhysicalGoodSubmit().url();
      this.pageTitle = "Showing controls related to your selected physical good";
    }
    else if (softTechControlsJourney == SoftTechControlsJourney.SOFTWARE_CATCHALL) {
      this.formAction = routes.SoftTechControlsController.handleSoftwareCatchallSubmit().url();
      this.pageTitle = "Showing catchall controls related to your items category";
    }
    else {
      throw new RuntimeException(String.format("Unexpected member of SoftTechControlsJourney enum: \"%s\""
          , softTechControlsJourney.toString()));
    }
    this.controlCodes = controlCodes;
  }
}
