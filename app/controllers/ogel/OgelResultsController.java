package controllers.ogel;

import static play.mvc.Results.ok;

import com.google.inject.Inject;
import components.persistence.PermissionsFinderDao;
import components.services.ogels.applicable.ApplicableOgelServiceClient;
import components.services.ogels.applicable.ApplicableOgelServiceResult;
import controllers.ErrorController;
import play.data.Form;
import play.data.FormFactory;
import play.data.validation.Constraints.Required;
import play.mvc.Result;
import views.html.ogel.ogelNoResults;
import views.html.ogel.ogelResults;

import java.util.List;
import java.util.concurrent.CompletionStage;

public class OgelResultsController {

  private final FormFactory formFactory;

  private final PermissionsFinderDao dao;

  private final ApplicableOgelServiceClient applicableOgelServiceClient;

  private final ErrorController errorController;

  private final OgelSummaryController ogelSummaryController;

  @Inject
  public OgelResultsController(FormFactory formFactory,
                               PermissionsFinderDao dao,
                               ApplicableOgelServiceClient applicableOgelServiceClient,
                               ErrorController errorController,
                               OgelSummaryController ogelSummaryController
                               ) {
    this.formFactory = formFactory;
    this.dao = dao;
    this.applicableOgelServiceClient = applicableOgelServiceClient;
    this.errorController = errorController;
    this.ogelSummaryController = ogelSummaryController;
  }

  public CompletionStage<Result> renderForm() {
    return renderWithForm(formFactory.form(OgelResultsForm.class));
  }

  public CompletionStage<Result> renderWithForm(Form<OgelResultsForm> form) {
    String sourceCountry = dao.getSourceCountry();
    String controlCode = dao.getPhysicalGoodControlCode();
    List<String> destinationCountries = dao.getDestinationCountryList();
    List<String> ogelActivities = dao.getOgelActivityList();
    return applicableOgelServiceClient.get(controlCode, sourceCountry, destinationCountries, ogelActivities)
        .thenApply(response -> {
          if (!response.isOk()) {
            return errorController.renderForm("An issue occurred while processing your request, please try again later.");
          }
          List<ApplicableOgelServiceResult> results = response.getResults();
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
    dao.saveOgelId(form.get().chosenOgel);
    return ogelSummaryController.renderForm();
  }

  public static class OgelResultsForm {

    @Required(message = "You must choose from the list of results below")
    public String chosenOgel;

  }

}
