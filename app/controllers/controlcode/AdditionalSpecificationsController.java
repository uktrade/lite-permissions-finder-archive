package controllers.controlcode;

import static java.util.concurrent.CompletableFuture.completedFuture;
import static play.mvc.Results.ok;

import com.google.inject.Inject;
import components.common.journey.JourneyManager;
import components.persistence.PermissionsFinderDao;
import components.services.controlcode.ControlCodeData;
import components.services.controlcode.FrontendServiceClient;
import components.services.controlcode.FrontendServiceResult;
import exceptions.FormStateException;
import journey.Events;
import journey.helpers.ControlCodeJourneyHelper;
import journey.helpers.SoftTechJourneyHelper;
import models.ControlCodeFlowStage;
import models.controlcode.AdditionalSpecificationsDisplay;
import models.controlcode.ControlCodeJourney;
import play.data.Form;
import play.data.FormFactory;
import play.data.validation.Constraints.Required;
import play.libs.concurrent.HttpExecutionContext;
import play.mvc.Result;
import views.html.controlcode.additionalSpecifications;

import java.util.Optional;
import java.util.concurrent.CompletionStage;
import java.util.function.Function;

public class AdditionalSpecificationsController {

  private final JourneyManager journeyManager;
  private final FormFactory formFactory;
  private final PermissionsFinderDao permissionsFinderDao;
  private final HttpExecutionContext httpExecutionContext;
  private final FrontendServiceClient frontendServiceClient;
  private final ControlCodeJourneyHelper controlCodeJourneyHelper;


  @Inject
  public AdditionalSpecificationsController(JourneyManager journeyManager,
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
    Optional<Boolean> additionalSpecificationsApply = permissionsFinderDao.getControlCodeAdditionalSpecificationsApply(controlCodeJourney);
    AdditionalSpecificationsForm templateForm = new AdditionalSpecificationsForm();
    templateForm.stillDescribesItems = additionalSpecificationsApply.isPresent()
        ? additionalSpecificationsApply.get().toString()
        : "";
    return frontendServiceClient.get(permissionsFinderDao.getSelectedControlCode(controlCodeJourney))
        .thenApplyAsync(result ->
            ok(additionalSpecifications.render(formFactory.form(AdditionalSpecificationsForm.class).fill(templateForm),
                new AdditionalSpecificationsDisplay(controlCodeJourney, result))), httpExecutionContext.current());
  }

  public CompletionStage<Result> renderSearchForm() {
    return renderForm(ControlCodeJourney.PHYSICAL_GOODS_SEARCH);
  }

  public CompletionStage<Result> renderSearchRelatedToForm(String goodsTypeText) {
    return SoftTechJourneyHelper.validateGoodsTypeTextThenContinue(goodsTypeText, this::renderForm);
  }

  public CompletionStage<Result> renderSoftwareControlsForm() {
    return renderForm(ControlCodeJourney.SOFTWARE_CONTROLS);
  }

  public CompletionStage<Result> renderRelatedSoftwareControlsForm() {
    return renderForm(ControlCodeJourney.SOFTWARE_CONTROLS_RELATED_TO_A_PHYSICAL_GOOD);
  }

  public CompletionStage<Result> renderSoftwareCatchallControlsForm() {
    return renderForm(ControlCodeJourney.SOFTWARE_CATCHALL_CONTROLS);
  }

  private CompletionStage<Result> handleSubmit(ControlCodeJourney controlCodeJourney) {
    Form<AdditionalSpecificationsForm> form = formFactory.form(AdditionalSpecificationsForm.class).bindFromRequest();
    String code = permissionsFinderDao.getSelectedControlCode(controlCodeJourney);
    return frontendServiceClient.get(code)
        .thenApplyAsync(result -> {
          if (form.hasErrors()) {
            return completedFuture(ok(additionalSpecifications.render(form,
                new AdditionalSpecificationsDisplay(controlCodeJourney, result))));
          }
          else {
            String stillDescribesItems = form.get().stillDescribesItems;
            if("true".equals(stillDescribesItems)) {
              permissionsFinderDao.saveControlCodeAdditionalSpecificationsApply(controlCodeJourney, true);
              return nextScreenTrue(controlCodeJourney, result);
            }
            else if ("false".equals(stillDescribesItems)) {
              permissionsFinderDao.saveControlCodeAdditionalSpecificationsApply(controlCodeJourney, false);
              return controlCodeJourneyHelper.notApplicableJourneyTransition(controlCodeJourney);
            }
            else {
              throw new FormStateException("Unhandled form state");
            }
          }
        }, httpExecutionContext.current()).thenCompose(Function.identity());
  }

  public CompletionStage<Result> handleSearchSubmit() {
    return handleSubmit(ControlCodeJourney.PHYSICAL_GOODS_SEARCH);
  }

  public CompletionStage<Result> handleSearchRelatedToSubmit(String goodsTypeText) {
    return SoftTechJourneyHelper.validateGoodsTypeTextThenContinue(goodsTypeText, this::handleSubmit);
  }

  public CompletionStage<Result> handleSoftwareControlsSubmit() {
    return handleSubmit(ControlCodeJourney.SOFTWARE_CONTROLS);
  }

  public CompletionStage<Result> handleRelatedSoftwareControlsSubmit() {
    return handleSubmit(ControlCodeJourney.SOFTWARE_CONTROLS_RELATED_TO_A_PHYSICAL_GOOD);
  }

  public CompletionStage<Result> handleSoftwareCatchallControlsSubmit() {
    return handleSubmit(ControlCodeJourney.SOFTWARE_CATCHALL_CONTROLS);
  }

  public CompletionStage<Result> nextScreenTrue(ControlCodeJourney controlCodeJourney, FrontendServiceResult frontendServiceResult) {
    ControlCodeData controlCodeData = frontendServiceResult.controlCodeData;
    if (controlCodeData.canShowDecontrols()) {
      return journeyManager.performTransition(Events.CONTROL_CODE_FLOW_NEXT, ControlCodeFlowStage.DECONTROLS);
    }
    else if (controlCodeData.canShowTechnicalNotes()) {
      return journeyManager.performTransition(Events.CONTROL_CODE_FLOW_NEXT, ControlCodeFlowStage.TECHNICAL_NOTES);
    }
    else {
      return controlCodeJourneyHelper.confirmedJourneyTransition(controlCodeJourney, controlCodeData.controlCode);
    }
  }

  public static class AdditionalSpecificationsForm {

    @Required(message = "You must answer this question")
    public String stillDescribesItems;

  }
}
