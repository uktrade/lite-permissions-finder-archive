package controllers;

import static play.mvc.Results.ok;
import static play.mvc.Results.notFound;
import play.mvc.Result;
import views.html.frontendComponentTests.*;

public class FrontendComponentTestController {
  public Result test(String component) {
    switch(component) {
      case "controlCodeBreadcrumbs":
        return ok(controlCodeBreadcrumbsTest.render());
      default:
        return notFound();
    }
  }
}
