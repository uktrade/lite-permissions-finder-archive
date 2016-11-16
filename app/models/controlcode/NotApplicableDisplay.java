package models.controlcode;

import controllers.controlcode.routes;
import play.data.Form;

public class NotApplicableDisplay {
  public final Form<?> form;
  public final String formAction;
  public final String controlCodeAlias;
  public final boolean showExtendedContent;

  public NotApplicableDisplay(ControlCodeJourney controlCodeJourney, Form<?> form, String controlCodeAlias, boolean showExtendedContent) {
    this.form = form;
    this.controlCodeAlias = controlCodeAlias;
    this.showExtendedContent = showExtendedContent;
    if (controlCodeJourney== ControlCodeJourney.PHYSICAL_GOODS_SEARCH) {
      this.formAction = routes.NotApplicableController.handleSubmit().url();
    }
    else if (controlCodeJourney== ControlCodeJourney.PHYSICAL_GOODS_SEARCH_RELATED_TO_SOFTWARE) {
      this.formAction = routes.NotApplicableController.handleRelatedToSoftwareSubmit().url();
    }
    else if (controlCodeJourney== ControlCodeJourney.SOFTWARE_CONTROLS) {
      this.formAction = routes.NotApplicableController.handleSoftwareControlsSubmit().url();
    }
    else {
      throw new RuntimeException(String.format("Unexpected member of ControlCodeJourney enum: \"%s\""
          , controlCodeJourney.toString()));
    }
  }
}
