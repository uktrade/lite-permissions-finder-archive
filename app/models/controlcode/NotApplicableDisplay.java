package models.controlcode;

import controllers.controlcode.routes;
import models.GoodsType;
import models.softtech.ApplicableSoftTechControls;
import play.data.Form;

public class NotApplicableDisplay {
  public final Form<?> form;
  public final String formAction;
  public final String controlCodeAlias;
  public final boolean showExtendedContent;
  public final boolean canPickAgain;
  public final ControlCodeSubJourney controlCodeSubJourney;

  /**
   * Display object for the {@code notApplicable} view
   * @param controlCodeSubJourney The control code journey
   * @param form The form
   * @param controlCodeAlias The control code alias
   * @param showExtendedContent Show the extended content
   * @param applicableSoftTechControls Should be {@code null} unless {@code controlCodeSubJourney == }{@link ControlCodeSubJourney#SOFTWARE_CONTROLS}.
   */
  public NotApplicableDisplay(ControlCodeSubJourney controlCodeSubJourney, Form<?> form, String controlCodeAlias, boolean showExtendedContent, ApplicableSoftTechControls applicableSoftTechControls) {
    this.controlCodeSubJourney = controlCodeSubJourney;
    this.form = form;
    this.controlCodeAlias = controlCodeAlias;
    this.showExtendedContent = showExtendedContent;
    if (controlCodeSubJourney == models.controlcode.ControlCodeSubJourney.PHYSICAL_GOODS_SEARCH) {
      this.formAction = routes.NotApplicableController.handleSubmit().url();
      this.canPickAgain = true;
    }
    else if (controlCodeSubJourney == models.controlcode.ControlCodeSubJourney.PHYSICAL_GOODS_SEARCH_RELATED_TO_SOFTWARE) {
      this.formAction = routes.NotApplicableController.handleSearchRelatedToSubmit(GoodsType.SOFTWARE.urlString()).url();
      this.canPickAgain = true;
    }
    else if (controlCodeSubJourney == models.controlcode.ControlCodeSubJourney.PHYSICAL_GOODS_SEARCH_RELATED_TO_TECHNOLOGY) {
      this.formAction = routes.NotApplicableController.handleSearchRelatedToSubmit(GoodsType.TECHNOLOGY.urlString()).url();
      this.canPickAgain = true;
    }
    else if (controlCodeSubJourney == models.controlcode.ControlCodeSubJourney.SOFTWARE_CONTROLS ||
        controlCodeSubJourney == models.controlcode.ControlCodeSubJourney.TECHNOLOGY_CONTROLS ||
        controlCodeSubJourney == models.controlcode.ControlCodeSubJourney.SOFTWARE_CONTROLS_RELATED_TO_A_PHYSICAL_GOOD ||
        controlCodeSubJourney == models.controlcode.ControlCodeSubJourney.TECHNOLOGY_CONTROLS_RELATED_TO_A_PHYSICAL_GOOD ||
        controlCodeSubJourney == models.controlcode.ControlCodeSubJourney.SOFTWARE_CATCHALL_CONTROLS ||
        controlCodeSubJourney == models.controlcode.ControlCodeSubJourney.TECHNOLOGY_CATCHALL_CONTROLS) {
      // Software
      if (applicableSoftTechControls != null) {
        if (controlCodeSubJourney == models.controlcode.ControlCodeSubJourney.SOFTWARE_CONTROLS) {
          this.formAction = routes.NotApplicableController.handleControlsSubmit(GoodsType.SOFTWARE.urlString()).url();
        }
        else if (controlCodeSubJourney == models.controlcode.ControlCodeSubJourney.TECHNOLOGY_CONTROLS) {
          this.formAction = routes.NotApplicableController.handleControlsSubmit(GoodsType.TECHNOLOGY.urlString()).url();
        }
        else if (controlCodeSubJourney == models.controlcode.ControlCodeSubJourney.SOFTWARE_CONTROLS_RELATED_TO_A_PHYSICAL_GOOD) {
          this.formAction = routes.NotApplicableController.handleRelatedControlsSubmit(GoodsType.SOFTWARE.urlString()).url();
        }
        else if (controlCodeSubJourney == models.controlcode.ControlCodeSubJourney.TECHNOLOGY_CONTROLS_RELATED_TO_A_PHYSICAL_GOOD) {
          this.formAction = routes.NotApplicableController.handleRelatedControlsSubmit(GoodsType.TECHNOLOGY.urlString()).url();
        }
        else if (controlCodeSubJourney == models.controlcode.ControlCodeSubJourney.SOFTWARE_CATCHALL_CONTROLS) {
          this.formAction = routes.NotApplicableController.handleCatchallControlsSubmit(GoodsType.SOFTWARE.urlString()).url();
        }
        else {
          // ControlCodeSubJourney.TECHNOLOGY_CATCHALL_CONTROLS
          this.formAction = routes.NotApplicableController.handleCatchallControlsSubmit(GoodsType.TECHNOLOGY.urlString()).url();
        }

        if (applicableSoftTechControls == ApplicableSoftTechControls.ONE) {
          this.canPickAgain = false;
        }
        else if (applicableSoftTechControls == ApplicableSoftTechControls.GREATER_THAN_ONE) {
          this.canPickAgain = true;
        }
        else {
          throw new RuntimeException(String.format("Unexpected member of ApplicableSoftTechControls enum: \"%s\""
              , applicableSoftTechControls.toString()));
        }
      }
      else {
        throw new RuntimeException(String.format("Expected applicableSoftTechControls to not be null"));
      }
    }
    else {
      throw new RuntimeException(String.format("Unexpected member of ControlCodeSubJourney enum: \"%s\""
          , controlCodeSubJourney.toString()));
    }
  }

  /**
   * Display object for the {@code notApplicable} view
   * @param controlCodeSubJourney The control code journey
   * @param form The form
   * @param controlCodeAlias The control code alias
   * @param showExtendedContent Show the extended content
   */
  public NotApplicableDisplay(ControlCodeSubJourney controlCodeSubJourney, Form<?> form, String controlCodeAlias, boolean showExtendedContent) {
    this(controlCodeSubJourney, form, controlCodeAlias, showExtendedContent, null);
  }
}
