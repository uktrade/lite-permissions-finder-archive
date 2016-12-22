package models.controlcode;

import components.services.controlcode.Ancestor;
import components.services.controlcode.ControlCodeData;
import components.services.controlcode.FrontendServiceResult;
import controllers.controlcode.routes;
import models.GoodsType;

import java.util.List;

public class TechnicalNotesDisplay {
  public final String formAction;
  public final String title;
  public final String friendlyDescription;
  public final String controlCodeAlias;
  public final Ancestor greatestAncestor;
  public final List<Ancestor> otherAncestors;
  public final boolean showGreatestAncestor;
  public final String technicalNotes;

  public TechnicalNotesDisplay(ControlCodeSubJourney controlCodeSubJourney, FrontendServiceResult frontendServiceResult) {
    ControlCodeData controlCodeData = frontendServiceResult.controlCodeData;
    this.title = controlCodeData.title;
    this.friendlyDescription = controlCodeData.friendlyDescription;
    this.controlCodeAlias = controlCodeData.alias;
    this.technicalNotes = controlCodeData.technicalNotes;
    if (frontendServiceResult.greatestAncestor.isPresent()) {
      this.greatestAncestor = frontendServiceResult.greatestAncestor.get();
      showGreatestAncestor = true;
    }
    else {
      this.greatestAncestor = null;
      showGreatestAncestor = false;
    }
    this.otherAncestors = frontendServiceResult.otherAncestors;
    if (controlCodeSubJourney == models.controlcode.ControlCodeSubJourney.PHYSICAL_GOODS_SEARCH) {
      this.formAction = routes.TechnicalNotesController.handleSearchSubmit().url();
    }
    else if (controlCodeSubJourney == models.controlcode.ControlCodeSubJourney.PHYSICAL_GOODS_SEARCH_RELATED_TO_SOFTWARE) {
      this.formAction = routes.TechnicalNotesController.handleSearchRelatedToSubmit(GoodsType.SOFTWARE.urlString()).url();
    }
    else if (controlCodeSubJourney == models.controlcode.ControlCodeSubJourney.PHYSICAL_GOODS_SEARCH_RELATED_TO_TECHNOLOGY) {
      this.formAction = routes.TechnicalNotesController.handleSearchRelatedToSubmit(GoodsType.TECHNOLOGY.urlString()).url();
    }
    else if (controlCodeSubJourney == models.controlcode.ControlCodeSubJourney.SOFTWARE_CONTROLS) {
      this.formAction = routes.TechnicalNotesController.handleControlsSubmit(GoodsType.SOFTWARE.urlString()).url();
    }
    else if (controlCodeSubJourney == models.controlcode.ControlCodeSubJourney.TECHNOLOGY_CONTROLS) {
      this.formAction = routes.TechnicalNotesController.handleControlsSubmit(GoodsType.TECHNOLOGY.urlString()).url();
    }
    else if (controlCodeSubJourney == models.controlcode.ControlCodeSubJourney.SOFTWARE_CONTROLS_RELATED_TO_A_PHYSICAL_GOOD) {
      this.formAction = routes.TechnicalNotesController.handleRelatedControlsSubmit(GoodsType.SOFTWARE.urlString()).url();
    }
    else if (controlCodeSubJourney == models.controlcode.ControlCodeSubJourney.TECHNOLOGY_CONTROLS_RELATED_TO_A_PHYSICAL_GOOD) {
      this.formAction = routes.TechnicalNotesController.handleRelatedControlsSubmit(GoodsType.TECHNOLOGY.urlString()).url();
    }
    else if (controlCodeSubJourney == models.controlcode.ControlCodeSubJourney.SOFTWARE_CATCHALL_CONTROLS) {
      this.formAction = routes.TechnicalNotesController.handleCatchallControlsSubmit(GoodsType.SOFTWARE.urlString()).url();
    }
    else if (controlCodeSubJourney == models.controlcode.ControlCodeSubJourney.TECHNOLOGY_CATCHALL_CONTROLS) {
      this.formAction = routes.TechnicalNotesController.handleCatchallControlsSubmit(GoodsType.TECHNOLOGY.urlString()).url();
    }
    else {
      throw new RuntimeException(String.format("Unexpected member of ControlCodeSubJourney enum: \"%s\""
          , controlCodeSubJourney.toString()));
    }
  }

}
