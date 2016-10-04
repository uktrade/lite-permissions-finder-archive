package controllers.ogel;

import static java.util.concurrent.CompletableFuture.completedFuture;
import static play.mvc.Results.ok;

import com.google.inject.Inject;
import components.common.journey.JourneyManager;
import components.persistence.PermissionsFinderDao;
import journey.Events;
import models.OgelActivityType;
import play.data.Form;
import play.data.FormFactory;
import play.data.validation.Constraints.Required;
import play.mvc.Result;
import views.html.ogel.ogelQuestions;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.CompletionStage;

public class OgelQuestionsController {

  private final FormFactory formFactory;
  private final PermissionsFinderDao permissionsFinderDao;
  private final JourneyManager journeyManager;

  @Inject
  public OgelQuestionsController(JourneyManager journeyManager,
                                 FormFactory formFactory,
                                 PermissionsFinderDao permissionsFinderDao) {
    this.journeyManager = journeyManager;
    this.formFactory = formFactory;
    this.permissionsFinderDao = permissionsFinderDao;
  }

  public Result renderForm() {
    Optional<OgelQuestionsForm> templateFormOptional = permissionsFinderDao.getOgelQuestionsForm();
    OgelQuestionsForm templateForm = templateFormOptional.isPresent() ? templateFormOptional.get() : new OgelQuestionsForm();
    return ok(ogelQuestions.render(formFactory.form(OgelQuestionsForm.class).fill(templateForm)));
  }

  public CompletionStage<Result> handleSubmit() {
    Form<OgelQuestionsForm> form = formFactory.form(OgelQuestionsForm.class).bindFromRequest();
    if (form.hasErrors()) {
      return completedFuture(ok(ogelQuestions.render(form)));
    }
    else {
      OgelQuestionsForm ogelQuestionsForm = form.get();
      permissionsFinderDao.saveOgelQuestionsForm(ogelQuestionsForm);
      return journeyManager.performTransition(Events.OGEL_QUESTIONS_ANSWERED);
    }
  }

  public static class OgelQuestionsForm {

    @Required(message = "You must answer this question")
    public String forRepairReplacement;

    @Required(message = "You must answer this question")
    public String forExhibitionDemonstration;

    @Required(message = "You must answer this question")
    public String before1897upto35k;

    public static List<String> formToActivityTypes(Optional<OgelQuestionsForm> ogelQuestionsFormOptional) {
      // TODO before1897upto35k currently unused
      // TODO account for TECH
      List<String> activityTypes = new ArrayList<>();

      if (ogelQuestionsFormOptional.isPresent()) {
        OgelQuestionsForm ogelQuestionsForm = ogelQuestionsFormOptional.get();
        if ("true".equals(ogelQuestionsForm.forRepairReplacement)) {
          activityTypes.add(OgelActivityType.REPAIR.value());
        }
        if ("true".equals(ogelQuestionsForm.forExhibitionDemonstration)) {
          activityTypes.add(OgelActivityType.EXHIBITION.value());
        }
        // Always add these types
        activityTypes.add(OgelActivityType.MIL_ANY.value());
        activityTypes.add(OgelActivityType.MIL_GOV.value());
        activityTypes.add(OgelActivityType.DU_ANY.value());
      }

      return activityTypes;
    }

  }

}