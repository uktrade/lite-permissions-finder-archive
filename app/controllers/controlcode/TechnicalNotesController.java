package controllers.controlcode;

import static java.util.concurrent.CompletableFuture.completedFuture;
import static play.mvc.Results.ok;

import com.google.inject.Inject;
import components.common.journey.JourneyManager;
import components.persistence.PermissionsFinderDao;
import components.services.controlcode.FrontendServiceClient;
import exceptions.FormStateException;
import journey.helpers.ControlCodeJourneyHelper;
import models.controlcode.ControlCodeJourney;
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
  private final ControlCodeJourneyHelper controlCodeJourneyHelper;

  @Inject
  public TechnicalNotesController(JourneyManager journeyManager,
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
    Optional<Boolean> technicalNotesApply = permissionsFinderDao.getControlCodeTechnicalNotesApply(controlCodeJourney);
    TechnicalNotesForm templateForm = new TechnicalNotesForm();
    templateForm.stillDescribesItems = technicalNotesApply.isPresent() ? technicalNotesApply.get().toString() : "";
    return frontendServiceClient.get(permissionsFinderDao.getSelectedControlCode(controlCodeJourney))
        .thenApplyAsync(result -> ok(technicalNotes.render(formFactory.form(TechnicalNotesForm.class).fill(templateForm),
            new TechnicalNotesDisplay(controlCodeJourney, result))), httpExecutionContext.current());
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

  public CompletionStage<Result> renderRelatedSoftwareControlsForm() {
    return renderForm(ControlCodeJourney.SOFTWARE_CONTROLS_RELATED_TO_A_PHYSICAL_GOOD);
  }

  private CompletionStage<Result> handleSubmit(ControlCodeJourney controlCodeJourney) {
    Form<TechnicalNotesForm> form = formFactory.form(TechnicalNotesForm.class).bindFromRequest();
    String controlCode = permissionsFinderDao.getSelectedControlCode(controlCodeJourney);
    return frontendServiceClient.get(controlCode)
        .thenApplyAsync(result -> {
          if (form.hasErrors()) {
            return completedFuture(ok(technicalNotes.render(form, new TechnicalNotesDisplay(controlCodeJourney, result))));
          }
          else {
            String stillDescribesItems = form.get().stillDescribesItems;
            if("true".equals(stillDescribesItems)) {
              permissionsFinderDao.saveControlCodeTechnicalNotesApply(controlCodeJourney, true);
              return controlCodeJourneyHelper.confirmedJourneyTransition(controlCodeJourney, controlCode);
            }
            else if ("false".equals(stillDescribesItems)) {
              permissionsFinderDao.saveControlCodeTechnicalNotesApply(controlCodeJourney, false);
              return controlCodeJourneyHelper.notApplicableJourneyTransition(controlCodeJourney);
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

  public CompletionStage<Result> handleRelatedSoftwareControlsSubmit() {
    return handleSubmit(ControlCodeJourney.SOFTWARE_CONTROLS_RELATED_TO_A_PHYSICAL_GOOD);
  }

  public static class TechnicalNotesForm {

    @Required(message = "You must answer this question")
    public String stillDescribesItems;

  }

}
