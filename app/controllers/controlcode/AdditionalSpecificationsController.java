package controllers.controlcode;

import static java.util.concurrent.CompletableFuture.completedFuture;
import static play.mvc.Results.badRequest;
import static play.mvc.Results.ok;

import com.google.inject.Inject;
import components.common.journey.JourneyManager;
import components.persistence.PermissionsFinderDao;
import components.services.controlcode.frontend.FrontendServiceClient;
import components.services.controlcode.frontend.FrontendServiceResult;
import journey.Events;
import model.ControlCodeFlowStage;
import play.data.Form;
import play.data.FormFactory;
import play.data.validation.Constraints.Required;
import play.libs.concurrent.HttpExecutionContext;
import play.mvc.Result;
import views.html.controlcode.additionalSpecifications;

import java.util.concurrent.CompletionStage;
import java.util.function.Function;

public class AdditionalSpecificationsController {

  private final JourneyManager jm;
  private final FormFactory formFactory;
  private final PermissionsFinderDao permissionsFinderDao;
  private final HttpExecutionContext httpExecutionContext;
  private final FrontendServiceClient frontendServiceClient;


  @Inject
  public AdditionalSpecificationsController(JourneyManager jm,
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
            return ok(additionalSpecifications.render(formFactory.form(AdditionalSpecificationsForm.class), response.getFrontendServiceResult()));
          }
          return badRequest("An issue occurred while processing your request, please try again later.");
        }, httpExecutionContext.current());
  }

  public CompletionStage<Result> handleSubmit() {
    Form<AdditionalSpecificationsForm> form = formFactory.form(AdditionalSpecificationsForm.class).bindFromRequest();
    String code = permissionsFinderDao.getPhysicalGoodControlCode();
    return frontendServiceClient.get(code)
        .thenApplyAsync(response -> {
          if (response.isOk()) {
            if (form.hasErrors()) {
              return completedFuture(ok(additionalSpecifications.render(form, response.getFrontendServiceResult())));
            }
            String stillDescribesItems = form.get().stillDescribesItems;
            if("true".equals(stillDescribesItems)) {
              return nextScreenTrue(response.getFrontendServiceResult());
            }
            if ("false".equals(stillDescribesItems)) {
              return jm.performTransition(Events.CONTROL_CODE_FLOW_NEXT, ControlCodeFlowStage.SEARCH_AGAIN);
            }
          }
          return completedFuture(badRequest("An issue occurred while processing your request, please try again later."));
        }, httpExecutionContext.current()).thenCompose(Function.identity());
  }

  public CompletionStage<Result> nextScreenTrue(FrontendServiceResult frontendServiceResult){
    if (frontendServiceResult.controlCodeData.canShowDecontrols()) {
      return jm.performTransition(Events.CONTROL_CODE_FLOW_NEXT, ControlCodeFlowStage.DECONTROLS);
    }
    else if (frontendServiceResult.controlCodeData.canShowTechnicalNotes()) {
      return jm.performTransition(Events.CONTROL_CODE_FLOW_NEXT, ControlCodeFlowStage.TECHNICAL_NOTES);
    }
    return jm.performTransition(Events.CONTROL_CODE_FLOW_NEXT, ControlCodeFlowStage.CONFIRMED);
  }

  public static class AdditionalSpecificationsForm {

    @Required(message = "You must answer this question")
    public String stillDescribesItems;

  }
}
