package controllers.guard;

import static java.util.concurrent.CompletableFuture.completedFuture;

import com.google.inject.Inject;
import components.cms.dao.SessionOutcomeDao;
import components.services.FlashService;
import controllers.routes;
import lombok.AllArgsConstructor;
import org.apache.commons.lang.StringUtils;
import org.slf4j.LoggerFactory;
import play.mvc.Action;
import play.mvc.Http;
import play.mvc.Result;
import triage.config.JourneyConfigService;
import triage.session.SessionOutcome;
import triage.session.SessionService;
import triage.session.TriageSession;

import java.util.concurrent.CompletionStage;

@AllArgsConstructor(onConstructor = @__({ @Inject }))
public class StageGuardAction extends Action.Simple {

  private static final org.slf4j.Logger LOGGER = LoggerFactory.getLogger(StageGuardAction.class);

  private final FlashService flashService;
  private final SessionService sessionService;
  private final JourneyConfigService journeyConfigService;
  private final SessionOutcomeDao sessionOutcomeDao;

  @Override
  public CompletionStage<Result> call(Http.Context ctx) {
    String sessionId = ctx.request().getQueryString("sessionId");
    if (StringUtils.isBlank(sessionId)) {
      return unknownSession(sessionId);
    } else {
      TriageSession triageSession = sessionService.getSessionById(sessionId);
      if (triageSession == null) {
        return unknownSession(sessionId);
      } else {
        SessionOutcome sessionOutcome = sessionOutcomeDao.getSessionOutcomeBySessionId(sessionId);
        if (sessionOutcome != null) {
          return completedFuture(redirect(routes.ViewOutcomeController.renderOutcome(sessionOutcome.getId())));
        } else {
          long sessionJourneyId = triageSession.getJourneyId();
          long currentJourneyId = journeyConfigService.getDefaultJourneyId();
          if (sessionJourneyId != currentJourneyId) {
            LOGGER.warn("SessionId {} has journeyId {} which doesn't match current journeyId {}",
                sessionId, sessionJourneyId, currentJourneyId);
            //return unknownSession(sessionId);
          }
          return delegate.call(ctx);
        }
      }
    }
  }

  private CompletionStage<Result> unknownSession(String sessionId) {
    flashService.flashInvalidSession();
    LOGGER.error("Unknown or blank sessionId {}", sessionId);
    return completedFuture(redirect(routes.StartApplicationController.createApplication()));
  }

}
