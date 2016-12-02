package models.softtech;

import models.GoodsType;
import controllers.softtech.routes;

public class DualUseSoftTechCategoriesDisplay {
  public final String formAction;
  public final String pageTitle;

  public DualUseSoftTechCategoriesDisplay(GoodsType goodsType) {
    if (goodsType == GoodsType.SOFTWARE) {
      this.formAction = routes.DualUseSoftTechCategoriesController.handleSubmit(goodsType.toUrlString()).url();
      this.pageTitle = "What is your software for?";
    }
    else if (goodsType == GoodsType.TECHNOLOGY) {
      this.formAction = routes.DualUseSoftTechCategoriesController.handleSubmit(goodsType.toUrlString()).url();
      this.pageTitle = "What is your technology for?";
    }
    else {
      throw new RuntimeException(String.format("Unexpected member of GoodsType enum: \"%s\""
          , goodsType.toString()));
    }
  }
}
