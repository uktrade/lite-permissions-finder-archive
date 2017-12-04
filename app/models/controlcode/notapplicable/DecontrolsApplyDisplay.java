package models.controlcode.notapplicable;

import models.ExportCategory;
import models.GoodsType;
import models.controlcode.BackType;
import models.controlcode.ControlCodeSubJourney;
import models.controlcode.NotApplicableDisplayCommon;
import models.controlcode.NotApplicableDisplayCommon.ActionButton;
import models.softtech.ApplicableSoftTechControls;
import models.softtech.SoftTechCategory;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class DecontrolsApplyDisplay {

  public final ControlCodeSubJourney controlCodeSubJourney;
  public final ApplicableSoftTechControls applicableSoftTechControls;
  public final List<ActionButton> buttons;
  public final String controlCodeTitle ;

  public DecontrolsApplyDisplay(NotApplicableDisplayCommon displayCommon, ExportCategory exportCategory, Optional<SoftTechCategory> softTechCategory) {
    this.controlCodeSubJourney = displayCommon.controlCodeSubJourney;
    this.controlCodeTitle = displayCommon.frontendControlCode.getControlCodeData().getTitle();
    this.applicableSoftTechControls = displayCommon.applicableSoftTechControls;
    this.buttons = new ArrayList<>();
    if (controlCodeSubJourney.isPhysicalGoodsSearchVariant()) {
      this.buttons.add(new ActionButton(BackType.RESULTS.toString(), "return to the list of possible matches and choose again"));
      this.buttons.add(new ActionButton(BackType.SEARCH.toString(), "edit your item description to get a different set of results"));
    }
    else if (controlCodeSubJourney.isSoftTechControlsVariant() ||
        controlCodeSubJourney.isSoftTechControlsRelatedToPhysicalGoodVariant() ||
        controlCodeSubJourney.isSoftTechCatchallControlsVariant() ||
        controlCodeSubJourney.isNonExemptControlsVariant()) {

      if (NotApplicableDisplayCommon.canPickAgain(applicableSoftTechControls)) {
        this.buttons.add(new ActionButton(BackType.MATCHES.toString(), "return to the list of possible matches and choose again"));
      }

      if (controlCodeSubJourney.isSoftTechControlsVariant() && softTechCategory.isPresent() && softTechCategory.get().isDualUseSoftTechCategory()) {
        GoodsType goodsType = controlCodeSubJourney.getSoftTechGoodsType();
        this.buttons.add(new ActionButton(BackType.SOFT_TECH_CATEGORY.toString(), "change your dual use " + goodsType.value().toLowerCase() + " category (currently " + softTechCategory.get().getHeading() + ")"));
      }
    }
    else {
      throw new RuntimeException(String.format("Unexpected member of ControlCodeSubJourney enum: \"%s\""
          , controlCodeSubJourney.toString()));
    }
    //todo: clean up ExportCategory enum
//    this.buttons.add(new ActionButton(BackType.EXPORT_CATEGORY.toString(), "change your item category (currently " + exportCategory.getHeading() + ")"));
  }
}
