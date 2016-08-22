package controllers.controlcode;

import static play.mvc.Results.ok;

import components.services.controlcode.frontend.FrontendServiceResult;
import play.mvc.Result;
import views.html.controlcode.decontrolledItem;

public class DecontrolledItemController {


  public DecontrolledItemController() {
  }

  public Result render(FrontendServiceResult frontendServiceResult) {
    return ok(decontrolledItem.render(frontendServiceResult, true));
  }
}
