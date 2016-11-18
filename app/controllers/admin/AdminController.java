package controllers.admin;

import play.mvc.Controller;
import play.mvc.Result;

/**
 * Actions for system administrators
 */
public class AdminController extends Controller {

  public Result buildInfo() {
    //Use the auto-generated BuildInfo object to produce some JSON describing the build (configured in build.sbt)
    return ok(buildinfo.BuildInfo$.MODULE$.toJson());
  }

}
