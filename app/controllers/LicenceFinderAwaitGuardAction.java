package controllers;

import static java.util.concurrent.CompletableFuture.completedFuture;

import com.google.inject.Inject;
import components.persistence.LicenceFinderDao;
import org.apache.commons.lang3.StringUtils;
import play.mvc.Action;
import play.mvc.Http;
import play.mvc.Result;

import java.util.concurrent.CompletionStage;

/**
 * Checks for a Registration reference associated with session and redirects to RegisterAwaitController controller if found
 */
public class LicenceFinderAwaitGuardAction extends Action.Simple {

  private final LicenceFinderDao licenceFinderDao;
  private final views.html.licencefinder.errorPage errorPage;

  @Inject
  public LicenceFinderAwaitGuardAction(LicenceFinderDao licenceFinderDao, views.html.licencefinder.errorPage errorPage) {
    this.licenceFinderDao = licenceFinderDao;
    this.errorPage = errorPage;
  }

  @Override
  public CompletionStage<Result> call(Http.Context ctx) {

    // Ensure we have a sessionId in request
    if (!hasSessionId(ctx)) {
      return completedFuture(badRequest(errorPage.render("No session found")));
    }

    // Redirect to registerWait if we have a RegisterLicence
    String sessionId = ctx.request().getQueryString("sessionId");
    if (hasRegisterLicence(sessionId)) {
      return completedFuture(redirect(controllers.licencefinder.routes.RegisterAwaitController.renderAwaitResult(sessionId)));
    }

    // No action required
    return delegate.call(ctx);
  }

  boolean hasSessionId(Http.Context ctx) {
    return !StringUtils.isBlank(ctx.request().getQueryString("sessionId"));
  }

  boolean hasRegisterLicence(String sessionId) {
    return licenceFinderDao.getRegisterLicence(sessionId).isPresent();
  }

}
