package controllers;

import static play.mvc.Results.forbidden;
import static play.mvc.Results.ok;

import com.google.inject.Inject;
import play.mvc.Result;

public class AuthorisationController {

  private final views.html.auth.unauthorised unauthorised;
  private final views.html.auth.loggedOut loggedOut;

  @Inject
  public AuthorisationController(views.html.auth.unauthorised unauthorised, views.html.auth.loggedOut loggedOut) {
    this.unauthorised = unauthorised;
    this.loggedOut = loggedOut;
  }

  public Result unauthorised() {
    return forbidden(unauthorised.render());
  }

  public Result loggedOut() {
    return ok(loggedOut.render());
  }

}
