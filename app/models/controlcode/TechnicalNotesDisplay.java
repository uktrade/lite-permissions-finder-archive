package models.controlcode;

import components.services.controlcode.ControlCodeData;
import components.services.controlcode.FrontendServiceResult;
import controllers.controlcode.routes;
import models.GoodsType;

public class TechnicalNotesDisplay {
  public final String formAction;
  public final String title;
  public final String friendlyDescription;
  public final String controlCodeAlias;
  public final String technicalNotes;

  public TechnicalNotesDisplay(ControlCodeJourney controlCodeJourney, FrontendServiceResult frontendServiceResult) {
    ControlCodeData controlCodeData = frontendServiceResult.controlCodeData;
    this.title = controlCodeData.title;
    this.friendlyDescription = controlCodeData.friendlyDescription;
    this.controlCodeAlias = controlCodeData.alias;
    this.technicalNotes = controlCodeData.technicalNotes;
    if (controlCodeJourney == ControlCodeJourney.PHYSICAL_GOODS_SEARCH) {
      this.formAction = routes.TechnicalNotesController.handleSearchSubmit().url();
    }
    else if (controlCodeJourney == ControlCodeJourney.PHYSICAL_GOODS_SEARCH_RELATED_TO_SOFTWARE) {
      this.formAction = routes.TechnicalNotesController.handleSearchRelatedToSubmit(GoodsType.SOFTWARE.toUrlString()).url();
    }
    else if (controlCodeJourney == ControlCodeJourney.PHYSICAL_GOODS_SEARCH_RELATED_TO_TECHNOLOGY) {
      this.formAction = routes.TechnicalNotesController.handleSearchRelatedToSubmit(GoodsType.TECHNOLOGY.toUrlString()).url();
    }
    else if (controlCodeJourney == ControlCodeJourney.SOFTWARE_CONTROLS) {
      this.formAction = routes.TechnicalNotesController.handleControlsSubmit(GoodsType.SOFTWARE.toUrlString()).url();
    }
    else if (controlCodeJourney == ControlCodeJourney.TECHNOLOGY_CONTROLS) {
      this.formAction = routes.TechnicalNotesController.handleControlsSubmit(GoodsType.TECHNOLOGY.toUrlString()).url();
    }
    else if (controlCodeJourney == ControlCodeJourney.SOFTWARE_CONTROLS_RELATED_TO_A_PHYSICAL_GOOD) {
      this.formAction = routes.TechnicalNotesController.handleRelatedControlsSubmit(GoodsType.SOFTWARE.toUrlString()).url();
    }
    else if (controlCodeJourney == ControlCodeJourney.TECHNOLOGY_CONTROLS_RELATED_TO_A_PHYSICAL_GOOD) {
      this.formAction = routes.TechnicalNotesController.handleRelatedControlsSubmit(GoodsType.TECHNOLOGY.toUrlString()).url();
    }
    else if (controlCodeJourney == ControlCodeJourney.SOFTWARE_CATCHALL_CONTROLS) {
      this.formAction = routes.TechnicalNotesController.handleCatchallControlsSubmit(GoodsType.SOFTWARE.toUrlString()).url();
    }
    else if (controlCodeJourney == ControlCodeJourney.TECHNOLOGY_CATCHALL_CONTROLS) {
      this.formAction = routes.TechnicalNotesController.handleCatchallControlsSubmit(GoodsType.TECHNOLOGY.toUrlString()).url();
    }
    else {
      throw new RuntimeException(String.format("Unexpected member of ControlCodeJourney enum: \"%s\""
          , controlCodeJourney.toString()));
    }
  }

}
