package controllers.ogel;

import static java.util.concurrent.CompletableFuture.completedFuture;
import static play.mvc.Results.badRequest;
import static play.mvc.Results.ok;

import com.google.inject.Inject;
import components.common.client.CountryServiceClient;
import components.common.journey.JourneyManager;
import components.persistence.PermissionsFinderDao;
import components.services.ogels.applicable.ApplicableOgelServiceClient;
import components.services.ogels.conditions.OgelConditionsServiceClient;
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
import java.util.function.Function;
import java.util.stream.Collectors;

public class OgelResultsController {

  private final JourneyManager journeyManager;
  private final FormFactory formFactory;
  private final PermissionsFinderDao permissionsFinderDao;
  private final HttpExecutionContext httpExecutionContext;
  private final ApplicableOgelServiceClient applicableOgelServiceClient;
  private final OgelConditionsServiceClient ogelConditionsServiceClient;
  private final CountryServiceClient countryServiceClient;

  @Inject
  public OgelResultsController(JourneyManager journeyManager,
                               FormFactory formFactory,
                               PermissionsFinderDao permissionsFinderDao,
                               HttpExecutionContext httpExecutionContext,
                               ApplicableOgelServiceClient applicableOgelServiceClient,
                               OgelConditionsServiceClient ogelConditionsServiceClient,
                               CountryServiceClient countryServiceClient) {
    this.journeyManager = journeyManager;
    this.formFactory = formFactory;
    this.permissionsFinderDao = permissionsFinderDao;
    this.httpExecutionContext = httpExecutionContext;
    this.applicableOgelServiceClient = applicableOgelServiceClient;
    this.ogelConditionsServiceClient = ogelConditionsServiceClient;
    this.countryServiceClient = countryServiceClient;
  }

  public CompletionStage<Result> renderForm() {
    return renderWithForm(formFactory.form(OgelResultsForm.class));
  }

  public CompletionStage<Result> renderWithForm(Form<OgelResultsForm> form) {
    String controlCode = permissionsFinderDao.getPhysicalGoodControlCode();
    String sourceCountry = permissionsFinderDao.getSourceCountry();
    List<String> destinationCountries = permissionsFinderDao.getThroughDestinationCountries();

    // Add "primary" country to the first position
    destinationCountries.add(0, permissionsFinderDao.getFinalDestinationCountry());

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
                }, httpExecutionContext.current());
          }
        }, httpExecutionContext.current());
  }

  public CompletionStage<Result> handleSubmit() {
    Form<OgelResultsForm> form = formFactory.form(OgelResultsForm.class).bindFromRequest();
    if (form.hasErrors()) {
      return renderWithForm(form);
    }
    String chosenOgel = form.get().chosenOgel;
    permissionsFinderDao.saveOgelId(chosenOgel);

    String controlCode = permissionsFinderDao.getPhysicalGoodControlCode();
    String sourceCountry = permissionsFinderDao.getSourceCountry();
    List<String> destinationCountries = permissionsFinderDao.getThroughDestinationCountries();

    // Add "primary" country to the first position
    destinationCountries.add(0, permissionsFinderDao.getFinalDestinationCountry());

    List<String> ogelActivities = OgelQuestionsForm.formToActivityTypes(permissionsFinderDao.getOgelQuestionsForm());

    return applicableOgelServiceClient.get(controlCode, sourceCountry, destinationCountries, ogelActivities)
        .thenComposeAsync(applicableOgelResponse -> {
          if (!applicableOgelResponse.isOk()) {
            return completedFuture(badRequest("Invalid response from the applicable OGEL service"));
          }
          if (applicableOgelResponse.getResults().stream().noneMatch(ogel -> chosenOgel.equalsIgnoreCase(ogel.id))) {
            return completedFuture(badRequest("Selected OGEL is not valid with the applicable OGEL service response"));
          }
          return ogelConditionsServiceClient.get(chosenOgel, permissionsFinderDao.getPhysicalGoodControlCode())
              .thenApplyAsync(response -> {
                if (!response.isOk()) {
                  return completedFuture(badRequest("Invalid response from OGEL service"));
                }
                if (response.getResult().isPresent()) {
                  return journeyManager.performTransition(Events.OGEL_RESTRICTIONS_APPLY);
                }
                else {
                  return journeyManager.performTransition(Events.OGEL_SELECTED);
                }
              }, httpExecutionContext.current())
              .thenCompose(Function.identity());
        }, httpExecutionContext.current());
  }

  public static class OgelResultsForm {

    @Required(message = "You must choose from the list of results below")
    public String chosenOgel;

  }

}
