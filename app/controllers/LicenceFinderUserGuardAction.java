package controllers;

import static java.util.concurrent.CompletableFuture.completedFuture;

import com.google.inject.Inject;
import components.common.auth.SpireAuthManager;
import components.persistence.LicenceFinderDao;
import org.apache.commons.lang3.StringUtils;
import play.mvc.Action;
import play.mvc.Http;
import play.mvc.Result;

import java.util.concurrent.CompletionStage;

/**
 * Checks for session id and current user against user stored against session
 */
public class LicenceFinderUserGuardAction extends Action.Simple {

  private final LicenceFinderDao licenceFinderDao;
  private final SpireAuthManager authManager;
  private final views.html.licencefinder.errorPage errorPage;

  @Inject
  public LicenceFinderUserGuardAction(LicenceFinderDao licenceFinderDao, SpireAuthManager authManager,
                                      views.html.licencefinder.errorPage errorPage) {
    this.licenceFinderDao = licenceFinderDao;
    this.authManager = authManager;
    this.errorPage = errorPage;
  }

  @Override
  public CompletionStage<Result> call(Http.Context ctx) {

    // Ensure we have a sessionId in request
    if (!hasSessionId(ctx)) {
      return errorPage("No session found");
    }

    // Check the current and stored userId's match
    if(!hasMatchingUserIds(ctx)) {
      return errorPage("Mismatch of user session. Please start again.");
    }

    // No action required
    return delegate.call(ctx);
  }

  boolean hasMatchingUserIds(Http.Context ctx) {
    String currentUserId = authManager.getAuthInfoFromContext().getId();
    String storedUserId = licenceFinderDao.getUserId(ctx.request().getQueryString("sessionId"));
    return currentUserId.equals(storedUserId);
  }

  boolean hasSessionId(Http.Context ctx) {
    return !StringUtils.isBlank(ctx.request().getQueryString("sessionId"));
  }

  private CompletionStage<Result> errorPage(String message) {
    return completedFuture(badRequest(errorPage.render(message)));
  }
}
