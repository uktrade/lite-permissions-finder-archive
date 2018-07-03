package controllers;

import static java.util.concurrent.CompletableFuture.completedFuture;

import com.google.inject.Inject;
import components.services.FlashService;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.LoggerFactory;
import play.mvc.Action;
import play.mvc.Http;
import play.mvc.Result;
import triage.session.SessionService;
import triage.session.TriageSession;

import java.util.concurrent.CompletionStage;

public class SessionGuardAction extends Action.Simple {

  private static final org.slf4j.Logger LOGGER = LoggerFactory.getLogger(SessionGuardAction.class);

  private final FlashService flashService;
  private final SessionService sessionService;

  @Inject
  public SessionGuardAction(FlashService flashService, SessionService sessionService) {
    this.flashService = flashService;
    this.sessionService = sessionService;
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
      } else {
        return delegate.call(ctx);
      }
    }
  }

  private CompletionStage<Result> unknownSession(String sessionId) {
    flashService.flashInvalidSession();
    LOGGER.error("Unknown or blank sessionId " + sessionId);
    return completedFuture(redirect(routes.StartApplicationController.createApplication()));
  }

}
