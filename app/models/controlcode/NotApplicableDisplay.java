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
  public final ControlCodeJourney controlCodeJourney;

  /**
   * Display object for the {@code notApplicable} view
   * @param controlCodeJourney The control code journey
   * @param form The form
   * @param controlCodeAlias The control code alias
   * @param showExtendedContent Show the extended content
   * @param applicableSoftTechControls Should be {@code null} unless {@code controlCodeJourney == }{@link ControlCodeJourney#SOFTWARE_CONTROLS}.
   */
  public NotApplicableDisplay(ControlCodeJourney controlCodeJourney, Form<?> form, String controlCodeAlias, boolean showExtendedContent, ApplicableSoftTechControls applicableSoftTechControls) {
    this.controlCodeJourney = controlCodeJourney;
    this.form = form;
    this.controlCodeAlias = controlCodeAlias;
    this.showExtendedContent = showExtendedContent;
    if (controlCodeJourney == ControlCodeJourney.PHYSICAL_GOODS_SEARCH) {
      this.formAction = routes.NotApplicableController.handleSearchSubmit().url();
      this.canPickAgain = true;
    }
    else if (controlCodeJourney == ControlCodeJourney.PHYSICAL_GOODS_SEARCH_RELATED_TO_SOFTWARE) {
      this.formAction = routes.NotApplicableController.handleSearchRelatedToSubmit(GoodsType.SOFTWARE.toUrlString()).url();
      this.canPickAgain = true;
    }
    else if (controlCodeJourney == ControlCodeJourney.PHYSICAL_GOODS_SEARCH_RELATED_TO_TECHNOLOGY) {
      this.formAction = routes.NotApplicableController.handleSearchRelatedToSubmit(GoodsType.TECHNOLOGY.toUrlString()).url();
      this.canPickAgain = true;
    }
    else if (controlCodeJourney == ControlCodeJourney.SOFTWARE_CONTROLS ||
        controlCodeJourney == ControlCodeJourney.TECHNOLOGY_CONTROLS ||
        controlCodeJourney == ControlCodeJourney.SOFTWARE_CONTROLS_RELATED_TO_A_PHYSICAL_GOOD ||
        controlCodeJourney == ControlCodeJourney.TECHNOLOGY_CONTROLS_RELATED_TO_A_PHYSICAL_GOOD ||
        controlCodeJourney == ControlCodeJourney.SOFTWARE_CATCHALL_CONTROLS ||
        controlCodeJourney == ControlCodeJourney.TECHNOLOGY_CATCHALL_CONTROLS) {
      // Software
      if (applicableSoftTechControls != null) {
        if (controlCodeJourney == ControlCodeJourney.SOFTWARE_CONTROLS) {
          this.formAction = routes.NotApplicableController.handleControlsSubmit(GoodsType.SOFTWARE.toUrlString()).url();
        }
        else if (controlCodeJourney == ControlCodeJourney.TECHNOLOGY_CONTROLS) {
          this.formAction = routes.NotApplicableController.handleControlsSubmit(GoodsType.TECHNOLOGY.toUrlString()).url();
        }
        else if (controlCodeJourney == ControlCodeJourney.SOFTWARE_CONTROLS_RELATED_TO_A_PHYSICAL_GOOD) {
          this.formAction = routes.NotApplicableController.handleRelatedControlsSubmit(GoodsType.SOFTWARE.toUrlString()).url();
        }
        else if (controlCodeJourney == ControlCodeJourney.TECHNOLOGY_CONTROLS_RELATED_TO_A_PHYSICAL_GOOD) {
          this.formAction = routes.NotApplicableController.handleRelatedControlsSubmit(GoodsType.TECHNOLOGY.toUrlString()).url();
        }
        else if (controlCodeJourney == ControlCodeJourney.SOFTWARE_CATCHALL_CONTROLS) {
          this.formAction = routes.NotApplicableController.handleCatchallControlsSubmit(GoodsType.SOFTWARE.toUrlString()).url();
        }
        else {
          // ControlCodeJourney.TECHNOLOGY_CATCHALL_CONTROLS
          this.formAction = routes.NotApplicableController.handleCatchallControlsSubmit(GoodsType.TECHNOLOGY.toUrlString()).url();
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
      throw new RuntimeException(String.format("Unexpected member of ControlCodeJourney enum: \"%s\""
          , controlCodeJourney.toString()));
    }
  }

  /**
   * Display object for the {@code notApplicable} view
   * @param controlCodeJourney The control code journey
   * @param form The form
   * @param controlCodeAlias The control code alias
   * @param showExtendedContent Show the extended content
   */
  public NotApplicableDisplay(ControlCodeJourney controlCodeJourney, Form<?> form, String controlCodeAlias, boolean showExtendedContent) {
    this(controlCodeJourney, form, controlCodeAlias, showExtendedContent, null);
  }
}
