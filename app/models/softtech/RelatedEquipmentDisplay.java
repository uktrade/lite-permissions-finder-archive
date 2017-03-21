package models.softtech;

import models.GoodsType;
import controllers.softtech.routes;

import java.util.Arrays;
import java.util.List;

public class RelatedEquipmentDisplay {
  public final String formAction;
  public final String questionHeading;
  public final List<String> questionBullets;

  public RelatedEquipmentDisplay(GoodsType goodsType) {
    if (goodsType == GoodsType.SOFTWARE) {
      this.formAction = routes.RelatedEquipmentController.handleSubmit(goodsType.urlString()).url();
      this.questionHeading = "Is your software any of the following?";
      this.questionBullets = Arrays.asList("For the development, production, use, operation, repair or maintenance of equipment or materials",
          "To enhance or modify equipment to exceed its normal performance limits",
          "To perform or simulate the functions of cryptographic equipment, such as secure communications equipment");
    }
    else if (goodsType == GoodsType.TECHNOLOGY) {
      this.formAction = routes.RelatedEquipmentController.handleSubmit(goodsType.urlString()).url();
      this.questionHeading = "Is your technology any of the following?";
      this.questionBullets = Arrays.asList("For the development, production, use, operation, repair or maintenance of equipment or materials",
          "To enable equipment to exceed its normal performance limits",
          "To enable equipment, such as secure communications equipment, to perform or simulate cryptographic functions");
    }
    else {
      throw new RuntimeException(String.format("Unexpected member of GoodsType enum: \"%s\""
          , goodsType.toString()));
    }
  }
}
