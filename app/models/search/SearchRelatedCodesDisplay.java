package models.search;

import components.services.search.relatedcodes.RelatedCode;
import models.controlcode.ControlCodeSubJourney;

import java.util.List;

public class SearchRelatedCodesDisplay {

  public final ControlCodeSubJourney controlCodeSubJourney;
  public final String pageTitle;
  public final List<RelatedCode> relatedCodes;
  public final int relatedCodesDisplayCount;
  public final String lastChosenRelatedCode;
  public final String preRelatedCodesLabel;

  public SearchRelatedCodesDisplay(ControlCodeSubJourney controlCodeSubJourney, List<RelatedCode> relatedCodes, int relatedCodesDisplayCount, String lastChosenRelatedCode) {
    this.controlCodeSubJourney = controlCodeSubJourney;
    this.relatedCodes = relatedCodes;
    this.relatedCodesDisplayCount = relatedCodesDisplayCount;
    this.lastChosenRelatedCode = lastChosenRelatedCode;
    this.pageTitle = "PLACEHOLDER";
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

  public SearchRelatedCodesDisplay(ControlCodeSubJourney controlCodeSubJourney, List<RelatedCode> relatedCodes, int relatedCodesDisplayCount) {
    this(controlCodeSubJourney,relatedCodes, relatedCodesDisplayCount, null);
  }
}