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
import journey.helpers.ControlCodeSubJourneyHelper;
import models.ControlCodeFlowStage;
import models.controlcode.AdditionalSpecificationsDisplay;
import models.controlcode.ControlCodeSubJourney;
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
  private final ControlCodeSubJourneyHelper controlCodeSubJourneyHelper;


  @Inject
  public AdditionalSpecificationsController(JourneyManager journeyManager,
                                            FormFactory formFactory,
                                            PermissionsFinderDao permissionsFinderDao,
                                            HttpExecutionContext httpExecutionContext,
                                            FrontendServiceClient frontendServiceClient,
                                            ControlCodeSubJourneyHelper controlCodeSubJourneyHelper) {
    this.journeyManager = journeyManager;
    this.formFactory = formFactory;
    this.permissionsFinderDao = permissionsFinderDao;
    this.httpExecutionContext = httpExecutionContext;
    this.frontendServiceClient = frontendServiceClient;
    this.controlCodeSubJourneyHelper = controlCodeSubJourneyHelper;
  }

  public CompletionStage<Result> renderForm(String controlCodeVariantText, String goodsTypeText) {
    return ControlCodeSubJourneyHelper.resolveUrlToSubJourneyAndUpdateContext(controlCodeVariantText, goodsTypeText, this::renderFormInternal);
  }

  private CompletionStage<Result> renderFormInternal(ControlCodeSubJourney controlCodeSubJourney) {
    Optional<Boolean> additionalSpecificationsApply = permissionsFinderDao.getControlCodeAdditionalSpecificationsApply(controlCodeSubJourney);
    AdditionalSpecificationsForm templateForm = new AdditionalSpecificationsForm();
    templateForm.stillDescribesItems = additionalSpecificationsApply.isPresent()
        ? additionalSpecificationsApply.get().toString()
        : "";
    return frontendServiceClient.get(permissionsFinderDao.getSelectedControlCode(controlCodeSubJourney))
        .thenApplyAsync(result ->
            ok(additionalSpecifications.render(formFactory.form(AdditionalSpecificationsForm.class).fill(templateForm),
                new AdditionalSpecificationsDisplay(controlCodeSubJourney, result))), httpExecutionContext.current());
  }

  public CompletionStage<Result> renderSearchRelatedToForm(String goodsTypeText) {
    return ControlCodeSubJourneyHelper.getSearchRelatedToPhysicalGoodsResult(goodsTypeText, this::renderFormInternal);
  }

  public CompletionStage<Result> renderControlsForm(String goodsTypeText) {
    return ControlCodeSubJourneyHelper.getControlsResult(goodsTypeText, this::renderFormInternal);
  }

  public CompletionStage<Result> renderRelatedControlsForm(String goodsTypeText) {
    return ControlCodeSubJourneyHelper.getRelatedControlsResult(goodsTypeText, this::renderFormInternal);
  }

  public CompletionStage<Result> renderCatchallControlsForm(String goodsTypeText) {
    return ControlCodeSubJourneyHelper.getCatchAllControlsResult(goodsTypeText, this::renderFormInternal);
  }

  public CompletionStage<Result> handleSubmit() {
    return ControlCodeSubJourneyHelper.resolveContextToSubJourney(this::handleSubmitInternal);
  }

  private CompletionStage<Result> handleSubmitInternal(ControlCodeSubJourney controlCodeSubJourney) {
    Form<AdditionalSpecificationsForm> form = formFactory.form(AdditionalSpecificationsForm.class).bindFromRequest();
    String code = permissionsFinderDao.getSelectedControlCode(controlCodeSubJourney);
    return frontendServiceClient.get(code)
        .thenApplyAsync(result -> {
          if (form.hasErrors()) {
            return completedFuture(ok(additionalSpecifications.render(form,
                new AdditionalSpecificationsDisplay(controlCodeSubJourney, result))));
          }
          else {
            String stillDescribesItems = form.get().stillDescribesItems;
            if("true".equals(stillDescribesItems)) {
              permissionsFinderDao.saveControlCodeAdditionalSpecificationsApply(controlCodeSubJourney, true);
              return nextScreenTrue(controlCodeSubJourney, result);
            }
            else if ("false".equals(stillDescribesItems)) {
              permissionsFinderDao.saveControlCodeAdditionalSpecificationsApply(controlCodeSubJourney, false);
              return controlCodeSubJourneyHelper.notApplicableJourneyTransition(controlCodeSubJourney);
            }
            else {
              throw new FormStateException("Unhandled form state");
            }
          }
        }, httpExecutionContext.current()).thenCompose(Function.identity());
  }

  public CompletionStage<Result> handleSearchRelatedToSubmit(String goodsTypeText) {
    return ControlCodeSubJourneyHelper.getSearchRelatedToPhysicalGoodsResult(goodsTypeText, this::handleSubmitInternal);
  }

  public CompletionStage<Result> handleControlsSubmit(String goodsTypeText) {
    return ControlCodeSubJourneyHelper.getControlsResult(goodsTypeText, this::handleSubmitInternal);
  }

  public CompletionStage<Result> handleRelatedControlsSubmit(String goodsTypeText) {
    return ControlCodeSubJourneyHelper.getRelatedControlsResult(goodsTypeText, this::handleSubmitInternal);
  }

  public CompletionStage<Result> handleCatchallControlsSubmit(String goodsTypeText) {
    return ControlCodeSubJourneyHelper.getCatchAllControlsResult(goodsTypeText, this::handleSubmitInternal);
  }

  public CompletionStage<Result> nextScreenTrue(ControlCodeSubJourney controlCodeSubJourney, FrontendServiceResult frontendServiceResult) {
    ControlCodeData controlCodeData = frontendServiceResult.controlCodeData;
    if (controlCodeData.canShowDecontrols()) {
      return journeyManager.performTransition(Events.CONTROL_CODE_FLOW_NEXT, ControlCodeFlowStage.DECONTROLS);
    }
    else if (controlCodeData.canShowTechnicalNotes()) {
      return journeyManager.performTransition(Events.CONTROL_CODE_FLOW_NEXT, ControlCodeFlowStage.TECHNICAL_NOTES);
    }
    else {
      return controlCodeSubJourneyHelper.confirmedJourneyTransition(controlCodeSubJourney, controlCodeData.controlCode);
    }
  }

  public static class AdditionalSpecificationsForm {

    @Required(message = "You must answer this question")
    public String stillDescribesItems;

  }
}
