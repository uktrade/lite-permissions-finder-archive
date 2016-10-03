package controllers;

import com.google.inject.Inject;
import play.data.Form;
import play.data.FormFactory;
import play.mvc.Controller;
import play.mvc.Result;
import views.html.index;


public class EntryPointController extends Controller {

  private final FormFactory formFactory;

  @Inject
  public EntryPointController(FormFactory formFactory) {
    this.formFactory = formFactory;
  }

  public Result index() {
    return ok(index.render());
  }

  public Result handleSubmit() {
    Form<EntryPointForm> form = formFactory.form(EntryPointForm.class).bindFromRequest();
    if (!form.hasErrors()) {
      String action = form.get().action;
      if ("start".equals(action)) {
        return redirect(routes.StartApplicationController.renderForm());
      }
      if ("continue".equals(action)) {
        return redirect(routes.ContinueApplicationController.renderForm());
      }
      return badRequest("Unknown value for action: \"" + action + "\"");
    }
    return badRequest("Invalid form state");
  }

  public static class EntryPointForm {

    public String action;

  }

}
