package controllers.software;

import static java.util.concurrent.CompletableFuture.completedFuture;
import static play.mvc.Results.ok;

import com.google.inject.Inject;
import components.common.journey.JourneyManager;
import components.common.journey.StandardEvents;
import components.persistence.PermissionsFinderDao;
import exceptions.FormStateException;

import play.data.Form;
import play.data.FormFactory;
import play.mvc.Result;
import views.html.software.exemptions;

import java.util.concurrent.CompletionStage;

public class ExemptionsController {

  private final JourneyManager journeyManager;
  private final FormFactory formFactory;
  private final PermissionsFinderDao permissionsFinderDao;

  @Inject
  public ExemptionsController(FormFactory formFactory, PermissionsFinderDao permissionsFinderDao, JourneyManager journeyManager) {
    this.formFactory = formFactory;
    this.permissionsFinderDao = permissionsFinderDao;
    this.journeyManager = journeyManager;
  }

  public Result renderForm() {
    ExemptionsForm templateForm = new ExemptionsForm();
    templateForm.doExemptionsApply = permissionsFinderDao.getDoExemptionsApply();
    return ok(exemptions.render(formFactory.form(ExemptionsForm.class).fill(templateForm)));
  }

  public CompletionStage<Result> handleSubmit() {
    Form<ExemptionsForm> form = formFactory.form(ExemptionsForm.class).bindFromRequest();
    if (form.hasErrors()) {
      return completedFuture(ok(exemptions.render(form)));
    }

    String doExemptionsApply = form.get().doExemptionsApply;

    if ("true".equals(doExemptionsApply)) {
      permissionsFinderDao.saveDoExemptionsApply(doExemptionsApply);
      return journeyManager.performTransition(StandardEvents.YES);
    }
    else if ("false".equals(doExemptionsApply)) {
      permissionsFinderDao.saveDoExemptionsApply(doExemptionsApply);
      return journeyManager.performTransition(StandardEvents.NO);
    }
    else {
      throw new FormStateException(String.format("Unknown value for doExemptionsApply: \"%s\"", doExemptionsApply));
    }

  }


  public static class ExemptionsForm {

    public String doExemptionsApply;

  }
}
