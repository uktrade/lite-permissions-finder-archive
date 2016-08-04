package controllers.controlcode;

import static play.mvc.Results.ok;

import com.google.inject.Inject;
import play.data.FormFactory;
import play.mvc.Result;

public class TechnicalNotesController {

  private final FormFactory formFactory;

  @Inject
  public TechnicalNotesController(FormFactory formFactory) {
    this.formFactory = formFactory;
  }

  public Result renderForm(){
    return ok(views.html.controlcode.technicalNotes.render(formFactory.form(TechnicalNotesController.TechnicalNotesForm.class)));
  }

  public Result handleSubmit() {
    return ok("technical notes submit not yet implemented");
  }

  public class TechnicalNotesForm {

  }

}
