package controllers.licencefinder;

import static java.util.concurrent.CompletableFuture.completedFuture;

import com.google.inject.Inject;
import components.auth.SamlAuthorizer;
import components.common.auth.SpireSAML2Client;
import components.persistence.LicenceFinderDao;
import components.services.LicenceFinderService;
import components.services.OgelService;
import models.view.RegisterResultView;
import org.pac4j.play.java.Secure;
import play.libs.concurrent.HttpExecutionContext;
import play.mvc.Controller;
import play.mvc.Result;

import java.util.Optional;
import java.util.concurrent.CompletionStage;

@Secure(clients = SpireSAML2Client.CLIENT_NAME, authorizers = SamlAuthorizer.AUTHORIZER_NAME)
public class RegisterAwaitController extends Controller {

  private final LicenceFinderDao licenceFinderDao;
  private final HttpExecutionContext httpContext;
  private final String dashboardUrl;
  private final OgelService ogelService;
  private final LicenceFinderService licenceFinderService;
  private final views.html.licencefinder.registerResult registerResult;
  private final views.html.licencefinder.registerWait registerWait;

  @Inject
  public RegisterAwaitController(HttpExecutionContext httpContext,
                                 LicenceFinderDao licenceFinderDao,
                                 @com.google.inject.name.Named("dashboardUrl") String dashboardUrl,
                                 OgelService ogelService, LicenceFinderService licenceFinderService,
                                 views.html.licencefinder.registerResult registerResult,
                                 views.html.licencefinder.registerWait registerWait) {
    this.httpContext = httpContext;
    this.licenceFinderDao = licenceFinderDao;
    this.dashboardUrl = dashboardUrl;
    this.ogelService = ogelService;
    this.licenceFinderService = licenceFinderService;
    this.registerResult = registerResult;
    this.registerWait = registerWait;
  }

  /**
   * renderAwaitResult
   */
  public CompletionStage<Result> renderAwaitResult(String sessionId) {
    Optional<String> regRef = licenceFinderService.getRegistrationReference(sessionId);
    if (regRef.isPresent()) {
      return ogelService.get(licenceFinderDao.getOgelId(sessionId))
          .thenApplyAsync(ogelFullView -> {
            RegisterResultView view = new RegisterResultView("You have successfully registered to use Open general export licence (" + ogelFullView.getName() + ") ", regRef.get());
            return ok(registerResult.render(view, ogelFullView, dashboardUrl));
          }, httpContext.current());
    }
    return completedFuture(ok(registerWait.render(sessionId)));
  }

  /**
   * Handles the RegistrationInterval form submission
   */
  public CompletionStage<Result> handleRegistrationProcessed(String sessionId) {
    return completedFuture(redirect(routes.RegisterAwaitController.renderAwaitResult(sessionId)));
  }
}
