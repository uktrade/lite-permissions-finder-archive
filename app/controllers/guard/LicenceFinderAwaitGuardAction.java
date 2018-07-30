package controllers.guard;

import static java.util.concurrent.CompletableFuture.completedFuture;

import com.google.inject.Inject;
import components.persistence.LicenceFinderDao;
import components.services.FlashService;
import controllers.routes;
import models.persistence.RegisterLicence;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import play.mvc.Action;
import play.mvc.Http;
import play.mvc.Result;

import java.util.Optional;
import java.util.concurrent.CompletionStage;

/**
 * Checks for a Registration reference associated with session and redirects to RegisterAwaitController controller if found
 */
public class LicenceFinderAwaitGuardAction extends Action.Simple {

  private static final Logger LOGGER = LoggerFactory.getLogger(LicenceFinderAwaitGuardAction.class);

  private final LicenceFinderDao licenceFinderDao;
  private final FlashService flashService;

  @Inject
  public LicenceFinderAwaitGuardAction(LicenceFinderDao licenceFinderDao, FlashService flashService) {
    this.licenceFinderDao = licenceFinderDao;
    this.flashService = flashService;
  }

  @Override
  public CompletionStage<Result> call(Http.Context ctx) {
    String sessionId = ctx.request().getQueryString("sessionId");
    if (StringUtils.isBlank(sessionId)) {
      return unknownSession(sessionId);
    } else {
      Optional<RegisterLicence> optRegisterLicence = licenceFinderDao.getRegisterLicence(sessionId);
      if (optRegisterLicence.isPresent()) {
        return completedFuture(redirect(controllers.licencefinder.routes.RegisterAwaitController.renderAwaitResult(sessionId)));
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
