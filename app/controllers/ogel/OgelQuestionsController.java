package controllers.ogel;

import static play.mvc.Results.ok;

import com.google.inject.Inject;
import components.persistence.PermissionsFinderDao;
import play.data.Form;
import play.data.FormFactory;
import play.data.validation.Constraints.Required;
import play.libs.concurrent.HttpExecutionContext;
import play.mvc.Result;
import views.html.ogel.ogelQuestions;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;

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
    return ok(ogelQuestions.render(formFactory.form(OgelQuestionsForm.class)));
  }

  public CompletionStage<Result> handleSubmit() {

    Form<OgelQuestionsForm> form = formFactory.form(OgelQuestionsForm.class).bindFromRequest();

    CompletionStage<Void> daoStage = CompletableFuture.runAsync(() -> {
      if (!form.hasErrors()) {
        dao.saveOgelQuestionMap(form.data());
      }
    }, ec.current());

    CompletionStage<Result> resultStage = daoStage.thenApply(aVoid-> {
      if (form.hasErrors()) {
        return ok(ogelQuestions.render(form));
      }
      else {
        return ogelResultsController.renderForm();
      }
    });

    return resultStage;
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