package controllers.categories;

import static java.util.concurrent.CompletableFuture.completedFuture;
import static play.mvc.Results.ok;

import com.google.inject.Inject;
import components.common.journey.JourneyManager;
import components.persistence.PermissionsFinderDao;
import journey.Events;
import model.ExportCategory;
import play.data.Form;
import play.data.FormFactory;
import play.data.validation.Constraints.Required;
import play.mvc.Result;
import views.html.categories.dualUse;

import java.util.Optional;
import java.util.concurrent.CompletionStage;

public class DualUseController {

  private final JourneyManager jm;
  private final FormFactory formFactory;
  private final PermissionsFinderDao dao;

  @Inject
  public DualUseController(JourneyManager jm,
                           FormFactory formFactory,
                           PermissionsFinderDao dao) {
    this.jm = jm;
    this.formFactory = formFactory;
    this.dao = dao;
  }

  public Result renderForm() {
    DualUseForm formTemplate = new DualUseForm();
    Optional<Boolean> isDualUse = dao.getIsDualUseGood();
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
    dao.saveIsDualUseGood(isDualUse);

    if (isDualUse) {
      dao.saveExportCategory(ExportCategory.DUAL_USE);
      return jm.performTransition(Events.GOOD_CONTROLLED);
    }
    else {
      dao.saveExportCategory(ExportCategory.NONE);
      return jm.performTransition(Events.GOOD_NOT_CONTROLLED);
    }
  }

  public static class DualUseForm {

    @Required(message = "You must answer this question")
    public String isDualUse;

  }
}
