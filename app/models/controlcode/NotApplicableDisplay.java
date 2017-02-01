package models.controlcode;

import components.services.controlcode.FrontendControlCode;
import models.controlcode.NotApplicableDisplayCommon.ActionButton;
import models.softtech.ApplicableSoftTechControls;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public class NotApplicableDisplay{

  public final ControlCodeSubJourney controlCodeSubJourney;
  public final FrontendControlCode frontendControlCode;
  public final ApplicableSoftTechControls applicableSoftTechControls;
  public final String controlCodeAlias;
  public final List<ActionButton> buttons;

  public NotApplicableDisplay(NotApplicableDisplayCommon displayCommon) {
    this.controlCodeSubJourney = displayCommon.controlCodeSubJourney;
    this.frontendControlCode = displayCommon.frontendControlCode;
    this.applicableSoftTechControls = displayCommon.applicableSoftTechControls;
    this.controlCodeAlias = frontendControlCode.controlCodeData.alias;
    if (controlCodeSubJourney.isPhysicalGoodsSearchVariant()) {
      this.buttons = Arrays.asList(
          new ActionButton(BackType.RESULTS.toString(), "return to the list of possible matches and choose again"),
          new ActionButton(BackType.SEARCH.toString(), "edit your item description to get a different set of results")
      );
    }
    else if (controlCodeSubJourney.isSoftTechControlsVariant() ||
        controlCodeSubJourney.isSoftTechControlsRelatedToPhysicalGoodVariant() ||
        controlCodeSubJourney.isSoftTechCatchallControlsVariant() ||
        controlCodeSubJourney.isNonExemptControlsVariant()) {
      if (NotApplicableDisplayCommon.canPickAgain(applicableSoftTechControls)) {
        this.buttons = Collections.singletonList(new ActionButton(BackType.MATCHES.toString(), "return to the list of possible matches and choose again"));
      }
      else {
        this.buttons = Collections.singletonList(new ActionButton("continue", "continue to other options"));
      }
    }
    else {
      throw new RuntimeException(String.format("Unexpected member of ControlCodeSubJourney enum: \"%s\""
          , controlCodeSubJourney.toString()));
    }
  }

}
