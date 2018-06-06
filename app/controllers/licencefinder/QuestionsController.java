package controllers.licencefinder;

import static java.util.concurrent.CompletableFuture.completedFuture;

import com.google.inject.Inject;
import components.auth.SamlAuthorizer;
import components.common.auth.SpireSAML2Client;
import components.common.state.ContextParamManager;
import components.persistence.LicenceFinderDao;
import components.services.LicenceFinderService;
import org.pac4j.play.java.Secure;
import play.data.Form;
import play.data.FormFactory;
import play.data.validation.Constraints.Required;
import play.mvc.Controller;
import play.mvc.Result;

import java.util.Optional;
import java.util.concurrent.CompletionStage;

@Secure(clients = SpireSAML2Client.CLIENT_NAME, authorizers = SamlAuthorizer.AUTHORIZER_NAME)
public class QuestionsController extends Controller {

  private final FormFactory formFactory;
  private final LicenceFinderDao dao;
  private final LicenceFinderService licenceFinderService;
  private final ContextParamManager contextParam;
  private final views.html.licencefinder.questions questions;

  @Inject
  public QuestionsController(FormFactory formFactory, LicenceFinderDao dao, LicenceFinderService licenceFinderService,
                             ContextParamManager contextParam, views.html.licencefinder.questions questions) {
    this.formFactory = formFactory;
    this.dao = dao;
    this.licenceFinderService = licenceFinderService;
    this.contextParam = contextParam;
    this.questions = questions;
  }

  /************************************************************************************************
   * 'Questions' page
   *******************************************************************************************/
  public CompletionStage<Result> renderQuestionsForm() {
    Optional<QuestionsForm> optForm = dao.getQuestionsForm();
    return completedFuture(ok(questions.render(formFactory.form(QuestionsForm.class).fill(optForm.orElseGet(QuestionsForm::new)))));
  }

  public CompletionStage<Result> handleQuestionsSubmit() {
    Form<QuestionsForm> form = formFactory.form(QuestionsForm.class).bindFromRequest();
    if (form.hasErrors()) {
      return completedFuture(ok(questions.render(form)));
    } else {
      dao.saveQuestionsForm(form.get());

      // Take this opportunity in flow to save users CustomerId and SiteId
      licenceFinderService.persistCustomerAndSiteData();
      return contextParam.addParamsAndRedirect(routes.ResultsController.renderResultsForm());
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

