package models.controlcode;

import components.services.controlcode.frontend.FrontendServiceResult;
import models.GoodsType;
import uk.gov.bis.lite.controlcode.api.view.ControlCodeSummary;
import uk.gov.bis.lite.controlcode.api.view.FrontEndControlCodeView.FrontEndControlCodeData;

import java.util.List;

public class ControlCodeSummaryDisplay {
  public final String title;
  public final String friendlyDescription;
  public final String controlCodeAlias;
  public final ControlCodeSummary greatestAncestor;
  public final List<ControlCodeSummary> otherAncestors;
  public final boolean showGreatestAncestor;
  public final String couldDescribeItemsLabel;
  public final ControlCodeSubJourney controlCodeSubJourney;
  public final boolean showTechNotesQuestion;
  public final boolean showAdditionalSpecsPanel;

  public ControlCodeSummaryDisplay(ControlCodeSubJourney controlCodeSubJourney, FrontendServiceResult frontendServiceResult) {
    FrontEndControlCodeData controlCodeData = frontendServiceResult.getControlCodeData();
    this.title = controlCodeData.getTitle();
    this.friendlyDescription = controlCodeData.getFriendlyDescription();
    this.controlCodeAlias = controlCodeData.getAlias();
    this.controlCodeSubJourney = controlCodeSubJourney;
    this.showTechNotesQuestion = frontendServiceResult.canShowTechnicalNotes() && !frontendServiceResult.canShowAdditionalSpecifications();
    this.showAdditionalSpecsPanel = frontendServiceResult.canShowAdditionalSpecifications();
    if (frontendServiceResult.getGreatestAncestor().isPresent()) {
      this.greatestAncestor = frontendServiceResult.getGreatestAncestor().get();
      this.showGreatestAncestor = true;
    }
    else {
      this.greatestAncestor = null;
      this.showGreatestAncestor = false;
    }
    this.otherAncestors = frontendServiceResult.getOtherAncestors();
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
        controlCodeSubJourney.isSoftTechCatchallControlsVariant() ||
        controlCodeSubJourney.isNonExemptControlsVariant()) {
      this.couldDescribeItemsLabel = "Could this describe your items?";
    }
    else {
      throw new RuntimeException(String.format("Unexpected member of ControlCodeSubJourney enum: \"%s\""
          , controlCodeSubJourney.toString()));
    }
  }

}
