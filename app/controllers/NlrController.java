package controllers;

import static play.mvc.Results.ok;

import com.google.inject.Inject;
import components.auth.SamlAuthorizer;
import components.common.auth.SpireAuthManager;
import components.common.auth.SpireSAML2Client;
import components.common.client.userservice.UserServiceClientJwt;
import org.pac4j.play.java.Secure;
import play.mvc.Result;
import triage.session.SessionService;
import uk.gov.bis.lite.user.api.view.UserDetailsView;
import views.html.nlr.nlrLetter;
import views.html.nlr.nlrRegisterSuccess;

import java.util.concurrent.ExecutionException;

@Secure(clients = SpireSAML2Client.CLIENT_NAME, authorizers = SamlAuthorizer.AUTHORIZER_NAME)
public class NlrController {

  private final SessionService sessionService;
  private final SpireAuthManager authManager;
  private final UserServiceClientJwt userService;

  @Inject
  public NlrController(SessionService sessionService, SpireAuthManager authManager, UserServiceClientJwt userService) {
    this.sessionService = sessionService;
    this.authManager = authManager;
    this.userService = userService;
  }

  public Result registerNlr(String sessionId, String stageId) {
    String resumeCode = sessionService.getSessionById(sessionId).getResumeCode();
    return ok(nlrRegisterSuccess.render(resumeCode, sessionId));
  }

  //todo
  public Result viewNlrLetter(String sessionId, String resumeCode) throws ExecutionException, InterruptedException {

    String userId = authManager.getAuthInfoFromContext().getId();
    UserDetailsView userDetailsView = userService.getUserDetailsView(userId).toCompletableFuture().get();

    return ok(nlrLetter.render());
  }

}