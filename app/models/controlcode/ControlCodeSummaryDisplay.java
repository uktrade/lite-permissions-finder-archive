package models.controlcode;

import components.services.controlcode.Ancestor;
import components.services.controlcode.ControlCodeData;
import components.services.controlcode.FrontendServiceResult;
import models.GoodsType;

import java.util.List;

public class ControlCodeSummaryDisplay {
  public final String title;
  public final String friendlyDescription;
  public final String controlCodeAlias;
  public final Ancestor greatestAncestor;
  public final List<Ancestor> otherAncestors;
  public final boolean showGreatestAncestor;
  public final String couldDescribeItemsLabel;
  public final ControlCodeSubJourney controlCodeSubJourney;
  public final boolean decontrolsExist;
  public final boolean additionalSpecificationsExist;
  public final boolean technicalNotesExist;

  public ControlCodeSummaryDisplay(ControlCodeSubJourney controlCodeSubJourney, FrontendServiceResult frontendServiceResult) {
    ControlCodeData controlCodeData = frontendServiceResult.controlCodeData;
    this.title = controlCodeData.title;
    this.friendlyDescription = controlCodeData.friendlyDescription;
    this.controlCodeAlias = controlCodeData.alias;
    this.controlCodeSubJourney = controlCodeSubJourney;
    this.decontrolsExist = controlCodeData.canShowDecontrols();
    this.additionalSpecificationsExist = controlCodeData.canShowAdditionalSpecifications();
    this.technicalNotesExist = controlCodeData.canShowTechnicalNotes();
    if (frontendServiceResult.greatestAncestor.isPresent()) {
      this.greatestAncestor = frontendServiceResult.greatestAncestor.get();
      this.showGreatestAncestor = true;
    }
    else {
      this.greatestAncestor = null;
      this.showGreatestAncestor = false;
    }
    this.otherAncestors = frontendServiceResult.otherAncestors;
    if (controlCodeSubJourney.isPhysicalGoodsSearchVariant()) {
      GoodsType goodsType = controlCodeSubJourney.getGoodsType();
      if (goodsType == GoodsType.PHYSICAL) {
        this.couldDescribeItemsLabel = "Could this describe your items?";
      }
      else if (goodsType == GoodsType.SOFTWARE){
        this.couldDescribeItemsLabel = "Could this describe the item your software is used with?";
      }
      else {
        // GoodsType.TECHNOLOGY
        this.couldDescribeItemsLabel = "Could this describe the item your technology is used with?";
      }
    }
    else if (controlCodeSubJourney.isSoftTechControlsVariant() ||
        controlCodeSubJourney.isSoftTechControlsRelatedToPhysicalGoodVariant() ||
        controlCodeSubJourney.isSoftTechCatchallControlsVariant()) {
      this.couldDescribeItemsLabel = "Could this describe your items?";
    }
    else {
      throw new RuntimeException(String.format("Unexpected member of ControlCodeSubJourney enum: \"%s\""
          , controlCodeSubJourney.toString()));
    }
  }

}
