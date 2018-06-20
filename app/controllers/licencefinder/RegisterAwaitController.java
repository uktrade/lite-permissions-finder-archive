package controllers.licencefinder;

import static java.util.concurrent.CompletableFuture.completedFuture;

import com.google.inject.Inject;
import components.auth.SamlAuthorizer;
import components.common.auth.SpireSAML2Client;
import components.common.cache.CountryProvider;
import components.persistence.LicenceFinderDao;
import components.services.LicenceFinderService;
import components.services.OgelService;
import models.view.RegisterResultView;
import org.pac4j.play.java.Secure;
import play.data.FormFactory;
import play.libs.concurrent.HttpExecutionContext;
import play.mvc.Controller;
import play.mvc.Result;

import java.util.Optional;
import java.util.concurrent.CompletionStage;

import javax.inject.Named;

@Secure(clients = SpireSAML2Client.CLIENT_NAME, authorizers = SamlAuthorizer.AUTHORIZER_NAME)
//@With(UserGuardAction.class)
public class RegisterAwaitController extends Controller {

  private static final String CONTROL_CODE_QUESTION = "What Control list entry describes your goods?";
  private static final String GOODS_GOING_QUESTION = "Where are your goods going?";
  private static final String FIRST_COUNTRY = "First country or territory that will receive the items";
  private static final String REPAIR_QUESTION = "Are you exporting goods for or after repair or replacement?";
  private static final String EXHIBITION_QUESTION = "Are you exporting goods for or after exhibition or demonstration?";
  private static final String BEFORE_OR_LESS_QUESTION = "Were your goods manufactured before 1897, and worth less than £30,000?";

  private static final String YES = "Yes";
  private static final String NO = "No";

  private final FormFactory formFactory;
  private final LicenceFinderDao licenceFinderDao;
  private final CountryProvider countryProvider;
  private final HttpExecutionContext httpContext;
  private final String dashboardUrl;
  private final OgelService ogelService;
  private final LicenceFinderService licenceFinderService;
  private final views.html.licencefinder.registerResult registerResult;
  private final views.html.licencefinder.registerToUse registerToUse;
  private final views.html.licencefinder.registerWait registerWait;

  @Inject
  public RegisterAwaitController(FormFactory formFactory,
                                 HttpExecutionContext httpContext,
                                 LicenceFinderDao licenceFinderDao,
                                 @Named("countryProviderExport") CountryProvider countryProvider,
                                 @com.google.inject.name.Named("dashboardUrl") String dashboardUrl,
                                 OgelService ogelService, LicenceFinderService licenceFinderService,
                                 views.html.licencefinder.registerResult registerResult,
                                 views.html.licencefinder.registerToUse registerToUse,
                                 views.html.licencefinder.registerWait registerWait) {
    this.formFactory = formFactory;
    this.httpContext = httpContext;
    this.licenceFinderDao = licenceFinderDao;
    this.countryProvider = countryProvider;
    this.dashboardUrl = dashboardUrl;
    this.ogelService = ogelService;
    this.licenceFinderService = licenceFinderService;
    this.registerResult = registerResult;
    this.registerToUse = registerToUse;
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
            return ok(registerResult.render(view, ogelFullView, dashboardUrl, sessionId));
          }, httpContext.current());
    }
    return completedFuture(ok(registerWait.render(sessionId)));
  }

  /**
   * Handles the RegistrationInterval form submission
   */
  public CompletionStage<Result> handleRegistrationProcessed(String sessionId) {
    return completedFuture(redirect(routes.RegisterAwaitController.renderAwaitResult(sessionId)));
    //return CompletableFuture.completedFuture(redirect(licencefinder.routes.RegisterAwaitController.renderAwaitResult(sessionId)));
  }
}
