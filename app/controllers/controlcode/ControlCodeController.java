package controllers.controlcode;

import static java.util.concurrent.CompletableFuture.completedFuture;

import com.google.inject.Inject;
import components.common.journey.JourneyManager;
import components.persistence.PermissionsFinderDao;
import components.services.controlcode.frontend.ControlCodeData;
import components.services.controlcode.frontend.FrontendServiceClient;
import components.services.controlcode.frontend.FrontendServiceResult;
import exceptions.FormStateException;
import journey.Events;
import models.ControlCodeFlowStage;
import play.data.Form;
import play.data.FormFactory;
import play.data.validation.Constraints.Required;
import play.libs.concurrent.HttpExecutionContext;
import play.mvc.Controller;
import play.mvc.Result;
import views.html.controlcode.controlCode;

import java.util.concurrent.CompletionStage;
import java.util.function.Function;

public class ControlCodeController extends Controller {

  private final JourneyManager journeyManager;
  private final FormFactory formFactory;
  private final PermissionsFinderDao permissionsFinderDao;
  private final HttpExecutionContext httpExecutionContext;
  private final FrontendServiceClient frontendServiceClient;

  @Inject
  public ControlCodeController(JourneyManager journeyManager,
                               FormFactory formFactory,
                               PermissionsFinderDao permissionsFinderDao,
                               HttpExecutionContext httpExecutionContext,
                               FrontendServiceClient frontendServiceClient) {
    this.journeyManager = journeyManager;
    this.formFactory = formFactory;
    this.permissionsFinderDao = permissionsFinderDao;
    this.httpExecutionContext = httpExecutionContext;
    this.frontendServiceClient = frontendServiceClient;
  }


  public CompletionStage<Result> renderForm() {
    return frontendServiceClient.get(permissionsFinderDao.getPhysicalGoodControlCode())
        .thenApplyAsync(result ->
            ok(controlCode.render(formFactory.form(ControlCodeForm.class), result))
        , httpExecutionContext.current());
  }

  public CompletionStage<Result> handleSubmit() {
    Form<ControlCodeForm> form = formFactory.form(ControlCodeForm.class).bindFromRequest();
    String code = permissionsFinderDao.getPhysicalGoodControlCode();
    return frontendServiceClient.get(code)
        .thenApplyAsync(result -> {

          // Outside of form binding to preserve @Required validation for couldDescribeItems
          String action = form.field("action").value();
          if (action != null && !action.isEmpty()) {
            if ("backToSearch".equals(action)) {
              return journeyManager.performTransition(Events.CONTROL_CODE_FLOW_NEXT, ControlCodeFlowStage.BACK_TO_SEARCH);
            }
            if ("backToSearchResults".equals(action)) {
              return journeyManager.performTransition(Events.CONTROL_CODE_FLOW_NEXT, ControlCodeFlowStage.BACK_TO_SEARCH_RESULTS);
            }
            throw new FormStateException("Invalid value for action: \"" + action + "\"");
          }

          if (form.hasErrors()) {
            return completedFuture(ok(controlCode.render(form, result)));
          }
          String couldDescribeItems = form.get().couldDescribeItems;
          if("true".equals(couldDescribeItems)) {
            return nextScreenTrue(result);
          }
          if ("false".equals(couldDescribeItems)) {
            return journeyManager.performTransition(Events.CONTROL_CODE_FLOW_NEXT, ControlCodeFlowStage.SEARCH_AGAIN);
          }

          throw new FormStateException("Unhandled form state");
        }, httpExecutionContext.current()).thenCompose(Function.identity());
  }

  public CompletionStage<Result> nextScreenTrue(FrontendServiceResult frontendServiceResult) {
    ControlCodeData controlCodeData = frontendServiceResult.controlCodeData;
    if (controlCodeData.canShow()) {
      if (controlCodeData.canShowAdditionalSpecifications()) {
        return journeyManager.performTransition(Events.CONTROL_CODE_FLOW_NEXT, ControlCodeFlowStage.ADDITIONAL_SPECIFICATIONS);
      }
      else if (controlCodeData.canShowDecontrols()) {
        return journeyManager.performTransition(Events.CONTROL_CODE_FLOW_NEXT, ControlCodeFlowStage.DECONTROLS);
      }
      else {
        return journeyManager.performTransition(Events.CONTROL_CODE_FLOW_NEXT, ControlCodeFlowStage.TECHNICAL_NOTES);
      }
    }
    else {
      return journeyManager.performTransition(Events.CONTROL_CODE_FLOW_NEXT, ControlCodeFlowStage.CONFIRMED);
    }
  }

  public static class ControlCodeForm {

    @Required(message = "You must answer this question")
    public String couldDescribeItems;

    public String action;

  }

}