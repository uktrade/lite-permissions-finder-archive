package controllers.ogel;

import static play.mvc.Results.ok;

import com.google.inject.Inject;
import components.persistence.PermissionsFinderDao;
import components.services.ogel.OgelServiceClient;
import components.services.ogel.OgelServiceResult;
import controllers.ErrorController;
import play.data.Form;
import play.data.FormFactory;
import play.data.validation.Constraints.Required;
import play.mvc.Result;
import views.html.ogel.ogelResults;
import views.html.ogel.ogelNoResults;

import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;

public class OgelResultsController {

  private final FormFactory formFactory;

  private final PermissionsFinderDao dao;

  private final OgelServiceClient ogelServiceClient;

  private final ErrorController errorController;

  @Inject
  public OgelResultsController(FormFactory formFactory,
                               PermissionsFinderDao dao,
                               OgelServiceClient ogelServiceClient,
                               ErrorController errorController
                               ) {
    this.formFactory = formFactory;
    this.dao = dao;
    this.ogelServiceClient = ogelServiceClient;
    this.errorController = errorController;
  }

  public CompletionStage<Result> renderForm() {
    return renderWithForm(formFactory.form(OgelResultsForm.class));
  }

  public CompletionStage<Result> renderWithForm(Form<OgelResultsForm> form) {
    String sourceCountry = dao.getSourceCountry();
    String controlCode = dao.getPhysicalGoodControlCode();
    List<String> destinationCountries = dao.getDestinationCountryList();
    List<String> ogelActivities = dao.getOgelActivityList();
    return ogelServiceClient.get(controlCode, sourceCountry, destinationCountries, ogelActivities)
        .thenApply(response -> {
          if (!response.isOk()) {
            return errorController.renderForm("An issue occurred while processing your request, please try again later.");
          }
          List<OgelServiceResult> results = response.getResults();
          if (results.isEmpty()) {
            return ok(ogelNoResults.render());
          }
          return ok(ogelResults.render(form, results));
        });
  }

  public CompletionStage<Result> handleSubmit() {
    Form<OgelResultsForm> form = formFactory.form(OgelResultsForm.class).bindFromRequest();

    if (form.hasErrors()) {
      renderWithForm(form);
    }

    return CompletableFuture.completedFuture(ok("Selected OGEL: " + form.get().chosenOgel));
  }

  public static class OgelResultsForm {

    @Required(message = "You must choose from the list of results below")
    public String chosenOgel;

  }

}
