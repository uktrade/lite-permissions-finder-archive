package models.search;

import models.controlcode.ControlCodeSubJourney;

public class SearchDisplay {
  public final ControlCodeSubJourney controlCodeSubJourney;
  public final String pageTitle;
  public final String descriptionLabel;
  public final String componentLabel;
  public final String isComponentLabel;
  public String itemLabel;
  public String partNumberLabel;
  public String isCompleteLabel;

  public SearchDisplay(ControlCodeSubJourney controlCodeSubJourney) {
    this.controlCodeSubJourney = controlCodeSubJourney;
    if (controlCodeSubJourney == models.controlcode.ControlCodeSubJourney.PHYSICAL_GOODS_SEARCH) {
      this.pageTitle = "Describe the item you're exporting";
      this.descriptionLabel = "Describe item. Use keywords such as armour, radar, turbine etc.";
      this.isComponentLabel = "It is a component, module or accessory for something else";
      this.componentLabel = "What equipment, platform or system was the item originally designed to be used with?";
      this.isCompleteLabel = "It is a complete item";
      this.itemLabel = "Item name";
      this.partNumberLabel = "Part number (Optional)";
    }
    else if (controlCodeSubJourney == models.controlcode.ControlCodeSubJourney.PHYSICAL_GOODS_SEARCH_RELATED_TO_SOFTWARE) {
      this.pageTitle = "Describe the equipment or materials your software is related to";
      this.descriptionLabel = "Describe the physical item your software is used with - what it is and does. Try to use key words (eg radar, armour, turbine) rather than full sentences.";
      this.componentLabel = "Describe the equipment or system the item is designed to be part of";
      this.isComponentLabel = "Is the item with which your software is used a component or part of a larger system?";
    }
    else if (controlCodeSubJourney == models.controlcode.ControlCodeSubJourney.PHYSICAL_GOODS_SEARCH_RELATED_TO_TECHNOLOGY) {
      this.pageTitle = "Describe the equipment or materials your technology is related to";
      this.descriptionLabel = "Describe the physical item your technology is used with - what it is and does. Try to use key words (eg radar, armour, turbine) rather than full sentences.";
      this.componentLabel = "Describe the equipment or system the item is designed to be part of";
      this.isComponentLabel = "Is the item with which your technology is used a component or part of a larger system?";
    }
    else {
      throw new RuntimeException(String.format("Unexpected member of ControlCodeSubJourney enum: \"%s\""
          , controlCodeSubJourney.toString()));
    }
  }
}
