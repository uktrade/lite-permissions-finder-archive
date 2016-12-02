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
import models.softtech.SoftTechCategory;
import models.softtech.SoftwareExemptionsFlow;
import play.data.Form;
import play.data.FormFactory;
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

  public Result renderForm() {
    ExemptionsForm templateForm = new ExemptionsForm();
    templateForm.doExemptionsApply = permissionsFinderDao.getDoExemptionsApply();
    return ok(exemptions.render(formFactory.form(ExemptionsForm.class).fill(templateForm)));
  }

  public CompletionStage<Result> handleSubmit() {
    Form<ExemptionsForm> form = formFactory.form(ExemptionsForm.class).bindFromRequest();
    if (form.hasErrors()) {
      return completedFuture(ok(exemptions.render(form)));
    }

    String doExemptionsApply = form.get().doExemptionsApply;

    if ("true".equals(doExemptionsApply)) {
      return journeyManager.performTransition(Events.SOFTWARE_EXEMPTIONS_FLOW, SoftwareExemptionsFlow.EXEMPTIONS_APPLY);
    }
    else if ("false".equals(doExemptionsApply)) {
      permissionsFinderDao.saveDoExemptionsApply(doExemptionsApply);
      // Expecting an export category at this stage of the journey
      ExportCategory exportCategory = permissionsFinderDao.getExportCategory().get();
      if (exportCategory == ExportCategory.MILITARY) {
        permissionsFinderDao.saveSoftTechCategory(GoodsType.SOFTWARE, SoftTechCategory.MILITARY);
        return softTechJourneyHelper.checkSoftwareControls(SoftTechCategory.MILITARY, true) // MILITARY_ONE_CONTROL will set DAO state
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

    public String doExemptionsApply;

  }
}
