package controllers.admin;

import actions.BasicAuthAction;
import com.google.inject.Inject;
import play.mvc.Controller;
import play.mvc.Result;
import play.mvc.With;

/**
 * Actions for system administrators
 */
public class AdminController extends Controller {

  @Inject
  public AdminController() {
  }

  @With(BasicAuthAction.class)
  public Result buildInfo() {
    //Use the auto-generated BuildInfo object to produce some JSON describing the build (configured in build.sbt)
    return ok(buildinfo.BuildInfo$.MODULE$.toJson()).as("application/json");
  }

  @With(BasicAuthAction.class)
  public Result ping() {
    return ok("pong");
  }
}
