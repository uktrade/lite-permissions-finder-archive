package models.softtech.controls;

import models.GoodsType;
import models.controlcode.ControlCodeSubJourney;
import uk.gov.bis.lite.controlcode.api.view.ControlCodeFullView;

import java.util.List;

public class SoftTechControlsDisplay {

  public final String pageTitle;
  public final String preResultsLabel;
  public List<ControlCodeFullView> controlCodes;

  public SoftTechControlsDisplay(ControlCodeSubJourney controlCodeSubJourney, List<ControlCodeFullView> controlCodes) {
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
      this.pageTitle = "Matches related to your item category";
      this.preResultsLabel = "";
    }
    else if (controlCodeSubJourney.isNonExemptControlsVariant()) {
      this.pageTitle = "Possible matches";
      this.preResultsLabel = "";
    }
    else {
      throw new RuntimeException(String.format("Unexpected member of ControlCodeSubJourney enum: \"%s\""
          , controlCodeSubJourney.toString()));
    }
    this.controlCodes = controlCodes;
  }
}
