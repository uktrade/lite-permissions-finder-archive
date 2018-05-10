package controllers;

import static java.util.concurrent.CompletableFuture.completedFuture;

import com.google.inject.Inject;
import org.apache.commons.lang3.StringUtils;
import play.Logger;
import play.mvc.Action;
import play.mvc.Http;
import play.mvc.Result;
import triage.session.SessionService;

import java.util.concurrent.CompletionStage;

public class SessionGuardAction extends Action.Simple {

  private final SessionService sessionService;

  @Inject
  public SessionGuardAction(SessionService sessionService) {
    this.sessionService = sessionService;
  }

  @Override
  public CompletionStage<Result> call(Http.Context ctx) {
    String sessionId = ctx.request().getQueryString("sessionId");
    if (StringUtils.isBlank(sessionId) || sessionService.getSessionById(sessionId) == null) {
      Logger.error("Unknown or blank sessionId " + sessionId);
      return completedFuture(redirect(routes.StartApplicationController.renderForm()));
    } else {
      return delegate.call(ctx);
    }
  }

}
