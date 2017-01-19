package models.softtech.controls;

import components.services.controlcode.controls.ControlCode;
import models.GoodsType;
import models.controlcode.ControlCodeSubJourney;

import java.util.List;

public class SoftTechControlsDisplay {

  public final String pageTitle;
  public final String preResultsLabel;
  public List<ControlCode> controlCodes;

  public SoftTechControlsDisplay(ControlCodeSubJourney controlCodeSubJourney, List<ControlCode> controlCodes) {
    if (controlCodeSubJourney.isSoftTechControlsVariant()) {
      this.pageTitle = "Possible matches";
      this.preResultsLabel = "";
    }
    else if (controlCodeSubJourney.isSoftTechControlsRelatedToPhysicalGoodVariant()) {
      GoodsType goodsType = controlCodeSubJourney.getSoftTechGoodsType();
      if (goodsType == GoodsType.SOFTWARE) {
        this.pageTitle = "Software matches";
        this.preResultsLabel = "Select the closest description of your software";
      }
      else if (goodsType == GoodsType.TECHNOLOGY) {
        this.pageTitle = "Technology matches";
        this.preResultsLabel = "Select the closest description of your technology";
      }
      else {
        throw new RuntimeException(String.format("Unexpected member of GoodsType enum: \"%s\""
            , goodsType.toString()));
      }
    }
    else if (controlCodeSubJourney.isSoftTechCatchallControlsVariant()) {
      this.pageTitle = "Showing catchall controls related to your items category";
      this.preResultsLabel = "";
    }
    else {
      throw new RuntimeException(String.format("Unexpected member of ControlCodeSubJourney enum: \"%s\""
          , controlCodeSubJourney.toString()));
    }
    this.controlCodes = controlCodes;
  }
}
