package controllers.softtech;

import static java.util.concurrent.CompletableFuture.completedFuture;
import static play.mvc.Results.ok;

import com.google.inject.Inject;
import components.common.journey.JourneyManager;
import components.common.journey.StandardEvents;
import components.persistence.PermissionsFinderDao;
import models.GoodsType;
import models.softtech.SoftwareExemptionsDisplay;
import models.softtech.SoftwareExemptionQuestion;
import models.softtech.SoftTechCategory;
import play.data.Form;
import play.data.FormFactory;
import play.data.validation.Constraints.Required;
import play.mvc.Result;
import views.html.softtech.softwareExemptions;

import java.util.Optional;
import java.util.concurrent.CompletionStage;

public class SoftwareExemptionsController {

  private final JourneyManager journeyManager;
  private final FormFactory formFactory;
  private final PermissionsFinderDao permissionsFinderDao;

  @Inject
  public SoftwareExemptionsController(FormFactory formFactory,
                                      PermissionsFinderDao permissionsFinderDao,
                                      JourneyManager journeyManager) {
    this.formFactory = formFactory;
    this.permissionsFinderDao = permissionsFinderDao;
    this.journeyManager = journeyManager;
  }

  private Result renderForm(SoftwareExemptionQuestion softwareExemptionQuestion) {
    ExemptionsForm templateForm = new ExemptionsForm();
    Optional<Boolean> doExemptionsApply = permissionsFinderDao.getSoftwareExemptionQuestion(softwareExemptionQuestion);
    templateForm.doExemptionsApply = doExemptionsApply.orElse(null);
    return ok(softwareExemptions.render(formFactory.form(ExemptionsForm.class).fill(templateForm), new SoftwareExemptionsDisplay(softwareExemptionQuestion)));
  }

  public Result renderFormQ1() {
    return renderForm(SoftwareExemptionQuestion.Q1);
  }

  public Result renderFormQ2() {
    return renderForm(SoftwareExemptionQuestion.Q2);
  }

  public Result renderFormQ3() {
    return renderForm(SoftwareExemptionQuestion.Q3);
  }

  private CompletionStage<Result> handleSubmit(SoftwareExemptionQuestion softwareExemptionQuestion) {
    Form<ExemptionsForm> form = formFactory.form(ExemptionsForm.class).bindFromRequest();
    if (form.hasErrors()) {
      return completedFuture(ok(softwareExemptions.render(form, new SoftwareExemptionsDisplay(softwareExemptionQuestion))));
    }

    Boolean doExemptionsApply = form.get().doExemptionsApply;

    if (doExemptionsApply) {
      if (softwareExemptionQuestion == SoftwareExemptionQuestion.Q2) {
        permissionsFinderDao.saveSoftTechCategory(GoodsType.SOFTWARE, SoftTechCategory.TELECOMS);
      }
      permissionsFinderDao.saveSoftwareExemptionQuestion(softwareExemptionQuestion, true);
      return journeyManager.performTransition(StandardEvents.YES);
    }
    else {
      permissionsFinderDao.saveSoftwareExemptionQuestion(softwareExemptionQuestion, false);
      return journeyManager.performTransition(StandardEvents.NO);
    }
  }

  public CompletionStage<Result> handleSubmitQ1(){
    return handleSubmit(SoftwareExemptionQuestion.Q1);
  }

  public CompletionStage<Result> handleSubmitQ2(){
    return handleSubmit(SoftwareExemptionQuestion.Q2);
  }

  public CompletionStage<Result> handleSubmitQ3(){
    return handleSubmit(SoftwareExemptionQuestion.Q3);
  }

  public static class ExemptionsForm {

    @Required(message = "Answer this question")
    public Boolean doExemptionsApply;

  }
}
