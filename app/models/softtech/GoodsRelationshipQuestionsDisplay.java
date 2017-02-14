package models.softtech;

import components.common.journey.BackLink;
import models.GoodsType;
import controllers.softtech.routes;
import uk.gov.bis.lite.controlcode.api.view.GoodsRelationshipFullView;
import utils.common.ViewUtil;

public class GoodsRelationshipQuestionsDisplay {

  public final String formAction;
  public final String pageTitle;
  public final GoodsRelationshipFullView relationship;

  public GoodsRelationshipQuestionsDisplay(GoodsType goodsType, GoodsType relatedToGoodsType, GoodsRelationshipFullView relationship, int currentQuestionIndex) {
    this.pageTitle = relationship.getControlEntryHeading();
    this.formAction = routes.GoodsRelationshipQuestionsController.handleSubmit(goodsType.urlString(), relatedToGoodsType.urlString()).url();
    this.relationship = relationship;
    /** Override back link handling if prior question exists */
    if (currentQuestionIndex > 0) {
      ViewUtil.overrideBackLink(BackLink.to(routes.GoodsRelationshipQuestionsController.handleBack(goodsType.urlString(), relatedToGoodsType.urlString(), Integer.toString(currentQuestionIndex)), "Back"));
    }
  }
}
