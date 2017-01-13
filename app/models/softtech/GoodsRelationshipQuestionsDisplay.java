package models.softtech;

import components.common.journey.BackLink;
import components.services.controlcode.controls.relationships.GoodsRelationship;
import models.GoodsType;
import controllers.softtech.routes;
import utils.common.ViewUtil;

public class GoodsRelationshipQuestionsDisplay {

  public final String formAction;
  public final String pageTitle;
  public final GoodsRelationship relationship;

  public GoodsRelationshipQuestionsDisplay(GoodsType goodsType, GoodsType relatedToGoodsType, GoodsRelationship relationship, int currentQuestionIndex) {
    if (goodsType == GoodsType.SOFTWARE) {
      if (relatedToGoodsType == GoodsType.SOFTWARE) {
        this.pageTitle = "Software related to technology";
      }
      else if (relatedToGoodsType == GoodsType.TECHNOLOGY) {
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
    /** Override back link handling if prior question exists */
    if (currentQuestionIndex > 0) {
      ViewUtil.overrideBackLink(BackLink.to(routes.GoodsRelationshipQuestionsController.handleBack(goodsType.urlString(), relatedToGoodsType.urlString(), Integer.toString(currentQuestionIndex)), pageTitle));
    }
  }
}
