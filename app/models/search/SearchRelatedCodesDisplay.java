package models.search;

import models.controlcode.ControlCodeSubJourney;
import uk.gov.bis.lite.searchmanagement.api.view.RelatedCodeView;

import java.util.List;

public class SearchRelatedCodesDisplay {

  public final ControlCodeSubJourney controlCodeSubJourney;
  public final String pageTitle;
  public final List<RelatedCodeView> relatedCodes;
  public final int relatedCodesDisplayCount;
  public final String lastChosenRelatedCode;
  public final String preRelatedCodesLabel;

  public SearchRelatedCodesDisplay(ControlCodeSubJourney controlCodeSubJourney, String pageTitle, List<RelatedCodeView> relatedCodes, int relatedCodesDisplayCount, String lastChosenRelatedCode) {
    this.controlCodeSubJourney = controlCodeSubJourney;
    this.relatedCodes = relatedCodes;
    this.relatedCodesDisplayCount = relatedCodesDisplayCount;
    this.lastChosenRelatedCode = lastChosenRelatedCode;
    this.pageTitle = pageTitle;
    if (controlCodeSubJourney == ControlCodeSubJourney.PHYSICAL_GOODS_SEARCH) {
      this.preRelatedCodesLabel = "";
    }
    else if (controlCodeSubJourney == ControlCodeSubJourney.PHYSICAL_GOODS_SEARCH_RELATED_TO_SOFTWARE) {
      this.preRelatedCodesLabel = "Select the closest match to the item your software is used with";
    }
    else if (controlCodeSubJourney == ControlCodeSubJourney.PHYSICAL_GOODS_SEARCH_RELATED_TO_TECHNOLOGY) {
      this.preRelatedCodesLabel = "Select the closest match to the item your technology is used with";
    }
    else {
      throw new RuntimeException(String.format("Unexpected member of ControlCodeSubJourney enum: \"%s\""
          , controlCodeSubJourney.toString()));
    }
  }

  public SearchRelatedCodesDisplay(ControlCodeSubJourney controlCodeSubJourney, String pageTitle, List<RelatedCodeView> relatedCodes, int relatedCodesDisplayCount) {
    this(controlCodeSubJourney, pageTitle, relatedCodes, relatedCodesDisplayCount, null);
  }
}