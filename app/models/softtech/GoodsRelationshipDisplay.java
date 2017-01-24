package models.softtech;

import models.GoodsType;
import controllers.softtech.routes;

public class GoodsRelationshipDisplay {

  public final String formAction;
  public final String pageTitle;
  public final String questionLabel;
  public final String questionContent;

  public GoodsRelationshipDisplay(GoodsType goodsType, GoodsType relatedToGoodsType) {
    this.formAction = routes.GoodsRelationshipController.handleSubmit(goodsType.urlString(), relatedToGoodsType.urlString()).url();

    if (goodsType == GoodsType.SOFTWARE) {
      if (relatedToGoodsType == GoodsType.SOFTWARE) {
        this.pageTitle = "Is software related to software?";
        this.questionLabel = "Is software related to software?";
        this.questionContent = null;
      }
      else if (relatedToGoodsType == GoodsType.TECHNOLOGY) {
        this.pageTitle = "Software related to technology";
        this.questionLabel = "Are you exporting software for the development, production or use of technical information for licensable items?";
        this.questionContent = "For example software used to produce manuals, designs, models or blueprints for goods that themselves need a licence to be exported.";
      }
      else {
        throw new RuntimeException(String.format("Unexpected member of GoodsType enum: \"%s\" for parameter relatedToGoodsType", relatedToGoodsType.toString()));
      }
    }
    else if (goodsType == GoodsType.TECHNOLOGY) {
      if (relatedToGoodsType == GoodsType.SOFTWARE) {
        this.pageTitle = "Is technology related to software?";
        this.questionLabel = "Is technology related to software";
        this.questionContent = null;
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
