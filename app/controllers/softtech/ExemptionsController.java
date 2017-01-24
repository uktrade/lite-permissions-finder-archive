package controllers.softtech;

import static java.util.concurrent.CompletableFuture.completedFuture;
import static play.mvc.Results.ok;

import com.google.inject.Inject;
import components.common.journey.JourneyManager;
import components.common.journey.StandardEvents;
import components.persistence.PermissionsFinderDao;
import models.GoodsType;
import models.softtech.ExemptionsDisplay;
import models.softtech.ExemptionQuestion;
import models.softtech.SoftTechCategory;
import play.data.Form;
import play.data.FormFactory;
import play.data.validation.Constraints.Required;
import play.mvc.Result;
import views.html.softtech.exemptions;

import java.util.Optional;
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

  private Result renderForm(ExemptionQuestion exemptionQuestion) {
    ExemptionsForm templateForm = new ExemptionsForm();
    Optional<Boolean> doExemptionsApply = permissionsFinderDao.getSoftwareExemptionQuestion(exemptionQuestion);
    templateForm.doExemptionsApply = doExemptionsApply.orElse(null);
    return ok(exemptions.render(formFactory.form(ExemptionsForm.class).fill(templateForm), new ExemptionsDisplay(exemptionQuestion)));
  }

  public Result renderFormQ1() {
    return renderForm(ExemptionQuestion.Q1);
  }

  public Result renderFormQ2() {
    return renderForm(ExemptionQuestion.Q2);
  }

  public Result renderFormQ3() {
    return renderForm(ExemptionQuestion.Q3);
  }

  private CompletionStage<Result> handleSubmit(ExemptionQuestion exemptionQuestion) {
    Form<ExemptionsForm> form = formFactory.form(ExemptionsForm.class).bindFromRequest();
    if (form.hasErrors()) {
      return completedFuture(ok(exemptions.render(form, new ExemptionsDisplay(exemptionQuestion))));
    }

    Boolean doExemptionsApply = form.get().doExemptionsApply;

    if (doExemptionsApply) {
      if (exemptionQuestion == ExemptionQuestion.Q2) {
        permissionsFinderDao.saveSoftTechCategory(GoodsType.SOFTWARE, SoftTechCategory.TELECOMS);
      }
      permissionsFinderDao.saveSoftwareExemptionQuestion(exemptionQuestion, true);
      return journeyManager.performTransition(StandardEvents.YES);
    }
    else {
      permissionsFinderDao.saveSoftwareExemptionQuestion(exemptionQuestion, false);
      return journeyManager.performTransition(StandardEvents.NO);
    }
  }

  public CompletionStage<Result> handleSubmitQ1(){
    return handleSubmit(ExemptionQuestion.Q1);
  }

  public CompletionStage<Result> handleSubmitQ2(){
    return handleSubmit(ExemptionQuestion.Q2);
  }

  public CompletionStage<Result> handleSubmitQ3(){
    return handleSubmit(ExemptionQuestion.Q3);
  }

  public static class ExemptionsForm {

    @Required(message = "You must answer this question")
    public Boolean doExemptionsApply;

  }
}
