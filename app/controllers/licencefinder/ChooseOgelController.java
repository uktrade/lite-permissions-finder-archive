package controllers.licencefinder;

import static java.util.concurrent.CompletableFuture.completedFuture;

import com.google.inject.Inject;
import com.google.inject.name.Named;
import components.client.ApplicableOgelServiceClient;
import components.client.ApplicableOgelServiceClientImpl;
import components.client.OgelServiceClient;
import components.client.PermissionsServiceClient;
import components.common.auth.SamlAuthorizer;
import components.common.auth.SpireAuthManager;
import components.common.auth.SpireSAML2Client;
import components.common.cache.CountryProvider;
import components.persistence.LicenceFinderDao;
import controllers.guard.LicenceFinderAwaitGuardAction;
import controllers.guard.LicenceFinderUserGuardAction;
import exceptions.ServiceException;
import exceptions.UnknownParameterException;
import models.OgelActivityType;
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
import uk.gov.bis.lite.permissions.api.view.OgelRegistrationView;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.CompletionStage;
import java.util.concurrent.ExecutionException;
import java.util.stream.Collectors;

@Secure(clients = SpireSAML2Client.CLIENT_NAME, authorizers = SamlAuthorizer.AUTHORIZER_NAME)
@With({LicenceFinderUserGuardAction.class, LicenceFinderAwaitGuardAction.class})
public class ChooseOgelController extends Controller {

  public static final String NONE_ABOVE_KEY = "NONE_ABOVE_KEY";

  private final FormFactory formFactory;
  private final LicenceFinderDao licenceFinderDao;
  private final HttpExecutionContext httpContext;
  private final views.html.licencefinder.ogelResults ogelResults;
  private final views.html.licencefinder.noOgelResults noOgelResults;
  private final SpireAuthManager authManager;
  private final views.html.licencefinder.registerResult registerResult;
  private final OgelServiceClient ogelServiceClient;
  private final CountryProvider countryProvider;
  private final ApplicableOgelServiceClient applicableOgelServiceClient;
  private final PermissionsServiceClient permissionsServiceClient;
  private final String dashboardUrl;

  @Inject
  public ChooseOgelController(FormFactory formFactory, HttpExecutionContext httpContext,
                              LicenceFinderDao licenceFinderDao, views.html.licencefinder.ogelResults ogelResults,
                              views.html.licencefinder.noOgelResults noOgelResults, SpireAuthManager authManager,
                              views.html.licencefinder.registerResult registerResult,
                              OgelServiceClient ogelServiceClient,
                              @javax.inject.Named("countryProviderExport") CountryProvider countryProvider,
                              ApplicableOgelServiceClientImpl applicableOgelServiceClient,
                              PermissionsServiceClient permissionsServiceClient,
                              @Named("dashboardUrl") String dashboardUrl) {
    this.formFactory = formFactory;
    this.httpContext = httpContext;
    this.licenceFinderDao = licenceFinderDao;
    this.ogelResults = ogelResults;
    this.noOgelResults = noOgelResults;
    this.authManager = authManager;
    this.registerResult = registerResult;
    this.ogelServiceClient = ogelServiceClient;
    this.countryProvider = countryProvider;
    this.applicableOgelServiceClient = applicableOgelServiceClient;
    this.permissionsServiceClient = permissionsServiceClient;
    this.dashboardUrl = dashboardUrl;
  }

  public CompletionStage<Result> renderResultsForm(String sessionId) {
    String userId = authManager.getAuthInfoFromContext().getId();
    return renderWithForm(formFactory.form(ResultsForm.class), sessionId, userId);
  }

