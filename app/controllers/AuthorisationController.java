package controllers;

import static play.mvc.Controller.session;
import static play.mvc.Results.forbidden;
import static play.mvc.Results.ok;

import com.google.inject.Inject;
import play.mvc.Result;
import views.html.auth.loggedOut;
import views.html.auth.unauthorised;

public class AuthorisationController {

  private final unauthorised unauthorised;
  private final loggedOut loggedOut;

  @Inject
  public AuthorisationController(unauthorised unauthorised, loggedOut loggedOut) {
    this.unauthorised = unauthorised;
    this.loggedOut = loggedOut;
  }

  public Result unauthorised() {
    return forbidden(unauthorised.render());
  }

  public Result loggedOut() {
    session().clear();
    return ok(loggedOut.render());
  }

}
