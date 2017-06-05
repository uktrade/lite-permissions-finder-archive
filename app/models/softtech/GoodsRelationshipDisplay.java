package models.softtech;

import models.GoodsType;
import controllers.softtech.routes;

public class GoodsRelationshipDisplay {

  public final String formAction;
  public final String questionLabel;
  public final String questionContent;

  public GoodsRelationshipDisplay(GoodsType goodsType, GoodsType relatedToGoodsType) {
    this.formAction = routes.GoodsRelationshipController.handleSubmit(goodsType.urlString(), relatedToGoodsType.urlString()).url();

    if (goodsType == GoodsType.SOFTWARE) {
      if (relatedToGoodsType == GoodsType.SOFTWARE) {
        this.questionLabel = "Are you exporting software for the development, production or use of other software?";
        this.questionContent = "For example software to create computer programs for use with controlled goods.";
      }
      else if (relatedToGoodsType == GoodsType.TECHNOLOGY) {
        this.questionLabel = "Are you exporting software for the development, production or use of technical information?";
        this.questionContent = "For example software used to produce manuals, designs, models or blueprints for goods that themselves need a licence to be exported.";
      }
      else {
        throw new RuntimeException(String.format("Unexpected member of GoodsType enum: \"%s\" for parameter relatedToGoodsType", relatedToGoodsType.toString()));
      }
    }
    else if (goodsType == GoodsType.TECHNOLOGY) {
      if (relatedToGoodsType == GoodsType.SOFTWARE) {
        this.questionLabel = "Are you exporting technical information for the development, production or use of software?";
        this.questionContent = "For example manuals for software used to operate or control equipment that itself needs a licence to be exported.";
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
