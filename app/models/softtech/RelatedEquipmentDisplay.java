package models.softtech;

import models.GoodsType;
import controllers.softtech.routes;
public class RelatedEquipmentDisplay {
  public final String formAction;
  public final String pageTitle;

  public RelatedEquipmentDisplay(GoodsType goodsType) {
    if (goodsType == GoodsType.SOFTWARE) {
      this.formAction = routes.RelatedEquipmentController.handleSubmit(goodsType.toUrlString()).url();
      this.pageTitle = "Software related to equipment or materials?";
    }
    else if (goodsType == GoodsType.TECHNOLOGY) {
      this.formAction = routes.RelatedEquipmentController.handleSubmit(goodsType.toUrlString()).url();
      this.pageTitle = "Technology related to equipment or materials?";
    }
    else {
      throw new RuntimeException(String.format("Unexpected member of GoodsType enum: \"%s\""
          , goodsType.toString()));
    }
  }
}
