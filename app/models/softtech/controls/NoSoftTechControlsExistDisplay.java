package models.softtech.controls;

import controllers.softtech.routes;
import models.GoodsType;

public class NoSoftTechControlsExistDisplay {
  public final String formAction;
  public final String pageTitle;

  public NoSoftTechControlsExistDisplay(GoodsType goodsType) {
    if (goodsType == GoodsType.SOFTWARE) {
      this.formAction = routes.DualUseSoftTechCategoriesController.handleSubmit(goodsType.urlString()).url();
      this.pageTitle = "No software controls exist for item";
    }
    else if (goodsType == GoodsType.TECHNOLOGY) {
      this.formAction = routes.DualUseSoftTechCategoriesController.handleSubmit(goodsType.urlString()).url();
      this.pageTitle = "No technology controls exist for item";
    }
    else {
      throw new RuntimeException(String.format("Unexpected member of GoodsType enum: \"%s\""
          , goodsType.toString()));
    }
  }
}
