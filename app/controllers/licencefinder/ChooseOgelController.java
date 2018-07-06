package controllers.licencefinder;

import static java.util.concurrent.CompletableFuture.completedFuture;

import com.google.inject.Inject;
import com.google.inject.name.Named;
import components.common.auth.SamlAuthorizer;
import components.common.auth.SpireAuthManager;
import components.common.auth.SpireSAML2Client;
import components.common.cache.CountryProvider;
import components.persistence.LicenceFinderDao;
import components.services.LicenceFinderService;
import components.services.OgelService;
import controllers.guard.LicenceFinderAwaitGuardAction;
import controllers.guard.LicenceFinderUserGuardAction;
import exceptions.UnknownParameterException;
import models.view.RegisterResultView;
import models.view.licencefinder.OgelView;
import org.pac4j.play.java.Secure;
import play.data.Form;
import play.data.FormFactory;
import play.data.validation.Constraints.Required;
import play.libs.concurrent.HttpExecutionContext;
import play.mvc.Controller;
import play.mvc.Result;
import play.mvc.With;
import uk.gov.bis.lite.ogel.api.view.ApplicableOgelView;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.concurrent.CompletionStage;

@Secure(clients = SpireSAML2Client.CLIENT_NAME, authorizers = SamlAuthorizer.AUTHORIZER_NAME)
@With({LicenceFinderUserGuardAction.class, LicenceFinderAwaitGuardAction.class})
public class ChooseOgelController extends Controller {

  private final FormFactory formFactory;
  private final LicenceFinderDao licenceFinderDao;
  private final HttpExecutionContext httpContext;
  private final views.html.licencefinder.results results;
  private final SpireAuthManager authManager;
  private final LicenceFinderService licenceFinderService;
  private final views.html.licencefinder.registerResult registerResult;
  private final OgelService ogelService;
  private final CountryProvider countryProvider;
  private final String dashboardUrl;

  public static final String NONE_ABOVE_KEY = "NONE_ABOVE_KEY";

  @Inject
  public ChooseOgelController(FormFactory formFactory,
                              HttpExecutionContext httpContext,
                              LicenceFinderDao licenceFinderDao,
                              views.html.licencefinder.results results,
                              SpireAuthManager authManager, LicenceFinderService licenceFinderService,
                              views.html.licencefinder.registerResult registerResult, OgelService ogelService,
                              @javax.inject.Named("countryProviderExport") CountryProvider countryProvider,
                              @Named("dashboardUrl") String dashboardUrl) {
    this.formFactory = formFactory;
    this.httpContext = httpContext;
    this.licenceFinderDao = licenceFinderDao;
    this.results = results;
    this.authManager = authManager;
    this.licenceFinderService = licenceFinderService;
    this.registerResult = registerResult;
    this.ogelService = ogelService;
    this.countryProvider = countryProvider;
    this.dashboardUrl = dashboardUrl;
  }

  public CompletionStage<Result> renderResultsForm(String sessionId) {
    if (licenceFinderService.canAccessChooseOgelController(sessionId)) {
      return renderWithForm(formFactory.form(ResultsForm.class), sessionId);
    } else {
      throw UnknownParameterException.unknownOgelRegistrationOrder();
    }
  }

  public CompletionStage<Result> handleResultsSubmit(String sessionId) {
    if (licenceFinderService.canAccessChooseOgelController(sessionId)) {
      Form<ResultsForm> form = formFactory.form(ResultsForm.class).bindFromRequest();
      if (form.hasErrors()) {
        return renderWithForm(form, sessionId);
      } else {
        String chosenOgelId = form.get().chosenOgel;
        if (NONE_ABOVE_KEY.equals(chosenOgelId)) {
          // Return No licences available when 'None of the above' chosen
          licenceFinderDao.saveOgelId(sessionId, chosenOgelId);
          String title = "No open licences available";
          // TODO additional endpoint / template
          return completedFuture(ok(results.render(form, sessionId, title, new ArrayList<>())));
        } else if (!isValidOgelId(sessionId, chosenOgelId)) {
          return renderWithForm(form, sessionId);
        } else {
          licenceFinderDao.saveOgelId(sessionId, chosenOgelId);
          String reference = licenceFinderService.getUserOgelIdReferenceMap(sessionId).get(chosenOgelId);
          if (reference != null) {
            // Check if we have a Ogel that is already registered - return registerResult view
            return ogelService.getById(chosenOgelId).thenApplyAsync(ogelFullView -> {
              String title = "You are already registered to use Open general export licence " + ogelFullView.getName();
              RegisterResultView resultView = new RegisterResultView(title, reference);
              // TODO additional endpoint / template
              return ok(registerResult.render(resultView, ogelFullView, dashboardUrl));
            }, httpContext.current());
          } else {
            return completedFuture(redirect(routes.RegisterToUseController.renderRegisterToUseForm(sessionId)));
          }
        }
      }
    } else {
      throw UnknownParameterException.unknownOgelRegistrationOrder();
    }
  }

  private CompletionStage<Result> renderWithForm(Form<ResultsForm> form, String sessionId) {
    licenceFinderService.updateUserOgelIdReferenceMap(sessionId, authManager.getAuthInfoFromContext().getId());

    List<ApplicableOgelView> applicableOgelViews = getApplicableOgelViews(sessionId);
    Set<String> existingOgels = licenceFinderService.getUserOgelIdReferenceMap(sessionId).keySet();
    List<OgelView> ogelViews = licenceFinderService.getOgelViews(applicableOgelViews, existingOgels);

    String controlCode = licenceFinderDao.getControlCode(sessionId)
        .orElseThrow(UnknownParameterException::unknownOgelRegistrationOrder);
    String destinationCountry = licenceFinderDao.getDestinationCountry(sessionId)
        .orElseThrow(UnknownParameterException::unknownOgelRegistrationOrder);
    String destinationCountryName = countryProvider.getCountry(destinationCountry).getCountryName();
    String title = String.format("Open licences available for exporting goods described in control list entry %s to %s",
        controlCode, destinationCountryName);

    return completedFuture(ok(results.render(form, sessionId, title, ogelViews)));
  }

  private boolean isValidOgelId(String sessionId, String changeOgelId) {
    return getApplicableOgelViews(sessionId).stream()
        .anyMatch(applicableOgelView -> applicableOgelView.getId().equals(changeOgelId));
  }

  private List<ApplicableOgelView> getApplicableOgelViews(String sessionId) {
    String controlCode = licenceFinderDao.getControlCode(sessionId)
        .orElseThrow(UnknownParameterException::unknownOgelRegistrationOrder);
    String sourceCountry = licenceFinderDao.getSourceCountry(sessionId)
        .orElseThrow(UnknownParameterException::unknownOgelRegistrationOrder);

    List<String> countries = new ArrayList<>();
    String destinationCountry = licenceFinderDao.getDestinationCountry(sessionId)
        .orElseThrow(UnknownParameterException::unknownOgelRegistrationOrder);
    countries.add(destinationCountry);
    licenceFinderDao.getFirstConsigneeCountry(sessionId).ifPresent(countries::add);

    QuestionsController.QuestionsForm questionsForm = licenceFinderDao.getQuestionsForm(sessionId)
        .orElseThrow(UnknownParameterException::unknownOgelRegistrationOrder);

    return licenceFinderService.getApplicableOgelViews(controlCode, sourceCountry, countries, questionsForm);
  }

  public static class ResultsForm {
    @Required(message = "You must choose from the list of results below")
    public String chosenOgel;
  }

}

