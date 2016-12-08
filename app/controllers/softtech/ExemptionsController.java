package controllers.softtech;

import static java.util.concurrent.CompletableFuture.completedFuture;
import static play.mvc.Results.ok;

import com.google.inject.Inject;
import components.common.journey.JourneyManager;
import components.persistence.PermissionsFinderDao;
import exceptions.FormStateException;
import journey.Events;
import journey.helpers.SoftTechJourneyHelper;
import models.ExportCategory;
import models.GoodsType;
import models.softtech.ApplicableSoftTechControls;
import models.softtech.ExemptionsDisplay;
import models.softtech.SoftTechCategory;
import models.softtech.SoftwareExemptionsFlow;
import play.data.Form;
import play.data.FormFactory;
import play.data.validation.Constraints.Required;
import play.libs.concurrent.HttpExecutionContext;
import play.mvc.Result;
import views.html.softtech.exemptions;

import java.util.concurrent.CompletionStage;

public class ExemptionsController {

  private final JourneyManager journeyManager;
  private final FormFactory formFactory;
  private final PermissionsFinderDao permissionsFinderDao;
  private final SoftTechJourneyHelper softTechJourneyHelper;
  private final HttpExecutionContext httpExecutionContext;

  @Inject
  public ExemptionsController(FormFactory formFactory, PermissionsFinderDao permissionsFinderDao,
                              JourneyManager journeyManager, SoftTechJourneyHelper softTechJourneyHelper, HttpExecutionContext httpExecutionContext) {
    this.formFactory = formFactory;
    this.permissionsFinderDao = permissionsFinderDao;
    this.journeyManager = journeyManager;
    this.softTechJourneyHelper = softTechJourneyHelper;
    this.httpExecutionContext = httpExecutionContext;
  }

  private Result renderForm(ExemptionsDisplay.ExemptionDisplayType exemptionDisplayType) {
    ExemptionsForm templateForm = new ExemptionsForm();
    if (exemptionDisplayType == ExemptionsDisplay.ExemptionDisplayType.Q1) {
      templateForm.doExemptionsApply = permissionsFinderDao.getDoExemptionsApplyQ1();
    }
    else {
      templateForm.doExemptionsApply = permissionsFinderDao.getDoExemptionsApplyQ2();
    }
    return ok(exemptions.render(formFactory.form(ExemptionsForm.class).fill(templateForm), new ExemptionsDisplay(exemptionDisplayType)));
  }

  public Result renderFormQ1() {
    return renderForm(ExemptionsDisplay.ExemptionDisplayType.Q1);
  }

  public Result renderFormQ2() {
    return renderForm(ExemptionsDisplay.ExemptionDisplayType.Q2);
  }

  private CompletionStage<Result> handleSubmit(ExemptionsDisplay.ExemptionDisplayType exemptionDisplayType) {
    Form<ExemptionsForm> form = formFactory.form(ExemptionsForm.class).bindFromRequest();
    if (form.hasErrors()) {
      return completedFuture(ok(exemptions.render(form, new ExemptionsDisplay(exemptionDisplayType))));
    }

    String doExemptionsApply = form.get().doExemptionsApply;

    if (exemptionDisplayType == ExemptionsDisplay.ExemptionDisplayType.Q1) {
      if ("true".equals(doExemptionsApply)) {
        permissionsFinderDao.saveDoExemptionsApplyQ1(doExemptionsApply);
        return journeyManager.performTransition(Events.SOFTWARE_EXEMPTIONS_FLOW, SoftwareExemptionsFlow.Q1_EXEMPTIONS_APPLY);
      }
      if ("false".equals(doExemptionsApply)) {
        permissionsFinderDao.saveDoExemptionsApplyQ1(doExemptionsApply);
        return journeyManager.performTransition(Events.SOFTWARE_EXEMPTIONS_FLOW, SoftwareExemptionsFlow.Q1_EXEMPTIONS_DO_NOT_APPLY);
      }
      else {
        throw new FormStateException(String.format("Unknown value for doExemptionsApply: \"%s\"", doExemptionsApply));
      }
    }
    else {
      if ("true".equals(doExemptionsApply)) {
        permissionsFinderDao.saveDoExemptionsApplyQ2(doExemptionsApply);
        return journeyManager.performTransition(Events.SOFTWARE_EXEMPTIONS_FLOW, SoftwareExemptionsFlow.Q1_AND_Q2_EXEMPTIONS_APPLY);
      }
      else if ("false".equals(doExemptionsApply)) {
        permissionsFinderDao.saveDoExemptionsApplyQ2(doExemptionsApply);
        // Expecting an export category at this stage of the journey
        ExportCategory exportCategory = permissionsFinderDao.getExportCategory().get();
        if (exportCategory == ExportCategory.MILITARY) {
          permissionsFinderDao.saveSoftTechCategory(GoodsType.SOFTWARE, SoftTechCategory.MILITARY);
          return softTechJourneyHelper.checkSoftTechControls(GoodsType.SOFTWARE, SoftTechCategory.MILITARY, true) // MILITARY_ONE_CONTROL will set DAO state
              .thenComposeAsync(this::softwareExemptionsFlow, httpExecutionContext.current());
        }
        else if (exportCategory == ExportCategory.DUAL_USE) {
          return journeyManager.performTransition(Events.SOFTWARE_EXEMPTIONS_FLOW, SoftwareExemptionsFlow.DUAL_USE);
        }
        else {
          throw new RuntimeException(String.format("Unexpected member of ExportCategory enum: \"%s\""
              , exportCategory.toString()));
        }
      }
      else {
        throw new FormStateException(String.format("Unknown value for doExemptionsApply: \"%s\"", doExemptionsApply));
      }
    }
  }

  public CompletionStage<Result> handleSubmitQ1(){
    return handleSubmit(ExemptionsDisplay.ExemptionDisplayType.Q1);
  }

  public CompletionStage<Result> handleSubmitQ2(){
    return handleSubmit(ExemptionsDisplay.ExemptionDisplayType.Q2);
  }

  private CompletionStage<Result> softwareExemptionsFlow(ApplicableSoftTechControls applicableSoftTechControls) {
    if (applicableSoftTechControls == ApplicableSoftTechControls.ZERO) {
      return journeyManager.performTransition(Events.SOFTWARE_EXEMPTIONS_FLOW, SoftwareExemptionsFlow.MILITARY_ZERO_CONTROLS);
    }
    else if (applicableSoftTechControls == ApplicableSoftTechControls.ONE) {
      return journeyManager.performTransition(Events.SOFTWARE_EXEMPTIONS_FLOW, SoftwareExemptionsFlow.MILITARY_ONE_CONTROL);
    }
    else if (applicableSoftTechControls == ApplicableSoftTechControls.GREATER_THAN_ONE) {
      return journeyManager.performTransition(Events.SOFTWARE_EXEMPTIONS_FLOW, SoftwareExemptionsFlow.MILITARY_GREATER_THAN_ONE_CONTROL);
    }
    else {
      throw new RuntimeException(String.format("Unexpected member of ApplicableSoftTechControls enum: \"%s\""
          , applicableSoftTechControls.toString()));
    }
  }


  public static class ExemptionsForm {

    @Required(message = "You must answer this question")
    public String doExemptionsApply;

  }
}
