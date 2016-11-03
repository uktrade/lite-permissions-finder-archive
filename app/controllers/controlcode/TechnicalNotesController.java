package controllers.controlcode;

import static java.util.concurrent.CompletableFuture.completedFuture;
import static play.mvc.Results.ok;

import com.google.inject.Inject;
import components.common.journey.JourneyManager;
import components.persistence.PermissionsFinderDao;
import components.services.controlcode.FrontendServiceClient;
import exceptions.FormStateException;
import journey.Events;
import models.ControlCodeFlowStage;
import models.controlcode.TechnicalNotesDisplay;
import play.data.Form;
import play.data.FormFactory;
import play.data.validation.Constraints.Required;
import play.libs.concurrent.HttpExecutionContext;
import play.mvc.Result;
import views.html.controlcode.technicalNotes;

import java.util.Optional;
import java.util.concurrent.CompletionStage;
import java.util.function.Function;

public class TechnicalNotesController {

  private final JourneyManager journeyManager;
  private final FormFactory formFactory;
  private final PermissionsFinderDao permissionsFinderDao;
  private final HttpExecutionContext httpExecutionContext;
  private final FrontendServiceClient frontendServiceClient;

  @Inject
  public TechnicalNotesController(JourneyManager journeyManager,
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

  public CompletionStage<Result> renderForm(){
    Optional<Boolean> technicalNotesApply = permissionsFinderDao.getControlCodeTechnicalNotesApply();
    TechnicalNotesForm templateForm = new TechnicalNotesForm();
    templateForm.stillDescribesItems = technicalNotesApply.isPresent() ? technicalNotesApply.get().toString() : "";
    return frontendServiceClient.get(permissionsFinderDao.getPhysicalGoodControlCode())
        .thenApplyAsync(result -> ok(technicalNotes.render(formFactory.form(TechnicalNotesForm.class).fill(templateForm),
            new TechnicalNotesDisplay(result))), httpExecutionContext.current());
  }

  public CompletionStage<Result> handleSubmit() {
    Form<TechnicalNotesForm> form = formFactory.form(TechnicalNotesForm.class).bindFromRequest();
    String code = permissionsFinderDao.getPhysicalGoodControlCode();
    return frontendServiceClient.get(code)
        .thenApplyAsync(result -> {
          if (form.hasErrors()) {
            return completedFuture(ok(technicalNotes.render(form, new TechnicalNotesDisplay(result))));
          }
          else {
            String stillDescribesItems = form.get().stillDescribesItems;
            if("true".equals(stillDescribesItems)) {
              permissionsFinderDao.saveControlCodeTechnicalNotesApply(true);
              return journeyManager.performTransition(Events.CONTROL_CODE_FLOW_NEXT, ControlCodeFlowStage.CONFIRMED);
            }
            else if ("false".equals(stillDescribesItems)) {
              permissionsFinderDao.saveControlCodeTechnicalNotesApply(false);
              return journeyManager.performTransition(Events.CONTROL_CODE_FLOW_NEXT, ControlCodeFlowStage.NOT_APPLICABLE);
            }
            else {
              throw new FormStateException("Unhandled form state");
            }
          }
        }, httpExecutionContext.current()).thenCompose(Function.identity());
  }

  public static class TechnicalNotesForm {

    @Required(message = "You must answer this question")
    public String stillDescribesItems;

  }

}
