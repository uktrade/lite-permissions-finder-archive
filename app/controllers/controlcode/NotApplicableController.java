package controllers.controlcode;

import static play.mvc.Results.ok;

import com.google.inject.Inject;
import components.common.journey.JourneyManager;
import components.persistence.PermissionsFinderDao;
import components.services.controlcode.FrontendServiceClient;
import exceptions.FormStateException;
import journey.Events;
import models.ControlCodeFlowStage;
import play.data.Form;
import play.data.FormFactory;
import play.libs.concurrent.HttpExecutionContext;
import play.mvc.Result;
import views.html.controlcode.notApplicable;

import java.util.concurrent.CompletionStage;

public class NotApplicableController {

  private final FormFactory formFactory;
  private final JourneyManager journeyManager;
  private final PermissionsFinderDao permissionsFinderDao;
  private final HttpExecutionContext httpExecutionContext;
  private final FrontendServiceClient frontendServiceClient;

  @Inject
  public NotApplicableController(FormFactory formFactory, JourneyManager journeyManager, PermissionsFinderDao permissionsFinderDao, HttpExecutionContext httpExecutionContext, FrontendServiceClient frontendServiceClient) {
    this.formFactory = formFactory;
    this.journeyManager = journeyManager;
    this.permissionsFinderDao = permissionsFinderDao;
    this.httpExecutionContext = httpExecutionContext;
    this.frontendServiceClient = frontendServiceClient;
  }

  public CompletionStage<Result> renderForm(String showExtendedContent) {
    return frontendServiceClient
        .get(permissionsFinderDao.getPhysicalGoodControlCode())
        .thenApplyAsync(result ->
            ok(notApplicable.render(formFactory.form(NotApplicableForm.class), result.controlCodeData.alias,
                Boolean.parseBoolean(showExtendedContent)))
            , httpExecutionContext.current());
  }

  public CompletionStage<Result> renderRelatedToSoftwareForm(String showExtendedContent) {
    return renderForm(showExtendedContent);
  }

  public CompletionStage<Result> handleSubmit() {
    Form<NotApplicableForm> form = formFactory.form(NotApplicableForm.class).bindFromRequest();
    if (!form.hasErrors()) {
      String action = form.get().action;
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
    throw new FormStateException("Unhandled form state");
  }

  public static class NotApplicableForm {

    public String action;

  }
}
