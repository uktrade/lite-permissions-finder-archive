package controllers.ogel;

import static java.util.concurrent.CompletableFuture.completedFuture;
import static play.mvc.Results.ok;

import com.google.inject.Inject;
import components.common.client.CountryServiceClient;
import components.common.journey.JourneyManager;
import components.persistence.PermissionsFinderDao;
import components.services.ogels.applicable.ApplicableOgelServiceClient;
import components.services.ogels.conditions.OgelConditionsServiceClient;
import controllers.ogel.OgelQuestionsController.OgelQuestionsForm;
import exceptions.FormStateException;
import journey.Events;
import play.data.Form;
import play.data.FormFactory;
import play.data.validation.Constraints.Required;
import play.libs.concurrent.HttpExecutionContext;
import play.mvc.Result;
import utils.CountryUtils;
import views.html.ogel.ogelResults;

import java.util.Collections;
import java.util.List;
import java.util.Optional;
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

    List<String> destinationCountries = CountryUtils.getDestinationCountries(permissionsFinderDao.getFinalDestinationCountry(),
        permissionsFinderDao.getThroughDestinationCountries());

    Optional<OgelQuestionsForm> ogelQuestionsFormOptional = permissionsFinderDao.getOgelQuestionsForm();
    List<String> ogelActivities = OgelQuestionsForm.formToActivityTypes(ogelQuestionsFormOptional);
    boolean isGoodHistoric =  OgelQuestionsForm.isGoodHistoric(ogelQuestionsFormOptional);

    return applicableOgelServiceClient.get(controlCode, sourceCountry, destinationCountries, ogelActivities, isGoodHistoric)
        .thenComposeAsync(result -> {
          if (!result.results.isEmpty()) {
            return completedFuture(ok(ogelResults.render(form, result.results, null, null)));
          }
          else {
            return countryServiceClient.getCountries()
                .thenApplyAsync(countryServiceResponse -> {

                  List<String> countryNames = countryServiceResponse.getCountriesByRef(destinationCountries).stream()
                      .map(country -> country.getCountryName())
                      .collect(Collectors.toList());

                  // Creates a string in the form "A, B and C"
                  String destinationCountryNamesHtml = countryNames.size() == 1
                      ? countryNames.get(0)
                      : countryNames.subList(0, countryNames.size() -1).stream()
                        .collect(Collectors.joining(", ", "", " "))
                        .concat("and " + countryNames.get(countryNames.size() -1));

                  return ok(ogelResults.render(form, Collections.emptyList(), controlCode, destinationCountryNamesHtml));
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
    List<String> destinationCountries = CountryUtils.getDestinationCountries(
        permissionsFinderDao.getFinalDestinationCountry(), permissionsFinderDao.getThroughDestinationCountries());
    Optional<OgelQuestionsForm> ogelQuestionsFormOptional = permissionsFinderDao.getOgelQuestionsForm();
    List<String> ogelActivities = OgelQuestionsForm.formToActivityTypes(ogelQuestionsFormOptional);
    boolean isGoodHistoric =  OgelQuestionsForm.isGoodHistoric(ogelQuestionsFormOptional);

    CompletionStage<Void> checkOgelStage = applicableOgelServiceClient
        .get(controlCode, sourceCountry, destinationCountries, ogelActivities, isGoodHistoric)
        .thenAcceptAsync(result -> {
          if (!result.findResultById(chosenOgel).isPresent()) {
            throw new FormStateException(String.format("Chosen OGEL %s is not valid according to the applicable OGEL service response", chosenOgel));
          }
        }, httpExecutionContext.current());

    // Combines with the stage above, allowing any exceptions to propagate
    return checkOgelStage
        .thenCombine(ogelConditionsServiceClient.get(chosenOgel, permissionsFinderDao.getPhysicalGoodControlCode()),
            (empty, conditionsResult) -> {
              if (!conditionsResult.isEmpty) {
                return journeyManager.performTransition(Events.OGEL_CONDITIONS_APPLY);
              }
              else {
                return journeyManager.performTransition(Events.OGEL_SELECTED);
              }
            }).thenCompose(Function.identity());

  }

  public static class OgelResultsForm {

    @Required(message = "You must choose from the list of results below")
    public String chosenOgel;

  }

}
