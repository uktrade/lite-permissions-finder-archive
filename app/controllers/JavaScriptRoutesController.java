package controllers;

import play.mvc.Controller;
import play.mvc.Result;
import play.routing.JavaScriptReverseRouter;

public class JavaScriptRoutesController extends Controller {

  public Result javascriptRoutes() {
    return ok(
        JavaScriptReverseRouter.create("jsRoutes",
            routes.javascript.AjaxRegisterOgelController.pollStatus()
        )
    ).as("text/javascript");
  }

}
