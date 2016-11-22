package models.controlcode;

import controllers.controlcode.routes;
import models.software.ApplicableSoftwareControls;
import play.data.Form;

public class NotApplicableDisplay {
  public final Form<?> form;
  public final String formAction;
  public final String controlCodeAlias;
  public final boolean showExtendedContent;
  public final ButtonConfiguration buttonConfiguration;

  /**
   * Display object for the {@code notApplicable} view
   * @param controlCodeJourney The control code journey
   * @param form The form
   * @param controlCodeAlias The control code alias
   * @param showExtendedContent Show the extended content
   * @param applicableSoftwareControls Should be {@code null} unless {@code controlCodeJourney == }{@link ControlCodeJourney#SOFTWARE_CONTROLS}.
   */
  public NotApplicableDisplay(ControlCodeJourney controlCodeJourney, Form<?> form, String controlCodeAlias, boolean showExtendedContent, ApplicableSoftwareControls applicableSoftwareControls) {
    this.form = form;
    this.controlCodeAlias = controlCodeAlias;
    this.showExtendedContent = showExtendedContent;
    if (controlCodeJourney == ControlCodeJourney.PHYSICAL_GOODS_SEARCH) {
      this.formAction = routes.NotApplicableController.handleSearchSubmit().url();
      this.buttonConfiguration = ButtonConfiguration.RETURN_TO_SEARCH;
    }
    else if (controlCodeJourney == ControlCodeJourney.PHYSICAL_GOODS_SEARCH_RELATED_TO_SOFTWARE) {
      this.formAction = routes.NotApplicableController.handleSearchRelatedToSoftwareSubmit().url();
      this.buttonConfiguration = ButtonConfiguration.RETURN_TO_SEARCH;
    }
    else if (controlCodeJourney == ControlCodeJourney.SOFTWARE_CONTROLS ||
        controlCodeJourney == ControlCodeJourney.SOFTWARE_CONTROLS_RELATED_TO_A_PHYSICAL_GOOD) {
      if (applicableSoftwareControls != null) {
        if (controlCodeJourney == ControlCodeJourney.SOFTWARE_CONTROLS) {
          this.formAction = routes.NotApplicableController.handleSoftwareControlsSubmit().url();
        }
        else {
          // TODO check that this button configuration is correct for software controls related to a physical good
          this.formAction = routes.NotApplicableController.handleRelatedSoftwareControlsSubmit().url();
        }
        if (applicableSoftwareControls == ApplicableSoftwareControls.ONE) {
          buttonConfiguration = ButtonConfiguration.CONTINUE_NO_SOFTWARE_CONTROLS;
        }
        else if (applicableSoftwareControls == ApplicableSoftwareControls.GREATER_THAN_ONE) {
          buttonConfiguration = ButtonConfiguration.RETURN_TO_SOFTWARE_CONTROLS;
        }
        else {
          throw new RuntimeException(String.format("Unexpected member of ApplicableSoftwareControls enum: \"%s\""
              , applicableSoftwareControls.toString()));
        }
      }
      else {
        throw new RuntimeException(String.format("Expected applicableSoftwareControls to not be null"));
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

  public enum ButtonConfiguration {
    RETURN_TO_SEARCH,
    RETURN_TO_SOFTWARE_CONTROLS,
    CONTINUE_NO_SOFTWARE_CONTROLS
  }
}
