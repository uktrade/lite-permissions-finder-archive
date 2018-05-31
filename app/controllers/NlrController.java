package controllers;

import static play.mvc.Results.ok;

import com.google.inject.Inject;
import components.auth.SamlAuthorizer;
import components.common.auth.SpireSAML2Client;
import org.pac4j.play.java.Secure;
import play.mvc.Result;
import triage.session.SessionService;
import views.html.nlr.nlrLetter;
import views.html.nlr.nlrRegisterSuccess;

@Secure(clients = SpireSAML2Client.CLIENT_NAME, authorizers = SamlAuthorizer.AUTHORIZER_NAME)
public class NlrController {

  private final SessionService sessionService;

  @Inject
  public NlrController(SessionService sessionService) {
    this.sessionService = sessionService;
  }

  public Result registerNlr(String sessionId) {
    String resumeCode = sessionService.getSessionById(sessionId).getResumeCode();
    return ok(nlrRegisterSuccess.render(resumeCode, sessionId));
  }

  //todo
  public Result viewNlrLetter(String sessionId, String resumeCode) {
    return ok(nlrLetter.render());
  }

}
