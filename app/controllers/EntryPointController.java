package controllers;

import com.google.inject.Inject;
import exceptions.UnknownParameterException;
import lombok.Getter;
import lombok.Setter;
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
    String action = form.rawData().get("action");
    if ("start".equals(action)) {
      return redirect(routes.StartApplicationController.createApplication());
    } else if ("continue".equals(action)) {
      return redirect(routes.ContinueApplicationController.renderForm());
    } else {
      throw UnknownParameterException.unknownAction(action);
    }
  }

  public static class EntryPointForm {

    private @Getter @Setter String action;

  }

}
