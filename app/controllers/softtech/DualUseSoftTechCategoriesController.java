package controllers.softtech;

import static java.util.concurrent.CompletableFuture.completedFuture;
import static play.mvc.Results.ok;

import com.google.inject.Inject;
import components.common.journey.JourneyManager;
import components.common.journey.StandardEvents;
import components.persistence.PermissionsFinderDao;
import exceptions.FormStateException;
import journey.Events;
import journey.helpers.SoftTechJourneyHelper;
import models.GoodsType;
import models.softtech.DualUseSoftTechCategoriesDisplay;
import models.softtech.SoftTechCategory;
import org.apache.commons.lang3.StringUtils;
import play.data.Form;
import play.data.FormFactory;
import play.mvc.Result;
import views.html.softtech.dualUseSoftTechCategories;

import java.util.Optional;
import java.util.concurrent.CompletionStage;

public class DualUseSoftTechCategoriesController {

  private final JourneyManager journeyManager;
  private final FormFactory formFactory;
  private final PermissionsFinderDao permissionsFinderDao;

  @Inject
  public DualUseSoftTechCategoriesController(FormFactory formFactory,
                                             PermissionsFinderDao permissionsFinderDao,
                                             JourneyManager journeyManager) {
    this.formFactory = formFactory;
    this.permissionsFinderDao = permissionsFinderDao;
    this.journeyManager = journeyManager;
  }

  public CompletionStage<Result> renderForm(String goodsTypeText) {
    return SoftTechJourneyHelper.validateGoodsTypeAndGetResult(goodsTypeText, this::renderFormInternal);
  }

  private CompletionStage<Result> renderFormInternal(GoodsType goodsType) {
    return completedFuture(ok(dualUseSoftTechCategories.render(formFactory.form(DualUseSoftTechCategoriesForm.class),
        new DualUseSoftTechCategoriesDisplay(goodsType))));
  }

  public CompletionStage<Result> handleSubmit(String goodsTypeText) {
    return SoftTechJourneyHelper.validateGoodsTypeAndGetResult(goodsTypeText, this::handleSubmitInternal);
  }

  private CompletionStage<Result> handleSubmitInternal(GoodsType goodsType) {
    Form<DualUseSoftTechCategoriesForm> form = formFactory.form(DualUseSoftTechCategoriesForm.class).bindFromRequest();
    if (form.hasErrors()) {
      return completedFuture(ok(dualUseSoftTechCategories.render(form, new DualUseSoftTechCategoriesDisplay(goodsType))));
    }
    Optional<SoftTechCategory> softTechCategoryOptional = SoftTechCategory.getMatched(form.get().dualUseSoftTechCategory);
    String action = form.get().action;

    if (StringUtils.isNotEmpty(action)) {
      if ("noneOfTheAbove".equals(action)) {
        // The user has identified this as a dual use item, however no categories have matched their selection
        permissionsFinderDao.saveSoftTechCategory(goodsType, SoftTechCategory.DUAL_USE_UNSPECIFIED);
        return journeyManager.performTransition(Events.NONE_MATCHED);
      }
      else {
        throw new FormStateException(String.format("Unknown value for action: \"%s\"", action));
      }
    }
    else if (softTechCategoryOptional.isPresent()) {
      SoftTechCategory softTechCategory = softTechCategoryOptional.get();
      if (softTechCategory.isDualUseSoftTechCategory()) {
        permissionsFinderDao.saveSoftTechCategory(goodsType, softTechCategory);
        return journeyManager.performTransition(StandardEvents.NEXT);
      }
      else {
        throw new RuntimeException(String.format("Unexpected member of SoftTechCategory enum: \"%s\""
            , softTechCategory.toString()));
      }
    }
    else {
      throw new FormStateException("Unhandled form state");
    }
  }

  public static class DualUseSoftTechCategoriesForm {

    public String dualUseSoftTechCategory;

    public String action;

  }
}
