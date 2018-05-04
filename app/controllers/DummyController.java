package controllers;

import static play.mvc.Results.ok;

import com.google.inject.Inject;
import components.auth.SamlAuthorizer;
import components.common.auth.SpireSAML2Client;
import org.pac4j.play.java.Secure;
import play.mvc.Result;
import views.html.auth.test;

@Secure(clients = SpireSAML2Client.CLIENT_NAME, authorizers = SamlAuthorizer.AUTHORIZER_NAME)
public class DummyController {

  private final test test;

  @Inject
  public DummyController(test test) {
    this.test = test;
  }

  public Result test() {
    return ok(test.render());
  }

}
