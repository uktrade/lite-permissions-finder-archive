package controllers.controlcode;

import static play.mvc.Results.ok;

import components.services.controlcode.frontend.FrontendServiceResult;
import play.mvc.Result;
import views.html.controlcode.searchAgain;

public class SearchAgainController {

  public SearchAgainController() {
  }

  public Result render(FrontendServiceResult frontendServiceResult) {
    return ok(searchAgain.render(frontendServiceResult));
  }

}
