package controllers.controlcode;

import static play.mvc.Results.ok;

import com.google.inject.Inject;
import components.common.journey.JourneyManager;
import components.common.journey.StandardEvents;
import components.persistence.PermissionsFinderDao;
import components.services.controlcode.FrontendServiceClient;
import exceptions.FormStateException;
import journey.Events;
import journey.helpers.ControlCodeSubJourneyHelper;
import models.controlcode.ControlCodeSubJourney;
import models.controlcode.TechnicalNotesDisplay;
import play.data.Form;
import play.data.FormFactory;
import play.data.validation.Constraints.Required;
import play.libs.concurrent.HttpExecutionContext;
import play.mvc.Result;
import views.html.controlcode.technicalNotes;

import java.util.Optional;
import java.util.concurrent.CompletionStage;

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

  private CompletionStage<Result> renderWithForm(ControlCodeSubJourney controlCodeSubJourney, Form<TechnicalNotesForm> form) {
    return frontendServiceClient.get(permissionsFinderDao.getSelectedControlCode(controlCodeSubJourney))
        .thenApplyAsync(result -> ok(technicalNotes.render(form, new TechnicalNotesDisplay(result)))
            , httpExecutionContext.current());
  }

  public CompletionStage<Result> renderForm(String controlCodeVariantText, String goodsTypeText) {
    return ControlCodeSubJourneyHelper.resolveUrlToSubJourneyAndUpdateContext(controlCodeVariantText, goodsTypeText, this::renderFormInternal);
  }

  private CompletionStage<Result> renderFormInternal(ControlCodeSubJourney controlCodeSubJourney) {
    Optional<Boolean> technicalNotesApply = permissionsFinderDao.getControlCodeTechnicalNotesApply(controlCodeSubJourney);
    TechnicalNotesForm templateForm = new TechnicalNotesForm();
    templateForm.stillDescribesItems = technicalNotesApply.isPresent() ? technicalNotesApply.get().toString() : "";
    return renderWithForm(controlCodeSubJourney, formFactory.form(TechnicalNotesForm.class).fill(templateForm));
  }

  public CompletionStage<Result> handleSubmit() {
    return ControlCodeSubJourneyHelper.resolveContextToSubJourney(this::handleSubmitInternal);
  }

  private CompletionStage<Result> handleSubmitInternal(ControlCodeSubJourney controlCodeSubJourney) {
    Form<TechnicalNotesForm> form = formFactory.form(TechnicalNotesForm.class).bindFromRequest();
    if (form.hasErrors()) {
      return renderWithForm(controlCodeSubJourney, form);
    }
    else {
      String stillDescribesItems = form.get().stillDescribesItems;
      if("true".equals(stillDescribesItems)) {
        permissionsFinderDao.saveControlCodeTechnicalNotesApply(controlCodeSubJourney, true);
        return journeyManager.performTransition(StandardEvents.NEXT);
      }
      else if ("false".equals(stillDescribesItems)) {
        permissionsFinderDao.saveControlCodeTechnicalNotesApply(controlCodeSubJourney, false);
        return journeyManager.performTransition(Events.CONTROL_CODE_NOT_APPLICABLE);
      }
      else {
        throw new FormStateException("Unhandled form state");
      }
    }
  }

  public static class TechnicalNotesForm {

    @Required(message = "You must answer this question")
    public String stillDescribesItems;

  }

}
