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
import models.controlcode.DecontrolsDisplay;
import play.data.Form;
import play.data.FormFactory;
import play.data.validation.Constraints.Required;
import play.libs.concurrent.HttpExecutionContext;
import play.mvc.Result;
import views.html.controlcode.decontrols;

import java.util.Optional;
import java.util.concurrent.CompletionStage;

public class DecontrolsController {

  private final JourneyManager journeyManager;
  private final FormFactory formFactory;
  private final PermissionsFinderDao permissionsFinderDao;
  private final HttpExecutionContext httpExecutionContext;
  private final FrontendServiceClient frontendServiceClient;

  @Inject
  public DecontrolsController(JourneyManager journeyManager,
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

  private CompletionStage<Result> renderWithForm(ControlCodeSubJourney controlCodeSubJourney, Form<DecontrolsForm> form) {
    return frontendServiceClient.get(permissionsFinderDao.getSelectedControlCode(controlCodeSubJourney))
        .thenApplyAsync(result -> ok(decontrols.render(form, new DecontrolsDisplay(result.getFrontendControlCode())))
            , httpExecutionContext.current());
  }

  public CompletionStage<Result> renderForm(String controlCodeVariantText, String goodsTypeText) {
    ControlCodeSubJourney controlCodeSubJourney = ControlCodeSubJourneyHelper.resolveUrlToSubJourneyAndUpdateContext(controlCodeVariantText, goodsTypeText);
    return renderFormInternal(controlCodeSubJourney);
  }

  private CompletionStage<Result> renderFormInternal(ControlCodeSubJourney controlCodeSubJourney) {
    Optional<Boolean> decontrolsApply = permissionsFinderDao.getControlCodeDecontrolsApply(controlCodeSubJourney);
    DecontrolsForm templateForm = new DecontrolsForm();
    templateForm.decontrolsDescribeItem = decontrolsApply.orElse(null);
    return renderWithForm(controlCodeSubJourney, formFactory.form(DecontrolsForm.class).fill(templateForm));
  }
  public CompletionStage<Result> handleSubmit() {
    ControlCodeSubJourney controlCodeSubJourney = ControlCodeSubJourneyHelper.resolveContextToSubJourney();
    return handleSubmitInternal(controlCodeSubJourney);
  }

  private CompletionStage<Result> handleSubmitInternal(ControlCodeSubJourney controlCodeSubJourney){
    Form<DecontrolsForm> form = formFactory.form(DecontrolsForm.class).bindFromRequest();
    if (form.hasErrors()) {
      return renderWithForm(controlCodeSubJourney, form);
    }
    else {
      Boolean decontrolsDescribeItem = form.get().decontrolsDescribeItem;
      if(decontrolsDescribeItem) {
        permissionsFinderDao.saveControlCodeDecontrolsApply(controlCodeSubJourney, true);
        return journeyManager.performTransition(Events.CONTROL_CODE_NOT_APPLICABLE);
      }
      else {
        permissionsFinderDao.saveControlCodeDecontrolsApply(controlCodeSubJourney, false);
        return journeyManager.performTransition(StandardEvents.NEXT);
      }
    }
  }

  public static class DecontrolsForm {

    @Required(message = "You must answer this question")
    public Boolean decontrolsDescribeItem;

  }

}
