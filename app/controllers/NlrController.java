package controllers;

import static play.mvc.Results.notFound;
import static play.mvc.Results.ok;

import com.google.inject.Inject;
import components.auth.SamlAuthorizer;
import components.cms.dao.SessionOutcomeDao;
import components.common.auth.SpireAuthManager;
import components.common.auth.SpireSAML2Client;
import components.services.SessionOutcomeService;
import components.services.UserPrivilegeService;
import models.enums.OutcomeType;
import org.pac4j.play.java.Secure;
import play.Logger;
import play.mvc.Result;
import play.twirl.api.Html;
import triage.session.SessionOutcome;
import triage.session.SessionService;

@Secure(clients = SpireSAML2Client.CLIENT_NAME, authorizers = SamlAuthorizer.AUTHORIZER_NAME)
public class NlrController {

  private final SessionService sessionService;
  private final SessionOutcomeService sessionOutcomeService;
  private final SessionOutcomeDao sessionOutcomeDao;
  private final UserPrivilegeService userPrivilegeService;
  private final SpireAuthManager spireAuthManager;
  private final views.html.nlr.nlrRegisterSuccess nlrRegisterSuccess;

  @Inject
  public NlrController(SessionService sessionService, SessionOutcomeService sessionOutcomeService,
                       SessionOutcomeDao sessionOutcomeDao,
                       UserPrivilegeService userPrivilegeService,
                       SpireAuthManager spireAuthManager,
                       views.html.nlr.nlrRegisterSuccess nlrRegisterSuccess) {
    this.sessionService = sessionService;
    this.sessionOutcomeService = sessionOutcomeService;
    this.sessionOutcomeDao = sessionOutcomeDao;
    this.userPrivilegeService = userPrivilegeService;
    this.spireAuthManager = spireAuthManager;
    this.nlrRegisterSuccess = nlrRegisterSuccess;
  }

  public Result renderOutcome(String outcomeId) {
    SessionOutcome sessionOutcome = sessionOutcomeDao.getSessionOutcomeById(outcomeId);
    if (sessionOutcome == null) {
      return notFound("Unknown outcomeId " + outcomeId);
    } else {
      String userId = spireAuthManager.getAuthInfoFromContext().getId();
      if (userPrivilegeService.canViewOutcome(userId, sessionOutcome)) {
        return ok(new Html(sessionOutcome.getOutcomeHtml()));
      } else {
        Logger.error("User with userId {} doesn't have privilege to view outcome with outcomeId {} ",
            sessionOutcome.getUserId(), sessionOutcome.getId());
        return notFound("Unknown outcomeId " + outcomeId);
      }
    }
  }

  public Result registerNotFoundNlr(String sessionId, String controlEntryId) {
    String resumeCode = sessionService.getSessionById(sessionId).getResumeCode();
    String outcomeId;
    SessionOutcome sessionOutcome = sessionOutcomeDao.getSessionOutcomeBySessionId(sessionId);
    if (sessionOutcome == null) {
      String userId = spireAuthManager.getAuthInfoFromContext().getId();
      outcomeId = sessionOutcomeService.generateNotFoundNlrLetter(userId, sessionId, controlEntryId, resumeCode);
    } else {
      outcomeId = sessionOutcome.getId();
    }
    return ok(nlrRegisterSuccess.render(outcomeId, resumeCode, OutcomeType.NLR_NOT_FOUND));
  }

  public Result registerDecontrolNlr(String sessionId, String stageId) {
    String resumeCode = sessionService.getSessionById(sessionId).getResumeCode();
    String outcomeId;
    SessionOutcome sessionOutcome = sessionOutcomeDao.getSessionOutcomeBySessionId(sessionId);
    if (sessionOutcome == null) {
      String userId = spireAuthManager.getAuthInfoFromContext().getId();
      outcomeId = sessionOutcomeService.generateDecontrolNlrLetter(userId, sessionId, stageId, resumeCode);
    } else {
      outcomeId = sessionOutcome.getId();
    }
    return ok(nlrRegisterSuccess.render(outcomeId, resumeCode, OutcomeType.NLR_DECONTROL));
  }

}
