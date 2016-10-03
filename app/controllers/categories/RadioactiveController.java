package controllers.categories;

import static java.util.concurrent.CompletableFuture.completedFuture;
import static play.mvc.Results.badRequest;
import static play.mvc.Results.ok;

import com.google.inject.Inject;
import components.common.journey.JourneyManager;
import components.common.journey.StandardEvents;
import play.data.Form;
import play.data.FormFactory;
import play.mvc.Result;
import views.html.categories.radioactive;

import java.util.concurrent.CompletionStage;

public class RadioactiveController {

  private final JourneyManager journeyManager;
  private final FormFactory formFactory;

  @Inject
  public RadioactiveController(JourneyManager journeyManager, FormFactory formFactory) {
    this.journeyManager = journeyManager;
    this.formFactory = formFactory;
  }

  public Result renderForm() {
    return ok(radioactive.render(formFactory.form(RadioactiveForm.class)));
  }

  public CompletionStage<Result> handleSubmit() {
    Form<RadioactiveForm> form = formFactory.form(RadioactiveForm.class).bindFromRequest();
    if ("true".equals(form.get().goToSearch)) {
      return journeyManager.performTransition(StandardEvents.NEXT);
    }
    return completedFuture(badRequest("Unknown value of goToSearch: \"" + form.get().goToSearch + "\""));
  }

  public static class RadioactiveForm {

    public String goToSearch;

  }
}
