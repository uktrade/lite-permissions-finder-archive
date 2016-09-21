package controllers.controlcode;

import static java.util.concurrent.CompletableFuture.completedFuture;
import static play.mvc.Results.badRequest;
import static play.mvc.Results.ok;

import com.google.inject.Inject;
import components.common.journey.JourneyManager;
import components.persistence.PermissionsFinderDao;
import components.services.controlcode.frontend.FrontendServiceClient;
import journey.Events;
import model.ControlCodeFlowStage;
import play.data.Form;
import play.data.FormFactory;
import play.libs.concurrent.HttpExecutionContext;
import play.mvc.Result;
import views.html.controlcode.searchAgain;

import java.util.concurrent.CompletionStage;

public class SearchAgainController {

  private final JourneyManager jm;
  private final FormFactory formFactory;
  private final PermissionsFinderDao permissionsFinderDao;
  private final HttpExecutionContext httpExecutionContext;
  private final FrontendServiceClient frontendServiceClient;

  @Inject
  public SearchAgainController(JourneyManager jm,
                               FormFactory formFactory,
                               PermissionsFinderDao permissionsFinderDao,
                               HttpExecutionContext httpExecutionContext,
                               FrontendServiceClient frontendServiceClient) {
    this.jm = jm;
    this.formFactory = formFactory;
    this.permissionsFinderDao = permissionsFinderDao;
    this.httpExecutionContext = httpExecutionContext;
    this.frontendServiceClient = frontendServiceClient;
  }

  public CompletionStage<Result> renderForm() {
    return frontendServiceClient.get(permissionsFinderDao.getPhysicalGoodControlCode())
        .thenApplyAsync(response -> {
          if (response.isOk()) {
            return ok(searchAgain.render(response.getFrontendServiceResult()));
          }
          return badRequest("An issue occurred while processing your request, please try again later.");
        }, httpExecutionContext.current());
  }

  public CompletionStage<Result> handleSubmit() {
    Form<SearchAgainForm> form = formFactory.form(SearchAgainForm.class).bindFromRequest();
    if (!form.hasErrors()) {
      String action = form.get().action;
      if ("backToSearch".equals(action)) {
        return jm.performTransition(Events.CONTROL_CODE_FLOW_NEXT, ControlCodeFlowStage.BACK_TO_SEARCH);
      }
      if ("backToSearchResults".equals(action)) {
        return jm.performTransition(Events.CONTROL_CODE_FLOW_NEXT, ControlCodeFlowStage.BACK_TO_SEARCH_RESULTS);
      }
    }
    return completedFuture(badRequest("Invalid form state"));
  }

  public static class SearchAgainForm {

    public String action;

  }

}
