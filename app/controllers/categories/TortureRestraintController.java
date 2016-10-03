package controllers.categories;

import static java.util.concurrent.CompletableFuture.completedFuture;
import static play.mvc.Results.badRequest;
import static play.mvc.Results.ok;

import com.google.inject.Inject;
import components.common.journey.JourneyManager;
import components.common.journey.StandardEvents;
import components.persistence.PermissionsFinderDao;
import model.ExportCategory;
import play.data.Form;
import play.data.FormFactory;
import play.mvc.Result;
import views.html.categories.tortureRestraint;

import java.util.concurrent.CompletionStage;

public class TortureRestraintController {

  private final JourneyManager journeyManager;
  private final FormFactory formFactory;
  private final PermissionsFinderDao permissionsFinderDao;

  @Inject
  public TortureRestraintController(JourneyManager journeyManager, FormFactory formFactory, PermissionsFinderDao permissionsFinderDao) {
    this.journeyManager = journeyManager;
    this.formFactory = formFactory;
    this.permissionsFinderDao = permissionsFinderDao;
  }

  public Result renderForm() {
    return ok(tortureRestraint.render(formFactory.form(TortureRestraintForm.class)));
  }

  public CompletionStage<Result> handleSubmit() {
    Form<TortureRestraintForm> form = formFactory.form(TortureRestraintForm.class).bindFromRequest();
    if ("true".equals(form.get().goToSearch)) {
      permissionsFinderDao.saveExportCategory(ExportCategory.TORTURE_RESTRAINT);
      return journeyManager.performTransition(StandardEvents.NEXT);
    }
    return completedFuture(badRequest("Unknown value of goToSearch: \"" + form.get().goToSearch + "\""));
  }

  public static class TortureRestraintForm {

    public String goToSearch;

  }
}
