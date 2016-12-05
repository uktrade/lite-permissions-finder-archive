package controllers;


import com.google.inject.Inject;
import components.services.ogels.ogel.OgelServiceClient;
import play.mvc.Controller;
import play.mvc.Result;
import views.html.ogel.ogelView;

import java.util.concurrent.CompletionStage;

public class ViewOgelController extends Controller {

  private final OgelServiceClient ogelServiceClient;

  @Inject
  public ViewOgelController(OgelServiceClient ogelServiceClient) {
    this.ogelServiceClient = ogelServiceClient;
  }

  public CompletionStage<Result> viewOgel(String ogelId) {
    return ogelServiceClient.get(ogelId).thenApply(result ->
        ok(ogelView.render(result))
    );
  }
}
