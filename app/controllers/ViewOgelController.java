package controllers;


import com.google.inject.Inject;
import components.services.ogels.ogel.OgelServiceClient;
import play.libs.concurrent.HttpExecutionContext;
import play.mvc.Controller;
import play.mvc.Result;
import views.html.ogel.ogelView;

import java.util.concurrent.CompletionStage;

public class ViewOgelController extends Controller {

  private final OgelServiceClient ogelServiceClient;
  private final HttpExecutionContext httpExecutionContext;

  @Inject
  public ViewOgelController(OgelServiceClient ogelServiceClient, HttpExecutionContext httpExecutionContext) {
    this.ogelServiceClient = ogelServiceClient;
    this.httpExecutionContext = httpExecutionContext;
  }

  public CompletionStage<Result> viewOgel(String ogelId) {
    return ogelServiceClient.get(ogelId)
        .thenApplyAsync(result -> ok(ogelView.render(result)), httpExecutionContext.current());
  }
}
