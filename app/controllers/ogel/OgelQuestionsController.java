package controllers.ogel;

import static play.mvc.Results.ok;

import com.google.inject.Inject;
import components.persistence.PermissionsFinderDao;
import model.OgelActivityType;
import play.data.Form;
import play.data.FormFactory;
import play.data.validation.Constraints.Required;
import play.libs.concurrent.HttpExecutionContext;
import play.mvc.Result;
import views.html.ogel.ogelQuestions;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;
import java.util.function.Function;

public class OgelQuestionsController {

  private final FormFactory formFactory;

  private final PermissionsFinderDao dao;

  private final HttpExecutionContext ec;

  private final OgelResultsController ogelResultsController;

  @Inject
  public OgelQuestionsController(FormFactory formFactory,
                                 PermissionsFinderDao dao,
                                 HttpExecutionContext ec,
                                 OgelResultsController ogelResultsController) {
    this.formFactory = formFactory;
    this.dao = dao;
    this.ec = ec;
    this.ogelResultsController = ogelResultsController;
  }

  public Result renderForm() {
    dao.getPhysicalGoodControlCode();
    return ok(ogelQuestions.render(formFactory.form(OgelQuestionsForm.class)));
  }

  public CompletionStage<Result> handleSubmit() {
    return CompletableFuture.supplyAsync(() -> {
      Form<OgelQuestionsForm> form = formFactory.form(OgelQuestionsForm.class).bindFromRequest();
      if (form.hasErrors()) {
        return CompletableFuture.completedFuture(ok(ogelQuestions.render(form)));
      }
      else {
        dao.saveOgelActivityList(formToActivityTypes(form));
        return ogelResultsController.renderForm();
      }
    }, ec.current()).thenCompose(Function.identity());
  }

  public List<String> formToActivityTypes(Form<OgelQuestionsForm> form) {
    // TODO before1897upto35k currently unused
    // TODO account for TECH

    OgelQuestionsForm questionsForm = form.get();
    List<String> activityTypes = new ArrayList<>();

    if ("true".equals(questionsForm.toGovernment)) {
      activityTypes.add(OgelActivityType.MIL_GOV);
    }

    if ("true".equals(questionsForm.forRepairReplacement)) {
      activityTypes.add(OgelActivityType.REPAIR);
    }

    if ("true".equals(questionsForm.forExhibitionDemonstration)) {
      activityTypes.add(OgelActivityType.EXHIBITION);
    }

    // Always add these types
    activityTypes.add(OgelActivityType.MIL_ANY);
    activityTypes.add(OgelActivityType.DU_ANY);

    return activityTypes;
  }

  public static class OgelQuestionsForm {

    @Required(message = "You must answer this question")
    public String toGovernment;

    @Required(message = "You must answer this question")
    public String forRepairReplacement;

    @Required(message = "You must answer this question")
    public String forExhibitionDemonstration;

    @Required(message = "You must answer this question")
    public String before1897upto35k;

  }

}