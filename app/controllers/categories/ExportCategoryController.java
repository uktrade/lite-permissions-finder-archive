package controllers.categories;

import static java.util.concurrent.CompletableFuture.completedFuture;

import com.google.inject.Inject;
import components.common.journey.JourneyManager;
import components.persistence.PermissionsFinderDao;
import journey.Events;
import model.ExportCategory;
import play.data.Form;
import play.data.FormFactory;
import play.mvc.Controller;
import play.mvc.Result;
import views.html.categories.selectExportCategories;

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
    return ok(selectExportCategories.render(formFactory.form()));
  }

  public CompletionStage<Result> handleSubmit() {
    Form<ExportCategoryForm> form = formFactory.form(ExportCategoryForm.class).bindFromRequest();

    Optional<ExportCategory> exportCategoryOptional = ExportCategory.getMatched(form.get().category);

    if (exportCategoryOptional.isPresent()) {
      permissionsFinderDao.saveExportCategory(exportCategoryOptional.get());
      return journeyManager.performTransition(Events.EXPORT_CATEGORY_SELECTED, exportCategoryOptional.get());
    }
    if ("true".equals(form.get().couldBeDualUse)) {
      return journeyManager.performTransition(Events.EXPORT_CATEGORY_COULD_BE_DUAL_USE);
    }

    return completedFuture(badRequest("Unknown export category: \"" + form.get().category + "\""));
  }

  public static class ExportCategoryForm {

    public String category;

    public String couldBeDualUse;

  }
}
