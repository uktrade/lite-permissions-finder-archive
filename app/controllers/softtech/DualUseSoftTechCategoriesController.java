package controllers.softtech;

import static java.util.concurrent.CompletableFuture.completedFuture;
import static journey.helpers.SoftTechJourneyHelper.validateThenGetResult;
import static play.mvc.Results.ok;

import com.google.inject.Inject;
import components.common.journey.JourneyManager;
import components.persistence.PermissionsFinderDao;
import exceptions.FormStateException;
import journey.Events;
import journey.helpers.SoftTechJourneyHelper;
import models.GoodsType;
import models.softtech.ApplicableSoftTechControls;
import models.softtech.DualUseSoftTechCategoriesDisplay;
import models.softtech.SoftTechCategory;
import org.apache.commons.lang3.StringUtils;
import play.data.Form;
import play.data.FormFactory;
import play.libs.concurrent.HttpExecutionContext;
import play.mvc.Result;
import views.html.softtech.dualUseSoftTechCategories;

import java.util.concurrent.CompletionStage;

public class DualUseSoftTechCategoriesController {

  private final JourneyManager journeyManager;
  private final FormFactory formFactory;
  private final PermissionsFinderDao permissionsFinderDao;
  private final SoftTechJourneyHelper softTechJourneyHelper;
  private final HttpExecutionContext httpExecutionContext;

  @Inject
  public DualUseSoftTechCategoriesController(FormFactory formFactory, PermissionsFinderDao permissionsFinderDao,
                                             JourneyManager journeyManager, SoftTechJourneyHelper softTechJourneyHelper, HttpExecutionContext httpExecutionContext) {
    this.formFactory = formFactory;
    this.permissionsFinderDao = permissionsFinderDao;
    this.journeyManager = journeyManager;
    this.softTechJourneyHelper = softTechJourneyHelper;
    this.httpExecutionContext = httpExecutionContext;
  }

  public CompletionStage<Result> renderForm(String goodsTypeText) {
    return validateThenGetResult(goodsTypeText, this::renderFormInternal);
  }

  private CompletionStage<Result> renderFormInternal(GoodsType goodsType) {
    return completedFuture(ok(dualUseSoftTechCategories.render(formFactory.form(DualUseSoftTechCategoriesForm.class),
        new DualUseSoftTechCategoriesDisplay(goodsType))));
  }

  public CompletionStage<Result> handleSubmit(String goodsTypeText) {
    return validateThenGetResult(goodsTypeText, this::handleSubmitInternal);
  }

  private CompletionStage<Result> handleSubmitInternal(GoodsType goodsType) {
    Form<DualUseSoftTechCategoriesForm> form = formFactory.form(DualUseSoftTechCategoriesForm.class).bindFromRequest();
    if (form.hasErrors()) {
      return completedFuture(ok(dualUseSoftTechCategories.render(form, new DualUseSoftTechCategoriesDisplay(goodsType))));
    }
    String dualUseSoftwareCategoryText = form.get().dualUseSoftTechCategory;
    String action = form.get().action;

    if (StringUtils.isNotEmpty(action)) {
      if ("noneOfTheAbove".equals(action)) {
        return journeyManager.performTransition(Events.NONE_MATCHED);
      }
      else {
        throw new FormStateException(String.format("Unknown value for action: \"%s\"", action));
      }
    }
    else if (StringUtils.isNotEmpty(dualUseSoftwareCategoryText)) {
      SoftTechCategory softTechCategory = SoftTechCategory.valueOf(dualUseSoftwareCategoryText);
      if (softTechCategory.isDualUseSoftTechCategory()) {
        permissionsFinderDao.saveSoftTechCategory(goodsType, softTechCategory);
        return softTechJourneyHelper.checkSoftTechControls(goodsType, softTechCategory, true) // Save to DAO
            .thenComposeAsync(this::dualUseSoftTechCategorySelected, httpExecutionContext.current());
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

  private CompletionStage<Result> dualUseSoftTechCategorySelected(ApplicableSoftTechControls applicableSoftTechControls) {
    return journeyManager.performTransition(Events.DUAL_USE_SOFT_TECH_CATEGORY_SELECTED, applicableSoftTechControls);
  }

  public static class DualUseSoftTechCategoriesForm {

    public String dualUseSoftTechCategory;

    public String action;

  }
}
