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

  private final JourneyManager jm;
  private final FormFactory formFactory;
  private final PermissionsFinderDao dao;

  @Inject
  public TortureRestraintController(JourneyManager jm, FormFactory formFactory, PermissionsFinderDao dao) {
    this.jm = jm;
    this.formFactory = formFactory;
    this.dao = dao;
  }

  public Result renderForm() {
    return ok(tortureRestraint.render(formFactory.form(TortureRestraintForm.class)));
  }

  public CompletionStage<Result> handleSubmit() {
    Form<TortureRestraintForm> form = formFactory.form(TortureRestraintForm.class).bindFromRequest();
    if ("true".equals(form.get().goToSearch)) {
      dao.saveExportCategory(ExportCategory.TORTURE_RESTRAINT);
      return jm.performTransition(StandardEvents.NEXT);
    }
    return completedFuture(badRequest("Unknown value of goToSearch: \"" + form.get().goToSearch + "\""));
  }

  public static class TortureRestraintForm {

    public String goToSearch;

  }
}
