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
import models.ogel.OgelResultsDisplay;
import org.apache.commons.lang3.StringUtils;
import play.data.Form;
import play.data.FormFactory;
import play.data.validation.Constraints.Required;
import play.libs.concurrent.HttpExecutionContext;
import play.mvc.Result;
import uk.gov.bis.lite.countryservice.api.CountryView;
import utils.CountryUtils;
import views.html.ogel.ogelResults;

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

    CompletionStage<FrontendServiceResult> frontendServiceStage = frontendServiceClient.get(controlCode);

    return applicableOgelServiceClient.get(controlCode, sourceCountry, destinationCountries, ogelActivities)
        .thenCombineAsync(frontendServiceStage, (applicableOgelView, frontendServiceResult) -> {
          if (!applicableOgelView.isEmpty()) {
            OgelResultsDisplay display = new OgelResultsDisplay(applicableOgelView, frontendServiceResult.getFrontendControlCode(), null);
            return ok(ogelResults.render(form, display));
          } else {
            List<CountryView> countries = CountryUtils.getSortedCountries(countryProviderExport.getCountries());

            List<String> countryNames = CountryUtils.getFilteredCountries(countries, destinationCountries).stream()
                .map(CountryView::getCountryName)
                .collect(Collectors.toList());

            OgelResultsDisplay display = new OgelResultsDisplay(applicableOgelView, frontendServiceResult.getFrontendControlCode(), countryNames);

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

    CompletionStage<Void> checkOgelStage = applicableOgelServiceClient
        .get(controlCode, sourceCountry, destinationCountries, ogelActivities)
        .thenAcceptAsync(result -> {
          if (!result.stream().filter(ogelView -> StringUtils.equals(ogelView.getId(), chosenOgel)).findFirst().isPresent()) {
            throw new FormStateException(String.format("Chosen OGEL %s is not valid according to the applicable OGEL service response", chosenOgel));
          }
        }, httpExecutionContext.current());

    // Combines with the stage above, allowing any exceptions to propagate
    return checkOgelStage
        .thenCombineAsync(ogelConditionsServiceClient.get(chosenOgel, permissionsFinderDao.getControlCodeForRegistration()),
            (empty, conditionsResult) -> {
              if (!conditionsResult.isEmpty) {
                return journeyManager.performTransition(Events.OGEL_CONDITIONS_APPLY);
              } else {
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
