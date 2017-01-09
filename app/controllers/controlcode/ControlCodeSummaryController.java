package controllers.controlcode;

import com.google.inject.Inject;
import components.common.journey.JourneyManager;
import components.common.journey.StandardEvents;
import components.persistence.PermissionsFinderDao;
import components.services.controlcode.FrontendServiceClient;
import exceptions.FormStateException;
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

  private CompletionStage<Result> renderWithForm(ControlCodeSubJourney controlCodeSubJourney, Form<ControlCodeSummaryForm> form) {
    return frontendServiceClient.get(permissionsFinderDao.getSelectedControlCode(controlCodeSubJourney))
        .thenApplyAsync(frontendServiceResult ->
                ok(controlCodeSummary.render(form, new ControlCodeSummaryDisplay(controlCodeSubJourney, frontendServiceResult)))
            , httpExecutionContext.current());
  }

  public CompletionStage<Result> renderForm(String controlCodeVariantText, String goodsTypeText) {
   return ControlCodeSubJourneyHelper.resolveUrlToSubJourneyAndUpdateContext(controlCodeVariantText, goodsTypeText, this::renderFormInternal);
  }

  private CompletionStage<Result> renderFormInternal(ControlCodeSubJourney controlCodeSubJourney) {
    Optional<Boolean> controlCodeApplies = permissionsFinderDao.getControlCodeApplies(controlCodeSubJourney);
    ControlCodeSummaryForm templateForm = new ControlCodeSummaryForm();
    templateForm.couldDescribeItems = controlCodeApplies.isPresent() ? controlCodeApplies.get().toString() : "";
    return renderWithForm(controlCodeSubJourney, formFactory.form(ControlCodeSummaryForm.class).fill(templateForm));
  }

  public CompletionStage<Result> handleSubmit() {
    return ControlCodeSubJourneyHelper.resolveContextToSubJourney(this::handleSubmitInternal);
  }

  private CompletionStage<Result> handleSubmitInternal(ControlCodeSubJourney controlCodeSubJourney) {
    Form<ControlCodeSummaryForm> form = formFactory.form(ControlCodeSummaryForm.class).bindFromRequest();

    if (form.hasErrors()) {
      return renderWithForm(controlCodeSubJourney, form);
    }

    String couldDescribeItems = form.get().couldDescribeItems;
    if("true".equals(couldDescribeItems)) {
      permissionsFinderDao.saveControlCodeApplies(controlCodeSubJourney, true);
      return journeyManager.performTransition(StandardEvents.NEXT);
    }
    else if ("false".equals(couldDescribeItems)) {
      permissionsFinderDao.saveControlCodeApplies(controlCodeSubJourney, false);
      return journeyManager.performTransition(Events.CONTROL_CODE_NOT_APPLICABLE);
    }
    else {
      throw new FormStateException("Unhandled form state");
    }
  }

  public static class ControlCodeSummaryForm {

    @Required(message = "You must answer this question")
    public String couldDescribeItems;

  }

}