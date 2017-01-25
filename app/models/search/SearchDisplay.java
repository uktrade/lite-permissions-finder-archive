package models.search;

import models.controlcode.ControlCodeSubJourney;

public class SearchDisplay {
  public final ControlCodeSubJourney controlCodeSubJourney;
  public final String pageTitle;
  public final String descriptionLabel;
  public final String componentLabel;
  public final String isComponenetLabel;

  public SearchDisplay(ControlCodeSubJourney controlCodeSubJourney) {
    this.controlCodeSubJourney = controlCodeSubJourney;
    if (controlCodeSubJourney == models.controlcode.ControlCodeSubJourney.PHYSICAL_GOODS_SEARCH) {
      this.pageTitle = "Describe your item";
      this.descriptionLabel = "Provide as much information as you can";
      this.componentLabel = "Describe the equipment or system the item is designed to be part of";
      this.isComponenetLabel = "Is your item a component, attachment or part for another item?";
    }
    else if (controlCodeSubJourney == models.controlcode.ControlCodeSubJourney.PHYSICAL_GOODS_SEARCH_RELATED_TO_SOFTWARE) {
      this.pageTitle = "Describe the equipment or materials your software is related to";
      this.descriptionLabel = "Provide as much information as you can about the physical item your software is used with";
      this.componentLabel = "Describe the equipment or system the item is designed to be part of";
      this.isComponenetLabel = "Is the item your software is used with a component or part of a larger system?";
    }
    else if (controlCodeSubJourney == models.controlcode.ControlCodeSubJourney.PHYSICAL_GOODS_SEARCH_RELATED_TO_TECHNOLOGY) {
      this.pageTitle = "Describe the equipment or materials your technology is related to";
      this.descriptionLabel = "Provide as much information as you can about the physical item your technology is used with";
      this.componentLabel = "Describe the equipment or system the item is designed to be part of";
      this.isComponenetLabel = "Is the item your technology is used with a component or part of a larger system?";
    }
    else {
      throw new RuntimeException(String.format("Unexpected member of ControlCodeSubJourney enum: \"%s\""
          , controlCodeSubJourney.toString()));
    }
  }
}
