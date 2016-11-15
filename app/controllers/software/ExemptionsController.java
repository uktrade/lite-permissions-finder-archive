package controllers.software;

import static java.util.concurrent.CompletableFuture.completedFuture;
import static play.mvc.Results.ok;

import com.google.inject.Inject;
import components.common.journey.JourneyManager;
import components.persistence.PermissionsFinderDao;
import exceptions.FormStateException;
import journey.Events;
import journey.SoftwareJourneyHelper;
import models.ExportCategory;
import models.software.ApplicableSoftwareControls;
import models.software.SoftwareCategory;
import models.software.SoftwareExemptionsFlow;
import play.data.Form;
import play.data.FormFactory;
import play.libs.concurrent.HttpExecutionContext;
import play.mvc.Result;
import views.html.software.exemptions;

import java.util.concurrent.CompletionStage;

public class ExemptionsController {

  private final JourneyManager journeyManager;
  private final FormFactory formFactory;
  private final PermissionsFinderDao permissionsFinderDao;
  private final SoftwareJourneyHelper softwareJourneyHelper;
  private final HttpExecutionContext httpExecutionContext;

  @Inject
  public ExemptionsController(FormFactory formFactory, PermissionsFinderDao permissionsFinderDao,
                              JourneyManager journeyManager, SoftwareJourneyHelper softwareJourneyHelper, HttpExecutionContext httpExecutionContext) {
    this.formFactory = formFactory;
    this.permissionsFinderDao = permissionsFinderDao;
    this.journeyManager = journeyManager;
    this.softwareJourneyHelper = softwareJourneyHelper;
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
        return softwareJourneyHelper.checkSoftwareControls(SoftwareCategory.MILITARY)
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

  private CompletionStage<Result> softwareExemptionsFlow(ApplicableSoftwareControls applicableSoftwareControls) {
    if (applicableSoftwareControls == ApplicableSoftwareControls.ZERO) {
      return journeyManager.performTransition(Events.SOFTWARE_EXEMPTIONS_FLOW, SoftwareExemptionsFlow.MILITARY_ZERO_CONTROLS);
    }
    else if (applicableSoftwareControls == ApplicableSoftwareControls.ONE) {
      return journeyManager.performTransition(Events.SOFTWARE_EXEMPTIONS_FLOW, SoftwareExemptionsFlow.MILITARY_ONE_CONTROL);
    }
    else if (applicableSoftwareControls == ApplicableSoftwareControls.GREATER_THAN_ONE) {
      return journeyManager.performTransition(Events.SOFTWARE_EXEMPTIONS_FLOW, SoftwareExemptionsFlow.MILITARY_GREATER_THAN_ONE_CONTROL);
    }
    else {
      throw new RuntimeException(String.format("Unexpected member of ApplicableSoftwareControls enum: \"%s\""
          , applicableSoftwareControls.toString()));
    }
  }


  public static class ExemptionsForm {

    public String doExemptionsApply;

  }
}
