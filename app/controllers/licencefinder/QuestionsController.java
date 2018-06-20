package controllers.licencefinder;

import static java.util.concurrent.CompletableFuture.completedFuture;

import com.google.inject.Inject;
import components.auth.SamlAuthorizer;
import components.common.auth.SpireSAML2Client;
import components.persistence.LicenceFinderDao;
import components.services.LicenceFinderService;
import controllers.UserGuardAction;
import org.pac4j.play.java.Secure;
import play.data.Form;
import play.data.FormFactory;
import play.data.validation.Constraints.Required;
import play.mvc.Controller;
import play.mvc.Result;
import play.mvc.With;

import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;

@Secure(clients = SpireSAML2Client.CLIENT_NAME, authorizers = SamlAuthorizer.AUTHORIZER_NAME)
@With(UserGuardAction.class)
public class QuestionsController extends Controller {

  private final FormFactory formFactory;
  private final LicenceFinderDao licenceFinderDao;
  private final LicenceFinderService licenceFinderService;
  private final views.html.licencefinder.questions questions;

  @Inject
  public QuestionsController(FormFactory formFactory, LicenceFinderDao licenceFinderDao, LicenceFinderService licenceFinderService,
                             views.html.licencefinder.questions questions) {
    this.formFactory = formFactory;
    this.licenceFinderDao = licenceFinderDao;
    this.licenceFinderService = licenceFinderService;
    this.questions = questions;
  }

  /**
   * renderQuestionsForm
   */
  public CompletionStage<Result> renderQuestionsForm(String sessionId) {
    Optional<QuestionsForm> optForm = licenceFinderDao.getQuestionsForm(sessionId);
    return completedFuture(ok(questions.render(formFactory.form(QuestionsForm.class).fill(optForm.orElseGet(QuestionsForm::new)), sessionId)));
  }

  /**
   * handleQuestionsSubmit
   */
  public CompletionStage<Result> handleQuestionsSubmit(String sessionId) {
    Form<QuestionsForm> form = formFactory.form(QuestionsForm.class).bindFromRequest();
    if (form.hasErrors()) {
      return completedFuture(ok(questions.render(form, sessionId)));
    } else {
      licenceFinderDao.saveQuestionsForm(sessionId, form.get());

      // Take this opportunity in flow to save users CustomerId and SiteId
      licenceFinderService.persistCustomerAndSiteData(sessionId);

      return CompletableFuture.completedFuture(redirect(routes.ResultsController.renderResultsForm(sessionId)));
    }
  }

  public static class QuestionsForm {

    @Required(message = "Select whether you are exporting goods for or after repair or replacement")
    public Boolean forRepair;

    @Required(message = "Select whether you are exporting goods for or after exhibition or demonstration")
    public Boolean forExhibition;

    @Required(message = "Select whether your goods were manufactured before 1897, or are worth less than £30,000")
    public Boolean beforeOrLess;

  }
}

