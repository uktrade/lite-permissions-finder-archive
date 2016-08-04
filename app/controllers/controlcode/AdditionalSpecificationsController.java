package controllers.controlcode;

import static play.mvc.Results.ok;

import com.google.inject.Inject;
import play.data.FormFactory;
import play.mvc.Result;
import views.html.controlcode.additionalSpecifications;

public class AdditionalSpecificationsController {

  private final FormFactory formFactory;

  private final DecontrolsController decontrolsController;

  @Inject
  public AdditionalSpecificationsController(FormFactory formFactory, DecontrolsController decontrolsController) {
    this.formFactory = formFactory;
    this.decontrolsController = decontrolsController;
  }

  public Result renderForm() {
    return ok(additionalSpecifications.render(formFactory.form(AdditionalSpecificationsForm.class)));
  }

  public Result handleSubmit() {
    return decontrolsController.renderForm();
  }

  public class AdditionalSpecificationsForm {

  }
}
