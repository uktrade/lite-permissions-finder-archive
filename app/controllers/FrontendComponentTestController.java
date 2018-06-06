package controllers;

import static play.mvc.Results.notFound;
import static play.mvc.Results.ok;

import com.google.inject.Inject;
import play.mvc.Result;

public class FrontendComponentTestController {

  private final views.html.frontendComponentTests.controlCodeBreadcrumbsTest controlCodeBreadcrumbsTest;

  @Inject
  public FrontendComponentTestController(
      views.html.frontendComponentTests.controlCodeBreadcrumbsTest controlCodeBreadcrumbsTest) {
    this.controlCodeBreadcrumbsTest = controlCodeBreadcrumbsTest;
  }

  public Result test(String component) {
    switch (component) {
      case "controlCodeBreadcrumbs":
        return ok(controlCodeBreadcrumbsTest.render());
      default:
        return notFound();
    }
  }
}
