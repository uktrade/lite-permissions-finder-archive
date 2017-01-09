package controllers.softtech;

import static java.util.concurrent.CompletableFuture.completedFuture;
import static play.mvc.Results.ok;

import com.google.inject.Inject;
import components.common.journey.JourneyManager;
import components.common.journey.StandardEvents;
import components.persistence.PermissionsFinderDao;
import exceptions.FormStateException;
import models.GoodsType;
import models.softtech.ExemptionsDisplay;
import models.softtech.ExemptionsDisplay.ExemptionDisplayType;
import models.softtech.SoftTechCategory;
import play.data.Form;
import play.data.FormFactory;
import play.data.validation.Constraints.Required;
import play.mvc.Result;
import views.html.softtech.exemptions;

import java.util.concurrent.CompletionStage;

public class ExemptionsController {

  private final JourneyManager journeyManager;
  private final FormFactory formFactory;
  private final PermissionsFinderDao permissionsFinderDao;

  @Inject
  public ExemptionsController(FormFactory formFactory,
                              PermissionsFinderDao permissionsFinderDao,
                              JourneyManager journeyManager) {
    this.formFactory = formFactory;
    this.permissionsFinderDao = permissionsFinderDao;
    this.journeyManager = journeyManager;
  }

  private Result renderForm(ExemptionDisplayType exemptionDisplayType) {
    ExemptionsForm templateForm = new ExemptionsForm();
    if (exemptionDisplayType == ExemptionDisplayType.Q1) {
      templateForm.doExemptionsApply = permissionsFinderDao.getDoExemptionsApplyQ1();
    }
    else if (exemptionDisplayType == ExemptionDisplayType.Q2) {
      templateForm.doExemptionsApply = permissionsFinderDao.getDoExemptionsApplyQ2();
    }
    else {
      templateForm.doExemptionsApply = permissionsFinderDao.getDoExemptionsApplyQ3();
    }
    return ok(exemptions.render(formFactory.form(ExemptionsForm.class).fill(templateForm), new ExemptionsDisplay(exemptionDisplayType)));
  }

  public Result renderFormQ1() {
    return renderForm(ExemptionDisplayType.Q1);
  }

  public Result renderFormQ2() {
    return renderForm(ExemptionDisplayType.Q2);
  }

  public Result renderFormQ3() {
    return renderForm(ExemptionDisplayType.Q3);
  }

  private CompletionStage<Result> handleSubmit(ExemptionDisplayType exemptionDisplayType) {
    Form<ExemptionsForm> form = formFactory.form(ExemptionsForm.class).bindFromRequest();
    if (form.hasErrors()) {
      return completedFuture(ok(exemptions.render(form, new ExemptionsDisplay(exemptionDisplayType))));
    }

    String doExemptionsApply = form.get().doExemptionsApply;

    if ("true".equals(doExemptionsApply) || "false".equals(doExemptionsApply)) {

      if (exemptionDisplayType == ExemptionDisplayType.Q1) {
        permissionsFinderDao.saveDoExemptionsApplyQ1(doExemptionsApply);
      }
      else if (exemptionDisplayType == ExemptionDisplayType.Q2) {
        permissionsFinderDao.saveDoExemptionsApplyQ2(doExemptionsApply);
      }
      else {
        // exemptionDisplayType == ExemptionDisplayType.Q3
        permissionsFinderDao.saveDoExemptionsApplyQ3(doExemptionsApply);
      }

      if ("true".equals(doExemptionsApply)) {

        if (exemptionDisplayType == ExemptionDisplayType.Q2) {
          permissionsFinderDao.saveSoftTechCategory(GoodsType.SOFTWARE, SoftTechCategory.MILITARY);
        }

        return journeyManager.performTransition(StandardEvents.YES);
      }
      else {
        // "false".equals(doExemptionsApplyText)
        return journeyManager.performTransition(StandardEvents.NO);
      }
    }
    else {
      throw new FormStateException(String.format("Unknown value for doExemptionsApply: \"%s\"", doExemptionsApply));
    }
  }

  public CompletionStage<Result> handleSubmitQ1(){
    return handleSubmit(ExemptionDisplayType.Q1);
  }

  public CompletionStage<Result> handleSubmitQ2(){
    return handleSubmit(ExemptionDisplayType.Q2);
  }

  public CompletionStage<Result> handleSubmitQ3(){
    return handleSubmit(ExemptionDisplayType.Q3);
  }

  public static class ExemptionsForm {

    @Required(message = "You must answer this question")
    public String doExemptionsApply;

  }
}
