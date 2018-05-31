package controllers.ogel;

import com.google.inject.Inject;
import com.google.inject.name.Named;
import components.auth.SamlAuthorizer;
import components.common.auth.SpireSAML2Client;
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
import org.apache.commons.lang3.StringUtils;
import org.pac4j.play.java.Secure;
import play.data.Form;
import play.data.FormFactory;
import play.data.validation.Constraints.Required;
import play.libs.concurrent.HttpExecutionContext;
import play.mvc.Result;
import utils.CountryUtils;

import java.util.List;
import java.util.Optional;
import java.util.concurrent.CompletionStage;

@Secure(clients = SpireSAML2Client.CLIENT_NAME, authorizers = SamlAuthorizer.AUTHORIZER_NAME)
public class OgelResultsController {

  private final JourneyManager journeyManager;
  private final FormFactory formFactory;
  private final PermissionsFinderDao dao;
  private final HttpExecutionContext httpContext;
  private final ApplicableOgelServiceClient applicableClient;
  private final OgelConditionsServiceClient conditionsClient;
  private final FrontendServiceClient frontendClient;
  private final CountryProvider countryProvider;

  public static final String NONE_ABOVE_KEY = "NONE_ABOVE_KEY";

  @Inject
  public OgelResultsController(JourneyManager journeyManager,
                               FormFactory formFactory,
                               PermissionsFinderDao dao,
                               HttpExecutionContext httpContext,
                               ApplicableOgelServiceClient applicableClient,
                               OgelConditionsServiceClient conditionsClient,
                               FrontendServiceClient frontendClient,
                               @Named("countryProviderExport") CountryProvider countryProvider) {
    this.journeyManager = journeyManager;
    this.formFactory = formFactory;
    this.dao = dao;
    this.httpContext = httpContext;
    this.applicableClient = applicableClient;
    this.conditionsClient = conditionsClient;
    this.frontendClient = frontendClient;
    this.countryProvider = countryProvider;
  }

  /**
   * renderForm
   */
  public CompletionStage<Result> renderForm() {
    return renderWithForm(formFactory.form(OgelResultsForm.class));
  }

  /**
   * handleSubmit
   */
  public CompletionStage<Result> handleSubmit() {
    Form<OgelResultsForm> form = formFactory.form(OgelResultsForm.class).bindFromRequest();
    if (form.hasErrors()) {
      return renderWithForm(form);
    }
    String chosenOgel = form.get().chosenOgel;

    dao.saveOgelId(chosenOgel);

    String controlCode = dao.getControlCodeForRegistration();
    String sourceCountry = dao.getSourceCountry();
    String destinationCountry = dao.getFinalDestinationCountry();

    List<String> destinationCountries = CountryUtils.getDestinationCountries(dao.getFinalDestinationCountry(), dao.getThroughDestinationCountries());
    Optional<OgelQuestionsForm> ogelQuestionsFormOptional = dao.getOgelQuestionsForm();
    List<String> ogelActivities = OgelQuestionsForm.formToActivityTypes(ogelQuestionsFormOptional);

    String destinationCountryName = countryProvider.getCountry(destinationCountry).getCountryName();

    CompletionStage<Void> checkOgelStage = applicableClient.get(controlCode, sourceCountry, destinationCountries, ogelActivities)
        .thenAcceptAsync(result -> {
          if (!result.stream().filter(ogelView -> StringUtils.equals(ogelView.getId(), chosenOgel)).findFirst().isPresent()) {
            throw new FormStateException(String.format("Chosen OGEL %s is not valid according to the applicable OGEL service response", chosenOgel));
          }
        }, httpContext.current());

    // All selections now go to 'register to use' page
    // So we can just render OGEL_SELECTED ('registerToUse') TODO check this is correct

    return journeyManager.performTransition(Events.OGEL_SELECTED);
    /*
    // Combines with the stage above, allowing any exceptions to propagate
    return checkOgelStage
        .thenCombineAsync(conditionsClient.get(chosenOgel, dao.getControlCodeForRegistration()),
            (empty, conditionsResult) -> {
              if (!conditionsResult.isEmpty) {
                //return journeyManager.performTransition(Events.OGEL_CONDITIONS_APPLY);

                return journeyManager.performTransition(Events.OGEL_SELECTED);
              } else {
                return journeyManager.performTransition(Events.OGEL_SELECTED);
              }
            }, httpContext.current()).thenCompose(Function.identity());
            */
  }

  private CompletionStage<Result> renderWithForm(Form<OgelResultsForm> form) {

    String controlCode = dao.getControlCodeForRegistration();
    String destinationCountry = dao.getFinalDestinationCountry();
    String destinationCountryName = countryProvider.getCountry(destinationCountry).getCountryName();
    List<String> destinationCountries = CountryUtils.getDestinationCountries(destinationCountry, dao.getThroughDestinationCountries());
    Optional<OgelQuestionsForm> ogelQuestionsFormOptional = dao.getOgelQuestionsForm();
    List<String> ogelActivities = OgelQuestionsForm.formToActivityTypes(ogelQuestionsFormOptional);
    CompletionStage<FrontendServiceResult> frontendServiceStage = frontendClient.get(controlCode);
    return journeyManager.performTransition(Events.OGEL_SELECTED);
   /* return applicableClient.get(controlCode, dao.getSourceCountry(), destinationCountries, ogelActivities)
        .thenCombineAsync(frontendServiceStage, (applicableOgelView, frontendServiceResult) -> {
          if (!applicableOgelView.isEmpty()) {
            OgelResultsDisplay display = new OgelResultsDisplay(applicableOgelView, frontendServiceResult.getFrontendControlCode(),
                null, controlCode, destinationCountryName);
            return ok(ogelResults.render(form, display));
          } else {
            List<String> countryNames = CountryUtils.getFilteredCountries(CountryUtils.getSortedCountries(countryProvider.getCountries()), destinationCountries).stream()
                .map(CountryView::getCountryName)
                .collect(Collectors.toList());
            OgelResultsDisplay display = new OgelResultsDisplay(applicableOgelView, frontendServiceResult.getFrontendControlCode(),
                countryNames, controlCode, destinationCountryName);

            return ok(ogelResults.render(form, display));
          }
        }, httpContext.current());*/
  }

  public static class OgelResultsForm {
    @Required(message = "You must choose from the list of results below")
    public String chosenOgel;
  }

}
