package models.search;

import controllers.search.routes;
import models.GoodsType;
import models.controlcode.ControlCodeJourney;
import play.data.Form;

public class SearchBaseDisplay {
  public final ControlCodeJourney controlCodeJourney;
  public final Form<?> form;
  public final String formAction;
  public final String pageTitle;

  public SearchBaseDisplay(ControlCodeJourney controlCodeJourney, Form<?> form) {
    this.controlCodeJourney = controlCodeJourney;
    this.form = form;
    if (controlCodeJourney == ControlCodeJourney.PHYSICAL_GOODS_SEARCH) {
      this.pageTitle = "Describe your items";
      this.formAction = routes.PhysicalGoodsSearchController.handleSearchSubmit().url();
    }
    else if (controlCodeJourney == ControlCodeJourney.PHYSICAL_GOODS_SEARCH_RELATED_TO_SOFTWARE) {
      this.pageTitle = "Describe your items";
      this.formAction = routes.PhysicalGoodsSearchController.handleSearchRelatedToSubmit(GoodsType.SOFTWARE.toUrlString()).url();
    }
    else if (controlCodeJourney == ControlCodeJourney.PHYSICAL_GOODS_SEARCH_RELATED_TO_TECHNOLOGY) {
      this.pageTitle = "Describe your items";
      this.formAction = routes.PhysicalGoodsSearchController.handleSearchRelatedToSubmit(GoodsType.TECHNOLOGY.toUrlString()).url();
    }
    else {
      throw new RuntimeException(String.format("Unexpected member of ControlCodeJourney enum: \"%s\""
          , controlCodeJourney.toString()));
    }
  }
}
