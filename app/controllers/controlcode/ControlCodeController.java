package controllers.controlcode;

import static java.util.concurrent.CompletableFuture.completedFuture;

import com.google.inject.Inject;
import components.common.journey.JourneyManager;
import components.common.journey.StandardEvents;
import components.persistence.PermissionsFinderDao;
import components.services.controlcode.frontend.ControlCodeData;
import components.services.controlcode.frontend.FrontendServiceClient;
import components.services.controlcode.frontend.FrontendServiceResult;
import journey.Events;
import model.ControlCodeFlowStage;
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

  private final JourneyManager jm;
  private final FormFactory formFactory;
  private final PermissionsFinderDao dao;
  private final HttpExecutionContext ec;
  private final FrontendServiceClient frontendServiceClient;

  @Inject
  public ControlCodeController(JourneyManager jm,
                               FormFactory formFactory,
                               PermissionsFinderDao dao,
                               HttpExecutionContext ec,
                               FrontendServiceClient frontendServiceClient) {
    this.jm = jm;
    this.formFactory = formFactory;
    this.dao = dao;
    this.ec = ec;
    this.frontendServiceClient = frontendServiceClient;
  }


  public CompletionStage<Result> renderForm() {
    return frontendServiceClient.get(dao.getPhysicalGoodControlCode())
        .thenApplyAsync(response -> {
          if (response.isOk()) {
            return ok(controlCode.render(formFactory.form(ControlCodeForm.class), response.getFrontendServiceResult()));
          }
          return badRequest("An issue occurred while processing your request, please try again later.");
        }, ec.current());
  }

  public CompletionStage<Result> handleSubmit() {
    Form<ControlCodeForm> form = formFactory.form(ControlCodeForm.class).bindFromRequest();
    String code = dao.getPhysicalGoodControlCode();
    return frontendServiceClient.get(code)
        .thenApplyAsync(response -> {
          if (response.isOk()) {
            // Outside of form binding to preserve @Required validation for couldDescribeItems
            String action = form.field("action").value();
            if (action != null && !action.isEmpty()) {
              if ("backToSearch".equals(action)) {
                return jm.performTransition(Events.CONTROL_CODE_FLOW_NEXT, ControlCodeFlowStage.BACK_TO_SEARCH);
              }
              if ("backToSearchResults".equals(action)) {
                return jm.performTransition(Events.CONTROL_CODE_FLOW_NEXT, ControlCodeFlowStage.BACK_TO_SEARCH_RESULTS);
              }
              return completedFuture(badRequest("Invalid value for action: \"" + action + "\""));
            }

            if (form.hasErrors()) {
              return completedFuture(ok(controlCode.render(form, response.getFrontendServiceResult())));
            }
            String couldDescribeItems = form.get().couldDescribeItems;
            if("true".equals(couldDescribeItems)) {
              return nextScreenTrue(response.getFrontendServiceResult());
            }
            if ("false".equals(couldDescribeItems)) {
              return jm.performTransition(Events.CONTROL_CODE_FLOW_NEXT, ControlCodeFlowStage.SEARCH_AGAIN);
            }
          }
          return completedFuture(badRequest("An issue occurred while processing your request, please try again later."));
        }, ec.current()).thenCompose(Function.identity());
  }

  public CompletionStage<Result> nextScreenTrue(FrontendServiceResult frontendServiceResult) {
    ControlCodeData controlCodeData = frontendServiceResult.controlCodeData;
    if (controlCodeData.canShow()) {
      if (controlCodeData.canShowAdditionalSpecifications()) {
        return jm.performTransition(Events.CONTROL_CODE_FLOW_NEXT, ControlCodeFlowStage.ADDITIONAL_SPECIFICATIONS);
      }
      else if (controlCodeData.canShowDecontrols()) {
        return jm.performTransition(Events.CONTROL_CODE_FLOW_NEXT, ControlCodeFlowStage.DECONTROLS);
      }
      else {
        return jm.performTransition(Events.CONTROL_CODE_FLOW_NEXT, ControlCodeFlowStage.TECHNICAL_NOTES);
      }
    }
    else {
      return jm.performTransition(Events.CONTROL_CODE_FLOW_NEXT, ControlCodeFlowStage.CONFIRMED);
    }
  }

  public static class ControlCodeForm {

    @Required(message = "You must answer this question")
    public String couldDescribeItems;

    public String action;

  }

}