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
import play.data.validation.Constraints.Required;
import play.libs.concurrent.HttpExecutionContext;
import play.mvc.Result;
import views.html.controlcode.technicalNotes;

import java.util.concurrent.CompletionStage;
import java.util.function.Function;

public class TechnicalNotesController {

  private final JourneyManager jm;
  private final FormFactory formFactory;
  private final PermissionsFinderDao dao;
  private final HttpExecutionContext ec;
  private final FrontendServiceClient frontendServiceClient;

  @Inject
  public TechnicalNotesController(JourneyManager jm,
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

  public CompletionStage<Result> renderForm(){
    return frontendServiceClient.get(dao.getPhysicalGoodControlCode())
        .thenApplyAsync(response -> {
          if (response.isOk()) {
            return ok(technicalNotes.render(formFactory.form(TechnicalNotesForm.class), response.getFrontendServiceResult()));
          }
          return badRequest("An issue occurred while processing your request, please try again later.");
        }, ec.current());
  }

  public CompletionStage<Result> handleSubmit() {
    Form<TechnicalNotesForm> form = formFactory.form(TechnicalNotesForm.class).bindFromRequest();
    String code = dao.getPhysicalGoodControlCode();
    return frontendServiceClient.get(code)
        .thenApplyAsync(response -> {
          if (response.isOk()) {
            if (form.hasErrors()) {
              return completedFuture(ok(technicalNotes.render(form, response.getFrontendServiceResult())));
            }
            String stillDescribesItems = form.get().stillDescribesItems;
            if("true".equals(stillDescribesItems)) {
              return jm.performTransition(Events.CONTROL_CODE_FLOW_NEXT, ControlCodeFlowStage.CONFIRMED);
            }
            if ("false".equals(stillDescribesItems)) {
              return jm.performTransition(Events.CONTROL_CODE_FLOW_NEXT, ControlCodeFlowStage.SEARCH_AGAIN);
            }
          }
          return completedFuture(badRequest("An issue occurred while processing your request, please try again later."));
        }, ec.current()).thenCompose(Function.identity());
  }

  public static class TechnicalNotesForm {

    @Required(message = "You must answer this question")
    public String stillDescribesItems;

  }

}
