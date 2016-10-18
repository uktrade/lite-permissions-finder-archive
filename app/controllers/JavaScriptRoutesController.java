package controllers;

import controllers.search.routes;
import play.mvc.Controller;
import play.mvc.Result;
import play.routing.JavaScriptReverseRouter;

public class JavaScriptRoutesController extends Controller {

  public Result jsRoutes() {
    return ok(
        JavaScriptReverseRouter.create("jsRoutes", routes.javascript.AjaxSearchResultsController.getResults())
    ).as("text/javascript");
  }

}
