package controllers.controlcode;

import static play.mvc.Results.ok;

import com.google.inject.Inject;
import components.common.journey.JourneyManager;
import components.persistence.PermissionsFinderDao;
import components.services.controlcode.FrontendServiceClient;
import components.services.controlcode.FrontendServiceResult;
import exceptions.FormStateException;
import journey.Events;
import journey.helpers.SoftwareJourneyHelper;
import models.ControlCodeFlowStage;
import models.controlcode.ControlCodeJourney;
import models.controlcode.NotApplicableDisplay;
import models.software.ApplicableSoftwareControls;
import models.software.SoftwareCategory;
import models.software.SoftwareControlsNotApplicableFlow;
import play.data.Form;
import play.data.FormFactory;
import play.libs.concurrent.HttpExecutionContext;
import play.mvc.Result;
import views.html.controlcode.notApplicable;

import java.util.concurrent.CompletionStage;
import java.util.function.Function;

public class NotApplicableController {

  private final FormFactory formFactory;
  private final JourneyManager journeyManager;
  private final PermissionsFinderDao permissionsFinderDao;
  private final HttpExecutionContext httpExecutionContext;
  private final FrontendServiceClient frontendServiceClient;
  private final SoftwareJourneyHelper softwareJourneyHelper;

  @Inject
  public NotApplicableController(FormFactory formFactory,
                                 JourneyManager journeyManager,
                                 PermissionsFinderDao permissionsFinderDao,
                                 HttpExecutionContext httpExecutionContext,
                                 FrontendServiceClient frontendServiceClient,
                                 SoftwareJourneyHelper softwareJourneyHelper) {
    this.formFactory = formFactory;
    this.journeyManager = journeyManager;
    this.permissionsFinderDao = permissionsFinderDao;
    this.httpExecutionContext = httpExecutionContext;
    this.frontendServiceClient = frontendServiceClient;
    this.softwareJourneyHelper = softwareJourneyHelper;
  }

  private CompletionStage<Result> renderForm(ControlCodeJourney controlCodeJourney, String showExtendedContent) {
    if (controlCodeJourney == ControlCodeJourney.PHYSICAL_GOODS_SEARCH ||
        controlCodeJourney == ControlCodeJourney.PHYSICAL_GOODS_SEARCH_RELATED_TO_SOFTWARE) {
      return frontendServiceClient
          .get(permissionsFinderDao.getSelectedControlCode(controlCodeJourney))
          .thenApplyAsync(result ->
                  ok(notApplicable.render(new NotApplicableDisplay(controlCodeJourney,
                      formFactory.form(NotApplicableForm.class),
                      result.controlCodeData.alias,
                      Boolean.parseBoolean(showExtendedContent))))
              , httpExecutionContext.current());
    }
    else if (controlCodeJourney == ControlCodeJourney.SOFTWARE_CONTROLS) {
      // If on the software control journey, check the amount of applicable controls. This feeds into the display logic
      SoftwareCategory softwareCategory = permissionsFinderDao.getSoftwareCategory().get();
      String selectedControlCode = permissionsFinderDao.getSelectedControlCode(controlCodeJourney);
      CompletionStage<FrontendServiceResult> frontendStage = frontendServiceClient.get(selectedControlCode);
      return softwareJourneyHelper.checkSoftwareControls(softwareCategory)
          .thenCombineAsync(frontendStage, (controls, result) -> ok(
              notApplicable.render(
                  new NotApplicableDisplay(controlCodeJourney, formFactory.form(NotApplicableForm.class),
                      result.controlCodeData.alias, Boolean.parseBoolean(showExtendedContent), controls))
          ), httpExecutionContext.current());
    }
    else {
      throw new RuntimeException(String.format("Unexpected member of ControlCodeJourney enum: \"%s\""
          , controlCodeJourney.toString()));
    }
  }

  public CompletionStage<Result> renderForm(String showExtendedContent) {
    return renderForm(ControlCodeJourney.PHYSICAL_GOODS_SEARCH, showExtendedContent);
  }

  public CompletionStage<Result> renderRelatedToSoftwareForm(String showExtendedContent) {
    return renderForm(ControlCodeJourney.PHYSICAL_GOODS_SEARCH_RELATED_TO_SOFTWARE, showExtendedContent);
  }

  public CompletionStage<Result> renderSoftwareControlsForm(String showExtendedContent) {
    return renderForm(ControlCodeJourney.SOFTWARE_CONTROLS, showExtendedContent);
  }

  public CompletionStage<Result> renderRelatedSoftwareControlsForm(String showExtendedContent) {
    return renderForm(ControlCodeJourney.SOFTWARE_CONTROLS_RELATED_TO_A_PHYSICAL_GOOD, showExtendedContent);
  }

  private CompletionStage<Result> handleSubmit(ControlCodeJourney controlCodeJourney) {
    Form<NotApplicableForm> form = formFactory.form(NotApplicableForm.class).bindFromRequest();
    if (!form.hasErrors()) {
      String action = form.get().action;
      if (controlCodeJourney == ControlCodeJourney.PHYSICAL_GOODS_SEARCH
          || controlCodeJourney == ControlCodeJourney.PHYSICAL_GOODS_SEARCH_RELATED_TO_SOFTWARE) {
        if ("backToSearch".equals(action)) {
          return journeyManager.performTransition(Events.CONTROL_CODE_FLOW_NEXT, ControlCodeFlowStage.BACK_TO_SEARCH);
        }
        else if ("backToSearchResults".equals(action)) {
          return journeyManager.performTransition(Events.CONTROL_CODE_FLOW_NEXT, ControlCodeFlowStage.BACK_TO_SEARCH_RESULTS);
        }
        else {
          throw new FormStateException("Unknown value for action: \"" + action + "\"");
        }
      }
      else if (controlCodeJourney == ControlCodeJourney.SOFTWARE_CONTROLS) {
        // A different action is expected for each valid member of ApplicableSoftwareControls
        SoftwareCategory softwareCategory = permissionsFinderDao.getSoftwareCategory().get();
        return softwareJourneyHelper.checkSoftwareControls(softwareCategory)
            .thenApplyAsync(controls -> {
              if (controls == ApplicableSoftwareControls.ONE) {
                if ("continue".equals(action)) {
                  return journeyManager.performTransition(Events.CONTROL_CODE_SOFTWARE_CONTROLS_NOT_APPLICABLE_FLOW,
                      SoftwareControlsNotApplicableFlow.CONTINUE_NO_CONTROLS);
                }
                else {
                  throw new FormStateException("Unknown value for action: \"" + action + "\"");
                }
              }
              else if (controls == ApplicableSoftwareControls.GREATER_THAN_ONE) {
                if ("returnToControls".equals(action)) {
                  return journeyManager.performTransition(Events.CONTROL_CODE_SOFTWARE_CONTROLS_NOT_APPLICABLE_FLOW,
                      SoftwareControlsNotApplicableFlow.RETURN_TO_SOFTWARE_CONTROLS);
                }
                else {
                  throw new FormStateException("Unknown value for action: \"" + action + "\"");
                }
              }
              else {
                throw new RuntimeException(String.format("Unexpected member of ApplicableSoftwareControls enum: \"%s\""
                    , controls.toString()));
              }
            }, httpExecutionContext.current()).thenCompose(Function.identity());
      }
      else {
        throw new RuntimeException(String.format("Unexpected member of ControlCodeJourney enum: \"%s\""
            , controlCodeJourney.toString()));
      }
    }
    throw new FormStateException("Unhandled form state");
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

  public static class NotApplicableForm {

    public String action;

  }
}
