package controllers.licencefinder;

import static java.util.concurrent.CompletableFuture.completedFuture;

import com.google.inject.Inject;
import components.auth.SamlAuthorizer;
import components.common.auth.SpireAuthManager;
import components.common.auth.SpireSAML2Client;
import components.persistence.LicenceFinderDao;
import components.services.LicenceFinderService;
import components.services.OgelService;
import controllers.UserGuardAction;
import models.view.RegisterResultView;
import org.pac4j.play.java.Secure;
import play.data.Form;
import play.data.FormFactory;
import play.data.validation.Constraints.Required;
import play.libs.concurrent.HttpExecutionContext;
import play.mvc.Controller;
import play.mvc.Result;
import play.mvc.With;

import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;

@Secure(clients = SpireSAML2Client.CLIENT_NAME, authorizers = SamlAuthorizer.AUTHORIZER_NAME)
@With(UserGuardAction.class)
public class ResultsController extends Controller {

  private final FormFactory formFactory;
  private final LicenceFinderDao licenceFinderDao;
  private final HttpExecutionContext httpContext;
  private final views.html.licencefinder.results results;
  private final SpireAuthManager authManager;
  private final LicenceFinderService licenceFinderService;
  private final views.html.licencefinder.registerResult registerResult;
  private final OgelService ogelService;
  private final String dashboardUrl;

  public static final String NONE_ABOVE_KEY = "NONE_ABOVE_KEY";
  public static final String IS_ALREADY_REGISTERED_KEY = "IS_ALREADY_REGISTERED_KEY";

  @Inject
  public ResultsController(FormFactory formFactory,
                           HttpExecutionContext httpContext,
                           LicenceFinderDao licenceFinderDao,
                           views.html.licencefinder.results results,
                           SpireAuthManager authManager, LicenceFinderService licenceFinderService, views.html.licencefinder.registerResult registerResult, OgelService ogelService,
                           @com.google.inject.name.Named("dashboardUrl") String dashboardUrl) {
    this.formFactory = formFactory;
    this.httpContext = httpContext;
    this.licenceFinderDao = licenceFinderDao;
    this.results = results;
    this.authManager = authManager;
    this.licenceFinderService = licenceFinderService;
    this.registerResult = registerResult;
    this.ogelService = ogelService;
    this.dashboardUrl = dashboardUrl;
  }

  /**
   * renderResultsForm
   */
  public CompletionStage<Result> renderResultsForm(String sessionId) {
    return renderWithForm(formFactory.form(ResultsForm.class), sessionId);
  }

  /**
   * handleResultsSubmit
   */
  public CompletionStage<Result> handleResultsSubmit(String sessionId) {
    Form<ResultsForm> form = formFactory.form(ResultsForm.class).bindFromRequest();
    if (form.hasErrors()) {
      return renderWithForm(form, sessionId);
    }
    String chosenOgelId = form.get().chosenOgel;
    licenceFinderDao.saveOgelId(sessionId, chosenOgelId);

    // Return No licences available when 'None of the above' chosen
    if (chosenOgelId.equals(NONE_ABOVE_KEY)) {
      return completedFuture(ok(results.render(form, licenceFinderService.getNoResultsView(sessionId), sessionId)));
    }

    // Check if we have a Ogel that the is already registered - return registerResult view
    if(licenceFinderService.isOgelIdAlreadyRegistered(sessionId, chosenOgelId)) {
      return ogelService.get(licenceFinderDao.getOgelId(sessionId)).thenApplyAsync(ogelFullView -> {
        Optional<String> optRef = licenceFinderService.getUserOgelReference(sessionId, chosenOgelId);
        if(optRef.isPresent()) {
          RegisterResultView resultView = new RegisterResultView("You are already registered to use Open general export licence (" + ogelFullView.getName() + ")", optRef.get());
          return ok(registerResult.render(resultView, ogelFullView, dashboardUrl, sessionId));
        } else {
          RegisterResultView resultView = new RegisterResultView("You are already registered to use Open general export licence (" + ogelFullView.getName() + ")");
          return ok(registerResult.render(resultView, ogelFullView, dashboardUrl, sessionId));
        }
      }, httpContext.current());
    }

    return CompletableFuture.completedFuture(redirect(routes.RegisterToUseController.renderRegisterToUseForm(sessionId)));
  }

  /**
   * Private methods
   */
  private CompletionStage<Result> renderWithForm(Form<ResultsForm> form, String sessionId) {
    licenceFinderService.updateUsersOgelIdRefMap(sessionId, authManager.getAuthInfoFromContext().getId()); // update users current OgelId set
    return completedFuture(ok(results.render(form, licenceFinderService.getResultsView(sessionId), sessionId)));
  }

  public static class ResultsForm {
    @Required(message = "You must choose from the list of results below")
    public String chosenOgel;
  }

}

