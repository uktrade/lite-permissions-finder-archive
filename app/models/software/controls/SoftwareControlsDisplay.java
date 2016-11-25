package models.software.controls;

import components.services.controlcode.controls.ControlCode;
import controllers.software.controls.routes;
import java.util.List;

public class SoftwareControlsDisplay {

  public final String formAction;
  public final String pageTitle;
  public List<ControlCode> controlCodes;

  public SoftwareControlsDisplay(SoftwareControlsJourney softwareControlsJourney, List<ControlCode> controlCodes) {
    if (softwareControlsJourney == SoftwareControlsJourney.SOFTWARE_CATEGORY) {
      this.formAction = routes.SoftwareControlsController.handleSoftwareCategorySubmit().url();
      this.pageTitle = "Showing controls related to software category";
    }
    else if (softwareControlsJourney == SoftwareControlsJourney.SOFTWARE_RELATED_TO_A_PHYSICAL_GOOD) {
      this.formAction = routes.SoftwareControlsController.handleRelatedToPhysicalGoodSubmit().url();
      this.pageTitle = "Showing controls related to your selected physical good";
    }
    else if (softwareControlsJourney == SoftwareControlsJourney.SOFTWARE_CATCHALL) {
      this.formAction = routes.SoftwareControlsController.handleSoftwareCatchallSubmit().url();
      this.pageTitle = "Showing catchall controls related to your items category";
    }
    else {
      throw new RuntimeException(String.format("Unexpected member of SoftwareControlsJourney enum: \"%s\""
          , softwareControlsJourney.toString()));
    }
    this.controlCodes = controlCodes;
  }
}
