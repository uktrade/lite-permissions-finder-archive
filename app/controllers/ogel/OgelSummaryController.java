package controllers.ogel;

import static play.mvc.Results.ok;

import com.google.inject.Inject;
import components.persistence.PermissionsFinderDao;
import components.services.ogels.ogel.OgelServiceClient;
import components.services.ogels.ogel.OgelServiceResult;
import controllers.ErrorController;
import play.data.Form;
import play.data.FormFactory;
import play.data.validation.Constraints.Required;
import play.mvc.Result;
import views.html.ogel.ogelSummary;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;

public class OgelSummaryController {
  private final FormFactory formFactory;

  private final PermissionsFinderDao dao;

  private final OgelServiceClient ogelServiceClient;

  private final ErrorController errorController;

  @Inject
  public OgelSummaryController(FormFactory formFactory,
                               PermissionsFinderDao dao,
                               OgelServiceClient ogelServiceClient,
                               ErrorController errorController) {
    this.formFactory = formFactory;
    this.dao = dao;
    this.ogelServiceClient = ogelServiceClient;
    this.errorController = errorController;
  }

  public CompletionStage<Result> renderForm() {
    return renderWithForm(formFactory.form(OgelSummaryForm.class));
  }

  public CompletionStage<Result> handleSubmit() {
    Form<OgelSummaryForm> form = formFactory.form(OgelSummaryForm.class).bindFromRequest();
    if (form.hasErrors()) {
      return renderWithForm(form);
    }
    return CompletableFuture.completedFuture(ok("REGISTERED FOR OGEL"));
  }

  public CompletionStage<Result> renderWithForm(Form<OgelSummaryForm> form) {
    String ogelId = dao.getOgelId();
    return ogelServiceClient.get(ogelId)
        .thenApply(response -> {
          if (!response.isOk()) {
            return errorController.renderForm("An issue occurred while processing your request, please try again later.");
          }
          OgelServiceResult result = response.getResult();
          return ok(ogelSummary.render(form, result));
        });
  }

  public static class OgelSummaryForm {

    @Required(message = "You must confirm you have read the full licence text before you can register")
    public String fullLicenceRead;

  }

}
