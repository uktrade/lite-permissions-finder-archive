package controllers;

import static play.mvc.Results.notFound;
import static play.mvc.Results.ok;

import com.google.inject.Inject;
import components.auth.SamlAuthorizer;
import components.cms.dao.SessionOutcomeDao;
import components.common.auth.SpireSAML2Client;
import components.services.SessionOutcomeService;
import models.enums.OutcomeType;
import org.pac4j.play.java.Secure;
import play.mvc.Result;
import play.twirl.api.Html;
import triage.session.SessionOutcome;
import triage.session.SessionService;

@Secure(clients = SpireSAML2Client.CLIENT_NAME, authorizers = SamlAuthorizer.AUTHORIZER_NAME)
public class NlrController {

  private final SessionService sessionService;
  private final SessionOutcomeService sessionOutcomeService;
  private final SessionOutcomeDao sessionOutcomeDao;
  private final views.html.nlr.nlrRegisterSuccess nlrRegisterSuccess;

  @Inject
  public NlrController(SessionService sessionService, SessionOutcomeService sessionOutcomeService,
                       SessionOutcomeDao sessionOutcomeDao, views.html.nlr.nlrRegisterSuccess nlrRegisterSuccess) {
    this.sessionService = sessionService;
    this.sessionOutcomeService = sessionOutcomeService;
    this.sessionOutcomeDao = sessionOutcomeDao;
    this.nlrRegisterSuccess = nlrRegisterSuccess;
  }

  public Result renderOutcome(String sessionId) {
    SessionOutcome sessionOutcome = sessionOutcomeDao.getSessionOutcomeBySessionId(sessionId);
    if (sessionOutcome == null) {
      return notFound();
    } else {
      return ok(new Html(sessionOutcome.getOutcomeHtml()));
    }
  }

  public Result registerNotFoundNlr(String sessionId, String controlEntryId) {
    String resumeCode = sessionService.getSessionById(sessionId).getResumeCode();
    SessionOutcome sessionOutcome = sessionOutcomeDao.getSessionOutcomeBySessionId(sessionId);
    if (sessionOutcome == null) {
      sessionOutcomeService.generateNotFoundNlrLetter(sessionId, controlEntryId, resumeCode);
    }
    return ok(nlrRegisterSuccess.render(sessionId, resumeCode, OutcomeType.NLR_NOT_FOUND));
  }

  public Result registerDecontrolNlr(String sessionId, String stageId) {
    String resumeCode = sessionService.getSessionById(sessionId).getResumeCode();
    SessionOutcome sessionOutcome = sessionOutcomeDao.getSessionOutcomeBySessionId(sessionId);
    if (sessionOutcome == null) {
      sessionOutcomeService.generateDecontrolNlrLetter(sessionId, stageId, resumeCode);
    }
    return ok(nlrRegisterSuccess.render(sessionId, resumeCode, OutcomeType.NLR_DECONTROL));
  }

}