  public CompletionStage<Result> handleResultsSubmit(String sessionId) {
    String userId = authManager.getAuthInfoFromContext().getId();
    Form<ResultsForm> form = formFactory.form(ResultsForm.class).bindFromRequest();
    if (form.hasErrors()) {
      return renderWithForm(form, sessionId, userId);
    } else {
      String chosenOgelId = form.get().chosenOgel;
      if (NONE_ABOVE_KEY.equals(chosenOgelId)) {
        // Return No licences available when 'None of the above' chosen
        licenceFinderDao.saveOgelId(sessionId, chosenOgelId);
        return completedFuture(ok(noOgelResults.render()));
      } else if (!isValidOgelId(sessionId, chosenOgelId)) {
        return renderWithForm(form, sessionId, userId);
      } else {
        licenceFinderDao.saveOgelId(sessionId, chosenOgelId);
        String reference = getUserOgelIdReferenceMap(sessionId, userId).get(chosenOgelId);
        if (reference != null) {
          // Check if we have a Ogel that is already registered - return registerResult view
          return ogelServiceClient.getById(chosenOgelId).thenApplyAsync(ogelFullView -> {
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
  }

  private CompletionStage<Result> renderWithForm(Form<ResultsForm> form, String sessionId, String userId) {
    List<ApplicableOgelView> applicableOgelViews = getApplicableOgelViews(sessionId);
    Set<String> existingOgels = getUserOgelIdReferenceMap(sessionId, userId).keySet();
    List<OgelView> ogelViews = getOgelViews(applicableOgelViews, existingOgels);

    if (ogelViews.isEmpty()) {
      return completedFuture(ok(noOgelResults.render()));
    } else {
      // Sort Ogels alphabetically by name
      ogelViews.sort(Comparator.comparing(OgelView::getName));

      String controlCode = licenceFinderDao.getControlCode(sessionId)
          .orElseThrow(UnknownParameterException::unknownLicenceFinderOrder);
      String destinationCountry = licenceFinderDao.getDestinationCountry(sessionId)
          .orElseThrow(UnknownParameterException::unknownLicenceFinderOrder);
      String destinationCountryName = countryProvider.getCountry(destinationCountry).getCountryName();

      return completedFuture(ok(ogelResults.render(form, sessionId, controlCode, destinationCountryName, ogelViews)));
    }
  }

  private boolean isValidOgelId(String sessionId, String changeOgelId) {
    return getApplicableOgelViews(sessionId).stream()
        .anyMatch(applicableOgelView -> applicableOgelView.getId().equals(changeOgelId));
  }

  private List<ApplicableOgelView> getApplicableOgelViews(String sessionId) {
    String controlCode = licenceFinderDao.getControlCode(sessionId)
        .orElseThrow(UnknownParameterException::unknownLicenceFinderOrder);
    String sourceCountry = licenceFinderDao.getSourceCountry(sessionId)
        .orElseThrow(UnknownParameterException::unknownLicenceFinderOrder);

    List<String> destinationCountries = new ArrayList<>();
    String destinationCountry = licenceFinderDao.getDestinationCountry(sessionId)
        .orElseThrow(UnknownParameterException::unknownLicenceFinderOrder);
    destinationCountries.add(destinationCountry);
    licenceFinderDao.getFirstConsigneeCountry(sessionId).ifPresent(destinationCountries::add);

    QuestionsController.QuestionsForm questionsForm = licenceFinderDao.getQuestionsForm(sessionId)
        .orElseThrow(UnknownParameterException::unknownLicenceFinderOrder);

    List<String> activities = getActivityTypes(questionsForm);
    boolean showHistoricOgel = questionsForm.beforeOrLess;

    CompletionStage<List<ApplicableOgelView>> stage = applicableOgelServiceClient.get(controlCode, sourceCountry, destinationCountries, activities, showHistoricOgel);
    try {
      return stage.toCompletableFuture().get();
    } catch (Exception exception) {
      throw new ServiceException("Unable to get applicable views", exception);
    }
  }

  private List<OgelView> getOgelViews(List<ApplicableOgelView> applicableViews, Set<String> existingOgels) {
    return applicableViews.stream()
        .map(applicableOgelView -> {
          boolean alreadyRegistered = existingOgels.contains(applicableOgelView.getId());

          OgelView view = new OgelView();
          view.setId(applicableOgelView.getId());
          view.setName(applicableOgelView.getName());
          view.setUsageSummary(applicableOgelView.getUsageSummary());
          view.setAlreadyRegistered(alreadyRegistered);
          return view;
        }).collect(Collectors.toList());
  }

  private List<String> getActivityTypes(QuestionsController.QuestionsForm questionsForm) {
    Set<OgelActivityType> set = EnumSet.of(OgelActivityType.DU_ANY, OgelActivityType.MIL_ANY, OgelActivityType.MIL_GOV);
    if (questionsForm.forRepair) {
      set.add(OgelActivityType.REPAIR);
    }
    if (questionsForm.forExhibition) {
      set.add(OgelActivityType.EXHIBITION);
    }
    return set.stream().map(OgelActivityType::toString).collect(Collectors.toList());
  }

  private Map<String, String> getUserOgelIdReferenceMap(String sessionId, String userId) {
    Optional<Map<String, String>> userOgelIdReferenceMapOptional = licenceFinderDao.getUserOgelIdReferenceMap(sessionId);
    if (userOgelIdReferenceMapOptional.isPresent()) {
      return userOgelIdReferenceMapOptional.get();
    } else {
      Map<String, String> userOgelIdReferenceMap = createUserOgelIdReferenceMap(userId);
      licenceFinderDao.saveUserOgelIdRefMap(sessionId, userOgelIdReferenceMap);
      return userOgelIdReferenceMap;
    }
  }

  private Map<String, String> createUserOgelIdReferenceMap(String userId) {
    try {
      Map<String, String> ogelIdReferenceMap = new HashMap<>();
      List<OgelRegistrationView> views = permissionsServiceClient.getOgelRegistrations(userId).toCompletableFuture().get();
      for (OgelRegistrationView view : views) {
        ogelIdReferenceMap.put(view.getOgelType(), view.getRegistrationReference());
      }
      return ogelIdReferenceMap;
    } catch (InterruptedException | ExecutionException exception) {
      throw new ServiceException("Unable to get userOgelIdReferenceMap for userId " + userId, exception);
    }
  }

  public static class ResultsForm {
    @Required(message = "You must choose from the list of results below")
    public String chosenOgel;
  }

}

