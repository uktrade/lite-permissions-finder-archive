package controllers;

import play.mvc.Controller;
import play.mvc.Result;
//import play.routing.JavaScriptReverseRouter;

public class JavaScriptRoutesController extends Controller {

  public Result jsRoutes() {
    return ok(
        // TODO AJAX routes go here
//        JavaScriptReverseRouter.create("jsRoutes", "some-route")
    ).as("text/javascript");
  }

}
