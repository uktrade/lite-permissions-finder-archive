package controllers.controlcode;

import static play.mvc.Results.ok;

import com.google.inject.Inject;
import play.data.FormFactory;
import play.mvc.Result;

public class DecontrolsController {

  private final FormFactory formFactory;

  private final TechnicalNotesController technicalNotesController;

  @Inject
  public DecontrolsController(FormFactory formFactory, TechnicalNotesController technicalNotesController) {
    this.formFactory = formFactory;
    this.technicalNotesController = technicalNotesController;
  }

  public Result renderForm() {
    return ok(views.html.controlcode.decontrols.render(formFactory.form(DecontrolsController.DecontrolsForm.class)));
  }

  public Result handleSubmit(){
    return technicalNotesController.renderForm();
  }

  public class DecontrolsForm {

  }

}
