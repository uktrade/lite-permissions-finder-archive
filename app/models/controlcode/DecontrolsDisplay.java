package models.controlcode;

import components.services.controlcode.ControlCodeData;
import components.services.controlcode.FrontendServiceResult;
import controllers.controlcode.routes;

import java.util.List;
import java.util.stream.Collectors;

public class DecontrolsDisplay {
  public final String formAction;
  public final String title;
  public final String friendlyDescription;
  public final String controlCodeAlias;
  public final List<String> decontrols;

  public DecontrolsDisplay(ControlCodeJourney controlCodeJourney, FrontendServiceResult frontendServiceResult) {
    ControlCodeData controlCodeData = frontendServiceResult.controlCodeData;
    this.title = controlCodeData.title;
    this.friendlyDescription = controlCodeData.friendlyDescription;
    this.controlCodeAlias = controlCodeData.alias;
    this.decontrols = controlCodeData.decontrols.stream()
        .map(decontrol -> decontrol.text)
        .collect(Collectors.toList());
    if (controlCodeJourney == ControlCodeJourney.PHYSICAL_GOODS_SEARCH) {
      this.formAction = routes.DecontrolsController.handleSearchSubmit().url();
    }
    else if (controlCodeJourney == ControlCodeJourney.PHYSICAL_GOODS_SEARCH_RELATED_TO_SOFTWARE) {
      this.formAction = routes.DecontrolsController.handleSearchRelatedToSoftwareSubmit().url();
    }
    else if (controlCodeJourney == ControlCodeJourney.SOFTWARE_CONTROLS) {
      this.formAction = routes.DecontrolsController.handleSoftwareControlsSubmit().url();
    }
    else if (controlCodeJourney == ControlCodeJourney.SOFTWARE_CONTROLS_RELATED_TO_A_PHYSICAL_GOOD) {
      this.formAction = routes.DecontrolsController.handleRelatedSoftwareControlsSubmit().url();
    }
    else if (controlCodeJourney == ControlCodeJourney.SOFTWARE_CATCHALL_CONTROLS) {
      this.formAction = routes.DecontrolsController.handleSoftwareCatchallControlsSubmit().url();
    }
    else {
      throw new RuntimeException(String.format("Unexpected member of ControlCodeJourney enum: \"%s\""
          , controlCodeJourney.toString()));
    }
  }

}
