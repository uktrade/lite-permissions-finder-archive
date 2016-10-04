package controllers.categories;

import static java.util.concurrent.CompletableFuture.completedFuture;
import static play.mvc.Results.ok;

import com.google.inject.Inject;
import components.common.journey.JourneyManager;
import components.persistence.PermissionsFinderDao;
import journey.Events;
import models.ExportCategory;
import play.data.Form;
import play.data.FormFactory;
import play.data.validation.Constraints.Required;
import play.mvc.Result;
import views.html.categories.dualUse;

import java.util.Optional;
import java.util.concurrent.CompletionStage;

public class DualUseController {

  private final JourneyManager journeyManager;
  private final FormFactory formFactory;
  private final PermissionsFinderDao permissionsFinderDao;

  @Inject
  public DualUseController(JourneyManager journeyManager,
                           FormFactory formFactory,
                           PermissionsFinderDao permissionsFinderDao) {
    this.journeyManager = journeyManager;
    this.formFactory = formFactory;
    this.permissionsFinderDao = permissionsFinderDao;
  }

  public Result renderForm() {
    DualUseForm formTemplate = new DualUseForm();
    Optional<Boolean> isDualUse = permissionsFinderDao.getIsDualUseGood();
    if (isDualUse.isPresent()) {
      formTemplate.isDualUse = isDualUse.get().toString();
    }
    return ok(dualUse.render(formFactory.form(DualUseForm.class).fill(formTemplate)));
  }

  public CompletionStage<Result> handleSubmit() {
    Form<DualUseForm> form = formFactory.form(DualUseForm.class).bindFromRequest();
    if (form.hasErrors()) {
      return completedFuture(ok(dualUse.render(form)));
    }

    boolean isDualUse = Boolean.parseBoolean(form.get().isDualUse);
    permissionsFinderDao.saveIsDualUseGood(isDualUse);

    if (isDualUse) {
      permissionsFinderDao.saveExportCategory(ExportCategory.DUAL_USE);
      return journeyManager.performTransition(Events.IS_DUAL_USE, true);
    }
    else {
      permissionsFinderDao.saveExportCategory(ExportCategory.NONE);
      return journeyManager.performTransition(Events.IS_DUAL_USE, false);
    }
  }

  public static class DualUseForm {

    @Required(message = "You must answer this question")
    public String isDualUse;

  }
}
