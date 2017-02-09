package controllers.controlcode;

import com.google.inject.Inject;
import components.common.journey.JourneyManager;
import components.common.journey.StandardEvents;
import components.persistence.PermissionsFinderDao;
import components.services.controlcode.FrontendServiceClient;
import journey.Events;
import journey.helpers.ControlCodeSubJourneyHelper;
import models.controlcode.ControlCodeSubJourney;
import models.controlcode.ControlCodeSummaryDisplay;
import play.data.Form;
import play.data.FormFactory;
import play.data.validation.Constraints.Required;
import play.libs.concurrent.HttpExecutionContext;
import play.mvc.Controller;
import play.mvc.Result;
import views.html.controlcode.controlCodeSummary;

import java.util.Optional;
import java.util.concurrent.CompletionStage;

public class ControlCodeSummaryController extends Controller {

  private final JourneyManager journeyManager;
  private final FormFactory formFactory;
  private final PermissionsFinderDao permissionsFinderDao;
  private final HttpExecutionContext httpExecutionContext;
  private final FrontendServiceClient frontendServiceClient;

  @Inject
  public ControlCodeSummaryController(JourneyManager journeyManager,
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

  private CompletionStage<Result> renderWithForm(ControlCodeSubJourney controlCodeSubJourney, String controlCode, Form<ControlCodeSummaryForm> form) {
    return frontendServiceClient.get(controlCode)
        .thenApplyAsync(frontendServiceResult ->
                ok(controlCodeSummary.render(form, new ControlCodeSummaryDisplay(controlCodeSubJourney, frontendServiceResult.getFrontendControlCode())))
            , httpExecutionContext.current());
  }

  public CompletionStage<Result> renderForm(String controlCodeVariantText, String goodsTypeText) {
    ControlCodeSubJourney controlCodeSubJourney = ControlCodeSubJourneyHelper.resolveUrlToSubJourneyAndUpdateContext(controlCodeVariantText, goodsTypeText);
    return renderFormInternal(controlCodeSubJourney);
  }

  private CompletionStage<Result> renderFormInternal(ControlCodeSubJourney controlCodeSubJourney) {
    String controlCode = permissionsFinderDao.getSelectedControlCode(controlCodeSubJourney);
    Optional<Boolean> controlCodeApplies = permissionsFinderDao.getControlCodeApplies(controlCodeSubJourney);
    Optional<Boolean> showTechNotes = permissionsFinderDao.getShowTechNotes(controlCodeSubJourney, controlCode);
    ControlCodeSummaryForm templateForm = new ControlCodeSummaryForm();
    templateForm.couldDescribeItems = controlCodeApplies.orElse(null);
    templateForm.showTechNotes = showTechNotes.orElse(null);
    return renderWithForm(controlCodeSubJourney, controlCode, formFactory.form(ControlCodeSummaryForm.class).fill(templateForm));
  }

  public CompletionStage<Result> handleSubmit() {
    ControlCodeSubJourney controlCodeSubJourney = ControlCodeSubJourneyHelper.resolveContextToSubJourney();
    return handleSubmitInternal(controlCodeSubJourney);
  }

  private CompletionStage<Result> handleSubmitInternal(ControlCodeSubJourney controlCodeSubJourney) {
    Form<ControlCodeSummaryForm> form = formFactory.form(ControlCodeSummaryForm.class).bindFromRequest();
    String controlCode = permissionsFinderDao.getSelectedControlCode(controlCodeSubJourney);

    if (form.hasErrors()) {
      return renderWithForm(controlCodeSubJourney, controlCode, form);
    }

    Boolean couldDescribeItems = form.get().couldDescribeItems;
    Boolean showTechNotes = form.get().showTechNotes;

    if (showTechNotes != null) {
      permissionsFinderDao.saveShowTechNotes(controlCodeSubJourney, controlCode, showTechNotes);
    }

    if(couldDescribeItems) {
      permissionsFinderDao.saveControlCodeApplies(controlCodeSubJourney, true);
      return journeyManager.performTransition(StandardEvents.NEXT);
    }
    else {
      permissionsFinderDao.saveControlCodeApplies(controlCodeSubJourney, false);
      return journeyManager.performTransition(Events.CONTROL_CODE_NOT_APPLICABLE);
    }
  }

  public static class ControlCodeSummaryForm {

    @Required(message = "You must answer this question")
    public Boolean couldDescribeItems;

    public Boolean showTechNotes;

  }

}