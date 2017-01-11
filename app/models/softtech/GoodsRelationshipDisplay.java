package models.softtech;

import models.GoodsType;
import controllers.softtech.routes;

public class GoodsRelationshipDisplay {

  public final String formAction;
  public final String pageTitle;
  public final String questionLabel;

  public GoodsRelationshipDisplay(GoodsType goodsType, GoodsType relatedToGoodsType) {
    this.formAction = routes.GoodsRelationshipController.handleSubmit(goodsType.urlString(), relatedToGoodsType.urlString()).url();

    if (goodsType == GoodsType.SOFTWARE) {
      if (relatedToGoodsType == GoodsType.SOFTWARE) {
        this.pageTitle = "Is software related to software?";
        this.questionLabel = "Is software related to software?";
      }
      else if (relatedToGoodsType == GoodsType.TECHNOLOGY) {
        this.pageTitle = "Is software related to technology?";
        this.questionLabel = "Is software related to technology?";
      }
      else {
        throw new RuntimeException(String.format("Unexpected member of GoodsType enum: \"%s\" for parameter relatedToGoodsType", relatedToGoodsType.toString()));
      }
    }
    else if (goodsType == GoodsType.TECHNOLOGY) {
      if (relatedToGoodsType == GoodsType.SOFTWARE) {
        this.pageTitle = "Is technology related to software?";
        this.questionLabel = "Is technology related to software";
      }
      else {
        throw new RuntimeException(String.format("Unexpected member of GoodsType enum: \"%s\" for parameter relatedToGoodsType", relatedToGoodsType.toString()));
      }
    }
    else {
      throw new RuntimeException(String.format("Unexpected member of GoodsType enum: \"%s\" for parameter goodsType", goodsType.toString()));
    }
  }
}
