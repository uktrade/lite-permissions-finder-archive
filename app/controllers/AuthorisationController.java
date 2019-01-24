package controllers;

import static play.mvc.Results.forbidden;
import static play.mvc.Results.ok;

import com.google.inject.Inject;
import lombok.AllArgsConstructor;
import play.mvc.Result;

@AllArgsConstructor(onConstructor = @__({ @Inject }))
public class AuthorisationController {

  private final views.html.auth.unauthorised unauthorised;
  private final views.html.auth.loggedOut loggedOut;

  public Result unauthorised() {
    return forbidden(unauthorised.render());
  }

  public Result loggedOut() {
    return ok(loggedOut.render());
  }

}
