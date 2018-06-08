package controllers;

import com.google.inject.Inject;
import exceptions.FormStateException;
import play.data.Form;
import play.data.FormFactory;
import play.mvc.Controller;
import play.mvc.Result;

public class EntryPointController extends Controller {

  private final FormFactory formFactory;
  private final views.html.index index;

  @Inject
  public EntryPointController(FormFactory formFactory, views.html.index index) {
    this.formFactory = formFactory;
    this.index = index;
  }

  public Result index() {
    return ok(index.render());
  }

  public Result handleSubmit() {
    Form<EntryPointForm> form = formFactory.form(EntryPointForm.class).bindFromRequest();
    if (!form.hasErrors()) {
      String action = form.get().action;
      if ("start".equals(action)) {
        return redirect(routes.StartApplicationController.createApplication());
      }
      if ("continue".equals(action)) {
        return redirect(routes.ContinueApplicationController.renderForm());
      }
      throw new FormStateException("Unknown value for action: \"" + action + "\"");
    }
    throw new FormStateException("Unhandled form state");
  }

  public static class EntryPointForm {

    public String action;

  }

}
