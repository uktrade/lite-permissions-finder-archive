package controllers.controlcode;

import static play.mvc.Results.badRequest;
import static play.mvc.Results.ok;

import com.google.inject.Inject;
import components.services.controlcode.frontend.FrontendServiceResult;
import controllers.DestinationCountryController;
import play.data.Form;
import play.data.FormFactory;
import play.mvc.Result;
import views.html.controlcode.confirmation;

public class ConfirmationController {

  private final FormFactory formFactory;

  private final DestinationCountryController destinationCountryController;

  @Inject
  public ConfirmationController(FormFactory formFactory, DestinationCountryController destinationCountryController) {
    this.formFactory = formFactory;
    this.destinationCountryController = destinationCountryController;
  }

  public Result renderForm(FrontendServiceResult frontendServiceResult) {
    return ok(confirmation.render(formFactory.form(ConfirmationForm.class), frontendServiceResult));
  }

  public Result handleSubmit() {
    Form<ConfirmationForm> form = formFactory.form(ConfirmationForm.class).bindFromRequest();

    String goodsDescribedByRating = form.get().goodsDescribedByRating;
    if ("true".equals(goodsDescribedByRating)) {
      return destinationCountryController.renderForm();
    }

    return badRequest("Invalid form value for goodsDescribedByRating: \"" + goodsDescribedByRating + "\"");
  }

  public static class ConfirmationForm {

    public String goodsDescribedByRating;

  }

}
