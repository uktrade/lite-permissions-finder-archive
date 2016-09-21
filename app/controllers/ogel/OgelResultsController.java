package controllers.ogel;

import static java.util.concurrent.CompletableFuture.completedFuture;
import static play.mvc.Results.badRequest;
import static play.mvc.Results.ok;

import com.google.inject.Inject;
import components.common.client.CountryServiceClient;
import components.common.journey.JourneyManager;
import components.persistence.PermissionsFinderDao;
import components.services.ogels.applicable.ApplicableOgelServiceClient;
import controllers.ogel.OgelQuestionsController.OgelQuestionsForm;
import journey.Events;
import play.data.Form;
import play.data.FormFactory;
import play.data.validation.Constraints.Required;
import play.libs.concurrent.HttpExecutionContext;
import play.mvc.Result;
import views.html.ogel.ogelResults;

import java.util.Collections;
import java.util.List;
import java.util.concurrent.CompletionStage;
import java.util.stream.Collectors;

public class OgelResultsController {

  private final JourneyManager jm;
  private final FormFactory formFactory;
  private final PermissionsFinderDao permissionsFinderDao;
  private final HttpExecutionContext ec;
  private final ApplicableOgelServiceClient applicableOgelServiceClient;
  private final CountryServiceClient countryServiceClient;

  @Inject
  public OgelResultsController(JourneyManager jm,
                               FormFactory formFactory,
                               PermissionsFinderDao permissionsFinderDao,
                               HttpExecutionContext ec,
                               ApplicableOgelServiceClient applicableOgelServiceClient,
                               CountryServiceClient countryServiceClient) {
    this.jm = jm;
    this.formFactory = formFactory;
    this.permissionsFinderDao = permissionsFinderDao;
    this.ec = ec;
    this.applicableOgelServiceClient = applicableOgelServiceClient;
    this.countryServiceClient = countryServiceClient;
  }

  public CompletionStage<Result> renderForm() {
    return renderWithForm(formFactory.form(OgelResultsForm.class));
  }

  public CompletionStage<Result> renderWithForm(Form<OgelResultsForm> form) {
    String sourceCountry = permissionsFinderDao.getSourceCountry();
    String controlCode = permissionsFinderDao.getPhysicalGoodControlCode();
    List<String> destinationCountries = permissionsFinderDao.getDestinationCountryList();
    List<String> ogelActivities = OgelQuestionsForm.formToActivityTypes(permissionsFinderDao.getOgelQuestionsForm());

    return applicableOgelServiceClient.get(controlCode, sourceCountry, destinationCountries, ogelActivities)
        .thenComposeAsync(r -> {
          if (!r.isOk()) {
            return completedFuture(badRequest("An issue occurred while processing your request, please try again later."));
          }
          else if (!r.getResults().isEmpty()) {
            return completedFuture(ok(ogelResults.render(form, r.getResults(), null, null)));
          }
          else {
            return countryServiceClient.getCountries()
                .thenApplyAsync(countryServiceResponse -> {
                  String physicalGoodControlCode = permissionsFinderDao.getPhysicalGoodControlCode();
                  List<String> countryNames = countryServiceResponse.getCountriesByRef(destinationCountries).stream()
                      .map(country -> "<strong class=\"bold-small\">" + country.getCountryName() + "</strong>")
                      .collect(Collectors.toList());

                  // Creates a string in the form "A, B and C"
                  String destinationCountryNamesHtml = countryNames.size() == 1
                      ? countryNames.get(0)
                      : countryNames.subList(0, countryNames.size() -1).stream()
                        .collect(Collectors.joining(", ", "", " "))
                        .concat("and " + countryNames.get(countryNames.size() -1));

                  return ok(ogelResults.render(form, Collections.emptyList(), physicalGoodControlCode, destinationCountryNamesHtml));
                }, ec.current());
          }
        }, ec.current());
  }

  public CompletionStage<Result> handleSubmit() {
    Form<OgelResultsForm> form = formFactory.form(OgelResultsForm.class).bindFromRequest();
    if (form.hasErrors()) {
      return renderWithForm(form);
    }
    permissionsFinderDao.saveOgelId(form.get().chosenOgel);
    return jm.performTransition(Events.OGEL_SELECTED);
  }

  public static class OgelResultsForm {

    @Required(message = "You must choose from the list of results below")
    public String chosenOgel;

  }

}
