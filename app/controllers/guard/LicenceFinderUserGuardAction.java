package controllers.guard;

import static java.util.concurrent.CompletableFuture.completedFuture;

import com.google.inject.Inject;
import components.common.auth.SpireAuthManager;
import components.persistence.LicenceFinderDao;
import components.services.FlashService;
import controllers.routes;
import lombok.AllArgsConstructor;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import play.mvc.Action;
import play.mvc.Http;
import play.mvc.Result;

import java.util.concurrent.CompletionStage;

/**
 * Checks for session id and current user against user stored against session
 */
@AllArgsConstructor(onConstructor = @__({ @Inject }))
public class LicenceFinderUserGuardAction extends Action.Simple {

  private static final Logger LOGGER = LoggerFactory.getLogger(LicenceFinderUserGuardAction.class);

  private final LicenceFinderDao licenceFinderDao;
  private final SpireAuthManager authManager;
  private final FlashService flashService;

  @Override
  public CompletionStage<Result> call(Http.Context ctx) {
    String sessionId = ctx.request().getQueryString("sessionId");
    if (StringUtils.isBlank(sessionId)) {
      return unknownSession(sessionId);
    } else {
      String currentUserId = authManager.getAuthInfoFromContext().getId();
      String storedUserId = licenceFinderDao.getUserId(sessionId).orElse(null);
      if (!currentUserId.equals(storedUserId)) {
        LOGGER.error("currentUserId {} doesn't match storedUserId {}", currentUserId, storedUserId);
        return unknownSession(sessionId);
      } else {
        return delegate.call(ctx);
      }
    }
  }

  private CompletionStage<Result> unknownSession(String sessionId) {
    flashService.flashInvalidSession();
    LOGGER.error("Unknown or blank sessionId {}", sessionId);
    return completedFuture(redirect(routes.StartApplicationController.createApplication()));
  }

}
