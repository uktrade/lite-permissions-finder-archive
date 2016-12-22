package models.controlcode;

import components.services.controlcode.Ancestor;
import components.services.controlcode.ControlCodeData;
import components.services.controlcode.FrontendServiceResult;
import controllers.controlcode.routes;
import models.GoodsType;

import java.util.List;
import java.util.stream.Collectors;

public class DecontrolsDisplay {
  public final String formAction;
  public final String title;
  public final String friendlyDescription;
  public final String controlCodeAlias;
  public final Ancestor greatestAncestor;
  public final List<Ancestor> otherAncestors;
  public final boolean showGreatestAncestor;
  public final List<String> decontrols;

  public DecontrolsDisplay(ControlCodeJourney controlCodeJourney, FrontendServiceResult frontendServiceResult) {
    ControlCodeData controlCodeData = frontendServiceResult.controlCodeData;
    this.title = controlCodeData.title;
    this.friendlyDescription = controlCodeData.friendlyDescription;
    this.controlCodeAlias = controlCodeData.alias;
    if (frontendServiceResult.greatestAncestor.isPresent()) {
      this.greatestAncestor = frontendServiceResult.greatestAncestor.get();
      showGreatestAncestor = true;
    }
    else {
      this.greatestAncestor = null;
      showGreatestAncestor = false;
    }
    this.otherAncestors = frontendServiceResult.otherAncestors;
    this.decontrols = controlCodeData.decontrols.stream()
        .map(decontrol -> decontrol.text)
        .collect(Collectors.toList());
    if (controlCodeJourney == ControlCodeJourney.PHYSICAL_GOODS_SEARCH) {
      this.formAction = routes.DecontrolsController.handleSearchSubmit().url();
    }
    else if (controlCodeJourney == ControlCodeJourney.PHYSICAL_GOODS_SEARCH_RELATED_TO_SOFTWARE) {
      this.formAction = routes.DecontrolsController.handleSearchRelatedToSubmit(GoodsType.SOFTWARE.urlString()).url();
    }
    else if (controlCodeJourney == ControlCodeJourney.PHYSICAL_GOODS_SEARCH_RELATED_TO_TECHNOLOGY) {
      this.formAction = routes.DecontrolsController.handleSearchRelatedToSubmit(GoodsType.TECHNOLOGY.urlString()).url();
    }
    else if (controlCodeJourney == ControlCodeJourney.SOFTWARE_CONTROLS) {
      this.formAction = routes.DecontrolsController.handleControlsSubmit(GoodsType.SOFTWARE.urlString()).url();
    }
    else if (controlCodeJourney == ControlCodeJourney.TECHNOLOGY_CONTROLS) {
      this.formAction = routes.DecontrolsController.handleControlsSubmit(GoodsType.TECHNOLOGY.urlString()).url();
    }
    else if (controlCodeJourney == ControlCodeJourney.SOFTWARE_CONTROLS_RELATED_TO_A_PHYSICAL_GOOD) {
      this.formAction = routes.DecontrolsController.handleRelatedControlsSubmit(GoodsType.SOFTWARE.urlString()).url();
    }
    else if (controlCodeJourney == ControlCodeJourney.TECHNOLOGY_CONTROLS_RELATED_TO_A_PHYSICAL_GOOD) {
      this.formAction = routes.DecontrolsController.handleRelatedControlsSubmit(GoodsType.TECHNOLOGY.urlString()).url();
    }
    else if (controlCodeJourney == ControlCodeJourney.SOFTWARE_CATCHALL_CONTROLS) {
      this.formAction = routes.DecontrolsController.handleCatchallControlsSubmit(GoodsType.SOFTWARE.urlString()).url();
    }
    else if (controlCodeJourney == ControlCodeJourney.TECHNOLOGY_CATCHALL_CONTROLS) {
      this.formAction = routes.DecontrolsController.handleCatchallControlsSubmit(GoodsType.TECHNOLOGY.urlString()).url();
    }
    else {
      throw new RuntimeException(String.format("Unexpected member of ControlCodeJourney enum: \"%s\""
          , controlCodeJourney.toString()));
    }
  }

}
