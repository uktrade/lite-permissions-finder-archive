package controllers;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import components.cms.dao.SessionOutcomeDao;
import components.services.FlashService;
import controllers.guard.StageGuardAction;
import models.enums.SessionOutcomeType;
import org.junit.Test;
import play.mvc.Action;
import play.mvc.Http;
import play.mvc.Result;
import triage.config.JourneyConfigService;
import triage.session.SessionOutcome;
import triage.session.SessionService;
import triage.session.TriageSession;

public class StageGuardActionTest {

  private static final String SESSION_ID = "session-id";

  private final FlashService flashService = mock(FlashService.class);
  private final SessionService sessionService = mock(SessionService.class);
  private final JourneyConfigService journeyConfigService = mock(JourneyConfigService.class);
  private final SessionOutcomeDao sessionOutcomeDao = mock(SessionOutcomeDao.class);
  private final StageGuardAction sessionGuardAction = new StageGuardAction(flashService, sessionService,
      journeyConfigService, sessionOutcomeDao);

  @Test
  public void blankSessionIdShouldReturnCreateApplicationRedirect() throws Exception {
    Http.Context context = mockContext("        ");
    Result result = sessionGuardAction.call(context).toCompletableFuture().get();

    assertThat(result.status()).isEqualTo(Http.Status.SEE_OTHER);
    assertThat(result.redirectLocation()).contains(routes.StartApplicationController.createApplication().toString());

    verify(flashService).flashInvalidSession();
  }

  @Test
  public void unknownSessionIdShouldReturnCreateApplicationRedirect() throws Exception {
    Http.Context context = mockContext("unknown");
    Result result = sessionGuardAction.call(context).toCompletableFuture().get();

    assertThat(result.status()).isEqualTo(Http.Status.SEE_OTHER);
    assertThat(result.redirectLocation()).contains(routes.StartApplicationController.createApplication().toString());

    verify(flashService).flashInvalidSession();
  }

  @Test
  public void sessionIdWithOutcomeShouldReturnOutcomeRedirect() throws Exception {
    TriageSession triageSession = new TriageSession(SESSION_ID, 1L, "resumeCode", 1L);
    when(sessionService.getSessionById(SESSION_ID)).thenReturn(triageSession);
    SessionOutcome sessionOutcome = new SessionOutcome("session-outcome-id", SESSION_ID, "userId",
        "customerId", "siteId", SessionOutcomeType.CONTROL_ENTRY_FOUND, "outcomeHtml");
    when(sessionOutcomeDao.getSessionOutcomeBySessionId(SESSION_ID)).thenReturn(sessionOutcome);

    Http.Context context = mockContext(SESSION_ID);
    Result result = sessionGuardAction.call(context).toCompletableFuture().get();

    assertThat(result.status()).isEqualTo(Http.Status.SEE_OTHER);
    assertThat(result.redirectLocation()).contains(routes.ViewOutcomeController.renderOutcome("session-outcome-id").toString());
  }

  @Test
  public void sessionIdWithOutdatedJourneyIdShouldReturnCreateApplicationRedirect() throws Exception {
    TriageSession triageSession = new TriageSession(SESSION_ID, 1L, "resumeCode", 1L);
    when(sessionService.getSessionById(SESSION_ID)).thenReturn(triageSession);
    when(journeyConfigService.getDefaultJourneyId()).thenReturn(2L);

    Http.Context context = mockContext(SESSION_ID);
    Result result = sessionGuardAction.call(context).toCompletableFuture().get();

    assertThat(result.status()).isEqualTo(Http.Status.SEE_OTHER);
    assertThat(result.redirectLocation()).contains(routes.StartApplicationController.createApplication().toString());

    verify(flashService).flashInvalidSession();
  }

  @Test
  public void validSessionIdShouldCallDelegate() throws Exception {
    TriageSession triageSession = new TriageSession(SESSION_ID, 1L, "resumeCode", 1L);
    when(sessionService.getSessionById(SESSION_ID)).thenReturn(triageSession);
    when(journeyConfigService.getDefaultJourneyId()).thenReturn(1L);
    Action action = mock(Action.class);
    sessionGuardAction.delegate = action;

    Http.Context context = mockContext(SESSION_ID);
    sessionGuardAction.call(context);

    verify(action).call(context);
  }

  private Http.Context mockContext(String sessionId) {
    Http.Request request = mock(Http.Request.class);
    when(request.getQueryString("sessionId")).thenReturn(sessionId);
    Http.Context context = mock(Http.Context.class);
    when(context.request()).thenReturn(request);
    return context;
  }

}
