package controllers.controlcode;

import static java.util.concurrent.CompletableFuture.completedFuture;
import static play.mvc.Results.ok;

import com.google.inject.Inject;
import components.common.journey.JourneyManager;
import components.persistence.PermissionsFinderDao;
import components.services.controlcode.FrontendServiceClient;
import exceptions.FormStateException;
import journey.Events;
import journey.helpers.ControlCodeJourneyHelper;
import journey.helpers.SoftwareJourneyHelper;
import models.ControlCodeFlowStage;
import models.controlcode.ControlCodeJourney;
import models.controlcode.DecontrolsDisplay;
import models.software.SoftwareCategory;
import play.data.Form;
import play.data.FormFactory;
import play.data.validation.Constraints.Required;
import play.libs.concurrent.HttpExecutionContext;
import play.mvc.Result;
import views.html.controlcode.decontrols;

import java.util.Optional;
import java.util.concurrent.CompletionStage;
import java.util.function.Function;

public class DecontrolsController {

  private final JourneyManager journeyManager;
  private final FormFactory formFactory;
  private final PermissionsFinderDao permissionsFinderDao;
  private final HttpExecutionContext httpExecutionContext;
  private final FrontendServiceClient frontendServiceClient;
  private final ControlCodeJourneyHelper controlCodeJourneyHelper;

  @Inject
  public DecontrolsController(JourneyManager journeyManager,
                              FormFactory formFactory,
                              PermissionsFinderDao permissionsFinderDao,
                              HttpExecutionContext httpExecutionContext,
                              FrontendServiceClient frontendServiceClient,
                              ControlCodeJourneyHelper controlCodeJourneyHelper) {
    this.journeyManager = journeyManager;
    this.formFactory = formFactory;
    this.permissionsFinderDao = permissionsFinderDao;
    this.httpExecutionContext = httpExecutionContext;
    this.frontendServiceClient = frontendServiceClient;
    this.controlCodeJourneyHelper = controlCodeJourneyHelper;
  }

  private CompletionStage<Result> renderForm(ControlCodeJourney controlCodeJourney) {
    Optional<Boolean> decontrolsApply = permissionsFinderDao.getControlCodeDecontrolsApply(controlCodeJourney);
    DecontrolsForm templateForm = new DecontrolsForm();
    templateForm.decontrolsDescribeItem = decontrolsApply.isPresent() ? decontrolsApply.get().toString() : "";
    return frontendServiceClient.get(permissionsFinderDao.getSelectedControlCode(controlCodeJourney))
        .thenApplyAsync(result -> ok(decontrols.render(formFactory.form(DecontrolsForm.class).fill(templateForm),
            new DecontrolsDisplay(controlCodeJourney, result))), httpExecutionContext.current());
  }

  public CompletionStage<Result> renderForm() {
    return renderForm(ControlCodeJourney.PHYSICAL_GOODS_SEARCH);
  }

  public CompletionStage<Result> renderRelatedToSoftwareForm() {
    return renderForm(ControlCodeJourney.PHYSICAL_GOODS_SEARCH_RELATED_TO_SOFTWARE);
  }

  public CompletionStage<Result> renderSoftwareControlsForm() {
    return renderForm(ControlCodeJourney.SOFTWARE_CONTROLS);
  }

  private CompletionStage<Result> handleSubmit(ControlCodeJourney controlCodeJourney){
    Form<DecontrolsForm> form = formFactory.form(DecontrolsForm.class).bindFromRequest();
    String controlCode = permissionsFinderDao.getSelectedControlCode(controlCodeJourney);
    return frontendServiceClient.get(controlCode)
        .thenApplyAsync(result -> {
          if (form.hasErrors()) {
            return completedFuture(ok(decontrols.render(form, new DecontrolsDisplay(controlCodeJourney, result))));
          }
          else {
            String decontrolsDescribeItem = form.get().decontrolsDescribeItem;
            if("true".equals(decontrolsDescribeItem)) {
              permissionsFinderDao.saveControlCodeDecontrolsApply(controlCodeJourney, true);
              return controlCodeJourneyHelper.notApplicableJourneyTransition(controlCodeJourney);
            }
            else if ("false".equals(decontrolsDescribeItem)) {
              permissionsFinderDao.saveControlCodeDecontrolsApply(controlCodeJourney, false);
              if (result.controlCodeData.canShowTechnicalNotes()) {
                return journeyManager.performTransition(Events.CONTROL_CODE_FLOW_NEXT, ControlCodeFlowStage.TECHNICAL_NOTES);
              }
              else {
                return controlCodeJourneyHelper.confirmedJourneyTransition(controlCodeJourney, controlCode);
              }
            }
            else {
              throw new FormStateException("Unhandled form state");
            }
          }
        }, httpExecutionContext.current()).thenCompose(Function.identity());
  }

  public CompletionStage<Result> handleSubmit() {
    return handleSubmit(ControlCodeJourney.PHYSICAL_GOODS_SEARCH);
  }

  public CompletionStage<Result> handleRelatedToSoftwareSubmit() {
    return handleSubmit(ControlCodeJourney.PHYSICAL_GOODS_SEARCH_RELATED_TO_SOFTWARE);
  }

  public CompletionStage<Result> handleSoftwareControlsSubmit() {
    return handleSubmit(ControlCodeJourney.SOFTWARE_CONTROLS);
  }

  public static class DecontrolsForm {

    @Required(message = "You must answer this question")
    public String decontrolsDescribeItem;

  }

}
