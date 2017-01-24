package controllers.softtech;

import static java.util.concurrent.CompletableFuture.completedFuture;
import static play.mvc.Results.ok;

import com.google.inject.Inject;
import components.common.journey.JourneyManager;
import components.common.journey.StandardEvents;
import components.persistence.PermissionsFinderDao;
import exceptions.FormStateException;
import models.GoodsType;
import models.softtech.GoodsRelationshipDisplay;
import play.data.Form;
import play.data.FormFactory;
import play.data.validation.Constraints.Required;
import play.mvc.Result;
import views.html.softtech.goodsRelationship;

import java.util.Optional;
import java.util.concurrent.CompletionStage;

public class GoodsRelationshipController {

  private final FormFactory formFactory;
  private final JourneyManager journeyManager;
  private final PermissionsFinderDao permissionsFinderDao;

  @Inject
  public GoodsRelationshipController(FormFactory formFactory, JourneyManager journeyManager, PermissionsFinderDao permissionsFinderDao) {
    this.formFactory = formFactory;
    this.journeyManager = journeyManager;
    this.permissionsFinderDao = permissionsFinderDao;
  }

  @SuppressWarnings("OptionalGetWithoutIsPresent")
  public CompletionStage<Result> renderForm(String goodsTypeText, String relatedToGoodsTypeText){
    GoodsType goodsType = GoodsType.getMatchedByUrlString(goodsTypeText).get();
    GoodsType relatedToGoodsType = GoodsType.getMatchedByUrlString(relatedToGoodsTypeText).get();
    return renderFormInternal(goodsType, relatedToGoodsType);
  }

  private CompletionStage<Result> renderFormInternal(GoodsType goodsType, GoodsType relatedToGoodsType) {
    GoodsRelationshipForm templateForm = new GoodsRelationshipForm();
    Optional<Boolean> isRelatedToGoodsType = permissionsFinderDao.getIsRelatedToGoodsType(goodsType, relatedToGoodsType);
    templateForm.isRelatedToGoodsType = isRelatedToGoodsType.orElse(null);
    return completedFuture(ok(goodsRelationship.render(formFactory.form(GoodsRelationshipForm.class).fill(templateForm),
        new GoodsRelationshipDisplay(goodsType, relatedToGoodsType))));
  }

  @SuppressWarnings("OptionalGetWithoutIsPresent")
  public CompletionStage<Result> handleSubmit(String goodsTypeText, String relatedToGoodsTypeText){
    GoodsType goodsType = GoodsType.getMatchedByUrlString(goodsTypeText).get();
    GoodsType relatedToGoodsType = GoodsType.getMatchedByUrlString(relatedToGoodsTypeText).get();
    return handleSubmitInternal(goodsType, relatedToGoodsType);
  }

  private CompletionStage<Result> handleSubmitInternal(GoodsType goodsType, GoodsType relatedToGoodsType) {
    if (!validateGoodsTypeRelationship(goodsType, relatedToGoodsType)) {
      throw new RuntimeException(String.format("Invalid goodsType and relatedToGoodsType combination. %s, %s",
          goodsType.value(), relatedToGoodsType.value()));
    }
    Form<GoodsRelationshipForm> form = formFactory.form(GoodsRelationshipForm.class).bindFromRequest();
    if (form.hasErrors()) {
      return renderFormInternal(goodsType, relatedToGoodsType);
    }

    Boolean isRelatedToGoodsType = form.get().isRelatedToGoodsType;
    if (isRelatedToGoodsType) {
      permissionsFinderDao.saveIsRelatedToGoodsType(goodsType, relatedToGoodsType, true);
      return journeyManager.performTransition(StandardEvents.YES);
    }
    else {
      permissionsFinderDao.saveIsRelatedToGoodsType(goodsType, relatedToGoodsType, false);
      return journeyManager.performTransition(StandardEvents.NO);
    }
  }

  private boolean validateGoodsTypeRelationship(GoodsType goodsType, GoodsType relatedToGoodsType) {
    // Valid goodsType
    if (goodsType != GoodsType.SOFTWARE && goodsType != GoodsType.TECHNOLOGY) {
      return false;
    }
    // Valid relatedToGoodsType
    else if (relatedToGoodsType != GoodsType.SOFTWARE && relatedToGoodsType != GoodsType.TECHNOLOGY) {
      return false;
    }
    // Check for error case
    else if (goodsType == GoodsType.TECHNOLOGY && relatedToGoodsType == GoodsType.TECHNOLOGY) {
      return false;
    }
    else {
      return true;
    }
  }

  public static class GoodsRelationshipForm {

    @Required
    public Boolean isRelatedToGoodsType;

  }
}
