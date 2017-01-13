package models.softtech;

import components.services.controlcode.controls.relationships.GoodsRelationship;
import models.GoodsType;
import controllers.softtech.routes;

public class GoodsRelationshipQuestionsDisplay {

  public final String formAction;
  public final String pageTitle;
  public final GoodsRelationship relationship;

  public GoodsRelationshipQuestionsDisplay(GoodsType goodsType, GoodsType relatedToGoodsType, GoodsRelationship relationship) {
    if (goodsType == GoodsType.SOFTWARE) {
      if (relatedToGoodsType == GoodsType.SOFTWARE) {
        this.pageTitle = "Software related to technology";
      }
      else if (relatedToGoodsType == GoodsType.SOFTWARE) {
        this.pageTitle = "Software related to software";
      }
      else {
        this.pageTitle = "";
      }
    }
    else if (goodsType == GoodsType.TECHNOLOGY) {
      if (relatedToGoodsType == GoodsType.SOFTWARE) {
        this.pageTitle = "Technology related to software";
      }
      else {
        this.pageTitle = "";
      }
    }
    else {
      this.pageTitle = "";
    }
    this.formAction = routes.GoodsRelationshipQuestionsController.handleSubmit(goodsType.urlString(), relatedToGoodsType.urlString()).url();
    this.relationship = relationship;
  }
}
