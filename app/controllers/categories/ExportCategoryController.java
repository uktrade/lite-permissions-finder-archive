package controllers.categories;

import com.google.inject.Inject;
import components.common.journey.JourneyManager;
import components.persistence.PermissionsFinderDao;
import exceptions.FormStateException;
import journey.Events;
import models.ExportCategory;
import play.data.Form;
import play.data.FormFactory;
import play.mvc.Controller;
import play.mvc.Result;
import views.html.categories.exportCategories;

import java.util.Optional;
import java.util.concurrent.CompletionStage;

public class ExportCategoryController extends Controller {

  private final JourneyManager journeyManager;
  private final FormFactory formFactory;
  private final PermissionsFinderDao permissionsFinderDao;

  @Inject
  public ExportCategoryController(JourneyManager journeyManager, FormFactory formFactory, PermissionsFinderDao permissionsFinderDao) {
    this.journeyManager = journeyManager;
    this.formFactory = formFactory;
    this.permissionsFinderDao = permissionsFinderDao;
  }

  public Result renderForm() {
    return ok(exportCategories.render(formFactory.form()));
  }

  public CompletionStage<Result> handleSubmit() {
    Form<ExportCategoryForm> form = formFactory.form(ExportCategoryForm.class).bindFromRequest();

    Optional<ExportCategory> exportCategoryOptional = ExportCategory.getMatched(form.get().category);

    if (exportCategoryOptional.isPresent()) {
      permissionsFinderDao.saveExportCategory(exportCategoryOptional.get());
      // This is cleared as both CHEMICALS_COSMETICS and TORTURE_RESTRAINT saves to the goodsType dao field and skips
      // the goodsType view. Clearing prevents the goodsType view being erroneously pre-populated in some scenarios
      permissionsFinderDao.clearGoodsType();
      return journeyManager.performTransition(Events.EXPORT_CATEGORY_SELECTED, exportCategoryOptional.get());
    }
    if ("true".equals(form.get().couldBeDualUse)) {
      return journeyManager.performTransition(Events.EXPORT_CATEGORY_COULD_BE_DUAL_USE);
    }

    throw new FormStateException("Unknown export category: \"" + form.get().category + "\"");
  }

  public static class ExportCategoryForm {

    public String category;

    public String couldBeDualUse;

  }
}
