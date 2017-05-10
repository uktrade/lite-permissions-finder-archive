package controllers.ogel;

import static play.mvc.Results.ok;

import com.google.inject.Inject;
import com.google.inject.name.Named;
import components.common.cache.CountryProvider;
import components.common.journey.JourneyManager;
import components.persistence.PermissionsFinderDao;
import components.services.controlcode.frontend.FrontendServiceClient;
import components.services.controlcode.frontend.FrontendServiceResult;
import components.services.ogels.applicable.ApplicableOgelServiceClient;
import components.services.ogels.conditions.OgelConditionsServiceClient;
import controllers.ogel.OgelQuestionsController.OgelQuestionsForm;
import exceptions.FormStateException;
import journey.Events;
import models.common.Country;
import models.ogel.OgelResultsDisplay;
import play.data.Form;
import play.data.FormFactory;
import play.data.validation.Constraints.Required;
import play.libs.concurrent.HttpExecutionContext;
import play.mvc.Result;
import utils.CountryUtils;
import views.html.ogel.ogelResults;

import java.util.ArrayList;
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
  private final FrontendServiceClient frontendServiceClient;
  private final CountryProvider countryProviderExport;

  @Inject
  public OgelResultsController(JourneyManager journeyManager,
                               FormFactory formFactory,
                               PermissionsFinderDao permissionsFinderDao,
                               HttpExecutionContext httpExecutionContext,
                               ApplicableOgelServiceClient applicableOgelServiceClient,
                               OgelConditionsServiceClient ogelConditionsServiceClient,
                               FrontendServiceClient frontendServiceClient,
                               @Named("countryProviderExport") CountryProvider countryProviderExport) {
    this.journeyManager = journeyManager;
    this.formFactory = formFactory;
    this.permissionsFinderDao = permissionsFinderDao;
    this.httpExecutionContext = httpExecutionContext;
    this.applicableOgelServiceClient = applicableOgelServiceClient;
    this.ogelConditionsServiceClient = ogelConditionsServiceClient;
    this.frontendServiceClient = frontendServiceClient;
    this.countryProviderExport = countryProviderExport;
  }

  public CompletionStage<Result> renderForm() {
    return renderWithForm(formFactory.form(OgelResultsForm.class));
  }

  public CompletionStage<Result> renderWithForm(Form<OgelResultsForm> form) {
    String controlCode = permissionsFinderDao.getControlCodeForRegistration();

    String sourceCountry = permissionsFinderDao.getSourceCountry();

    List<String> destinationCountries = CountryUtils.getDestinationCountries(permissionsFinderDao.getFinalDestinationCountry(),
        permissionsFinderDao.getThroughDestinationCountries());

    Optional<OgelQuestionsForm> ogelQuestionsFormOptional = permissionsFinderDao.getOgelQuestionsForm();

    List<String> ogelActivities = OgelQuestionsForm.formToActivityTypes(ogelQuestionsFormOptional);

    boolean isGoodHistoric =  OgelQuestionsForm.isGoodHistoric(ogelQuestionsFormOptional);

    CompletionStage<FrontendServiceResult> frontendServiceStage = frontendServiceClient.get(controlCode);

    return applicableOgelServiceClient.get(controlCode, sourceCountry, destinationCountries, ogelActivities, isGoodHistoric)
        .thenCombineAsync(frontendServiceStage, (applicableOgelServiceResult, frontendServiceResult) -> {
          if (!applicableOgelServiceResult.results.isEmpty()) {
            OgelResultsDisplay display = new OgelResultsDisplay(applicableOgelServiceResult.results, frontendServiceResult.getFrontendControlCode(), null);
            return ok(ogelResults.render(form, display));
          }
          else {
            List<Country> countries = new ArrayList<>(countryProviderExport.getCountriesOrderedByName());

            List<String> countryNames = CountryUtils.getFilteredCountries(countries, destinationCountries).stream()
                .map(Country::getCountryName)
                .collect(Collectors.toList());

            OgelResultsDisplay display = new OgelResultsDisplay(applicableOgelServiceResult.results, frontendServiceResult.getFrontendControlCode(), countryNames);

            return ok(ogelResults.render(form, display));
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

    String controlCode = permissionsFinderDao.getControlCodeForRegistration();
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
        .thenCombineAsync(ogelConditionsServiceClient.get(chosenOgel, permissionsFinderDao.getControlCodeForRegistration()),
            (empty, conditionsResult) -> {
              if (!conditionsResult.isEmpty) {
                return journeyManager.performTransition(Events.OGEL_CONDITIONS_APPLY);
              }
              else {
                return journeyManager.performTransition(Events.OGEL_SELECTED);
              }
            }, httpExecutionContext.current())
        .thenCompose(Function.identity());

  }

  public static class OgelResultsForm {

    @Required(message = "You must choose from the list of results below")
    public String chosenOgel;

  }

}
