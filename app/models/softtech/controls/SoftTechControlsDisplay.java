package models.softtech.controls;

import components.services.controlcode.controls.ControlCode;
import controllers.softtech.controls.SoftTechControlsController;
import controllers.softtech.controls.routes;
import models.GoodsType;
import play.data.Form;

import java.util.List;

public class SoftTechControlsDisplay {

  public final Form<SoftTechControlsController.SoftTechControlsForm> form;
  public final String formAction;
  public final String pageTitle;
  public List<ControlCode> controlCodes;

  public SoftTechControlsDisplay(Form<SoftTechControlsController.SoftTechControlsForm> form, SoftTechControlsJourney softTechControlsJourney, List<ControlCode> controlCodes) {
    this.form = form;
    if (softTechControlsJourney == SoftTechControlsJourney.SOFTWARE_CATEGORY) {
      this.formAction = routes.SoftTechControlsController.handleCategorySubmit(GoodsType.SOFTWARE.toUrlString()).url();
      this.pageTitle = "Showing controls related to software category";
    }
    else if (softTechControlsJourney == SoftTechControlsJourney.TECHNOLOGY_CATEGORY) {
      this.formAction = routes.SoftTechControlsController.handleCategorySubmit(GoodsType.TECHNOLOGY.toUrlString()).url();
      this.pageTitle = "Showing controls related to technology category";
    }
    else if (softTechControlsJourney == SoftTechControlsJourney.SOFTWARE_RELATED_TO_A_PHYSICAL_GOOD) {
      this.formAction = routes.SoftTechControlsController.handleRelatedToPhysicalGoodSubmit(GoodsType.SOFTWARE.toUrlString()).url();
      this.pageTitle = "Showing controls related to your selected physical good";
    }
    else if (softTechControlsJourney == SoftTechControlsJourney.TECHNOLOGY_RELATED_TO_A_PHYSICAL_GOOD) {
      this.formAction = routes.SoftTechControlsController.handleRelatedToPhysicalGoodSubmit(GoodsType.TECHNOLOGY.toUrlString()).url();
      this.pageTitle = "Showing controls related to your selected physical good";
    }
    else if (softTechControlsJourney == SoftTechControlsJourney.SOFTWARE_CATCHALL) {
      this.formAction = routes.SoftTechControlsController.handleCatchallControlsSubmit(GoodsType.SOFTWARE.toUrlString()).url();
      this.pageTitle = "Showing catchall controls related to your items category";
    }
    else if (softTechControlsJourney == SoftTechControlsJourney.TECHNOLOGY_CATCHALL) {
      this.formAction = routes.SoftTechControlsController.handleCatchallControlsSubmit(GoodsType.TECHNOLOGY.toUrlString()).url();
      this.pageTitle = "Showing catchall controls related to your items category";
    }
    else {
      throw new RuntimeException(String.format("Unexpected member of SoftTechControlsJourney enum: \"%s\""
          , softTechControlsJourney.toString()));
    }
    this.controlCodes = controlCodes;
  }
}
