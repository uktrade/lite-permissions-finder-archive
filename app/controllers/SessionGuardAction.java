package controllers;

import static java.util.concurrent.CompletableFuture.completedFuture;
import static play.mvc.Controller.flash;

import com.google.inject.Inject;
import components.cms.dao.JourneyDao;
import org.apache.commons.lang3.StringUtils;
import play.Logger;
import play.mvc.Action;
import play.mvc.Http;
import play.mvc.Result;
import triage.session.SessionService;
import triage.session.TriageSession;

import java.util.concurrent.CompletionStage;

public class SessionGuardAction extends Action.Simple {

  private final SessionService sessionService;
  private final JourneyDao journeyDao;

  @Inject
  public SessionGuardAction(SessionService sessionService, JourneyDao journeyDao) {
    this.sessionService = sessionService;
    this.journeyDao = journeyDao;
  }

  @Override
  public CompletionStage<Result> call(Http.Context ctx) {
    String sessionId = ctx.request().getQueryString("sessionId");
    if (StringUtils.isBlank(sessionId)) {
      return unknownSession(sessionId);
    } else {
      TriageSession triageSession = sessionService.getSessionById(sessionId);
      if (triageSession == null) {
        return unknownSession(sessionId);
      } else if ("NOT_FOUND".equals(triageSession.getOutcomeType())) {
        return completedFuture(redirect("NlrType.NOT_FOUND"));
      } else if ("DECONTROL".equals(triageSession.getOutcomeType())) {
        return completedFuture(redirect("NlrType.DECONTROL"));
      } else if ("CONTROL_ENTRY_FOUND".equals(triageSession.getOutcomeType())) {
        return completedFuture(redirect("NlrType.CONTROL_ENTRY_FOUND"));
      } else {
        long sessionJourneyId = triageSession.getJourneyId();
        long currentJourneyId = journeyDao.getJourneysByJourneyName("MILITARY").get(0).getId();
        if (sessionJourneyId != currentJourneyId) {
          Logger.warn("SessionId {} has journeyId {} which doesn't match current journeyId {}",
              sessionId, sessionJourneyId, currentJourneyId);
          return unknownSession(sessionId);
        } else {
          return delegate.call(ctx);
        }
      }
    }
  }

  private CompletionStage<Result> unknownSession(String sessionId) {
    flash("error", "Sorry, your session is no longer valid.");
    flash("detail", "Please start again.");
    Logger.error("Unknown or blank sessionId " + sessionId);
    return completedFuture(redirect(routes.StartApplicationController.createApplication()));
  }

}
