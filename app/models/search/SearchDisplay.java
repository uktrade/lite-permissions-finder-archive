package models.search;

import controllers.search.routes;
import models.GoodsType;
import models.controlcode.ControlCodeSubJourney;
import play.data.Form;

public class SearchDisplay {
  public final ControlCodeSubJourney controlCodeSubJourney;
  public final Form<?> form;
  public final String formAction;
  public final String pageTitle;
  public final String descriptionLabel;
  public final String componentLabel;
  public final String brandLabel;
  public final String partNumberLabel;

  public SearchDisplay(ControlCodeSubJourney controlCodeSubJourney, Form<?> form) {
    this.controlCodeSubJourney = controlCodeSubJourney;
    this.form = form;
    if (controlCodeSubJourney == models.controlcode.ControlCodeSubJourney.PHYSICAL_GOODS_SEARCH) {
      this.formAction = routes.SearchController.handleSearchSubmit().url();
      this.pageTitle = "Describe your items";
      this.descriptionLabel = "Provide as much information as you can";
      this.componentLabel = "If the item is a component, attachment or part for another item, describe its use (optional)";
    }
    else if (controlCodeSubJourney == models.controlcode.ControlCodeSubJourney.PHYSICAL_GOODS_SEARCH_RELATED_TO_SOFTWARE) {
      this.formAction = routes.SearchController.handleSearchRelatedToSubmit(GoodsType.SOFTWARE.urlString()).url();
      this.pageTitle = "Describe the equipment or materials your software is related to";
      this.descriptionLabel = "Provide as much information as you can about the physical item your software is used with";
      this.componentLabel = "If the item your software is used with is a component, attachment or part for another item, describe the completed item or system (optional)";
    }
    else if (controlCodeSubJourney == models.controlcode.ControlCodeSubJourney.PHYSICAL_GOODS_SEARCH_RELATED_TO_TECHNOLOGY) {
      this.formAction = routes.SearchController.handleSearchRelatedToSubmit(GoodsType.TECHNOLOGY.urlString()).url();
      this.pageTitle = "Describe the equipment or materials your technology is related to";
      this.descriptionLabel = "Provide as much information as you can about the physical item your technology is used with";
      this.componentLabel = "If the item your technology is used with is a component, attachment or part for another item, describe the completed item or system (optional)";
    }
    else {
      throw new RuntimeException(String.format("Unexpected member of ControlCodeSubJourney enum: \"%s\""
          , controlCodeSubJourney.toString()));
    }
    this.brandLabel = "Brand or manufacturer";
    this.partNumberLabel = "Part or model number";
  }
}
