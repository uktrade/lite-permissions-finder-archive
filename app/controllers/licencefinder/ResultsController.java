package controllers.licencefinder;

import static java.util.concurrent.CompletableFuture.completedFuture;

import com.google.inject.Inject;
import components.auth.SamlAuthorizer;
import components.common.auth.SpireAuthManager;
import components.common.auth.SpireSAML2Client;
import components.common.cache.CountryProvider;
import components.common.state.ContextParamManager;
import components.persistence.LicenceFinderDao;
import components.services.LicenceFinderService;
import components.services.controlcode.frontend.FrontendServiceClient;
import components.services.ogels.applicable.ApplicableOgelServiceClient;
import org.pac4j.play.java.Secure;
import play.data.Form;
import play.data.FormFactory;
import play.data.validation.Constraints.Required;
import play.libs.concurrent.HttpExecutionContext;
import play.mvc.Controller;
import play.mvc.Result;

import java.util.concurrent.CompletionStage;

import javax.inject.Named;

@Secure(clients = SpireSAML2Client.CLIENT_NAME, authorizers = SamlAuthorizer.AUTHORIZER_NAME)
public class ResultsController extends Controller {

  private final FormFactory formFactory;
  private final LicenceFinderDao licenceFinderDao;
  private final CountryProvider countryProvider;
  private final HttpExecutionContext httpContext;
  private final FrontendServiceClient frontendClient;
  private final ApplicableOgelServiceClient applicableClient;
  private final ContextParamManager contextParam;
  private final views.html.licencefinder.results results;
  private final SpireAuthManager authManager;
  private final LicenceFinderService licenceFinderService;

  public static final String NONE_ABOVE_KEY = "NONE_ABOVE_KEY";

  @Inject
  public ResultsController(FormFactory formFactory,
                           HttpExecutionContext httpContext,
                           LicenceFinderDao licenceFinderDao, @Named("countryProviderExport") CountryProvider countryProvider,
                           FrontendServiceClient frontendClient,
                           ApplicableOgelServiceClient applicableClient, ContextParamManager contextParam,
                           views.html.licencefinder.results results,
                           SpireAuthManager authManager, LicenceFinderService licenceFinderService) {
    this.formFactory = formFactory;
    this.httpContext = httpContext;
    this.licenceFinderDao = licenceFinderDao;
    this.countryProvider = countryProvider;
    this.frontendClient = frontendClient;
    this.applicableClient = applicableClient;
    this.contextParam = contextParam;
    this.results = results;
    this.authManager = authManager;
    this.licenceFinderService = licenceFinderService;
  }

  /************************************************************************************************
   * 'Results' page
   *******************************************************************************************/
  public CompletionStage<Result> renderResultsForm() {
    return renderWithForm(formFactory.form(ResultsForm.class));
  }

  public CompletionStage<Result> handleResultsSubmit() {
    Form<ResultsForm> form = formFactory.form(ResultsForm.class).bindFromRequest();
    if (form.hasErrors()) {
      return renderWithForm(form);
    }
    String chosenOgel = form.get().chosenOgel;
    licenceFinderDao.saveOgelId(chosenOgel);

    // Return No licences available when 'None of the above' chosen
    if (chosenOgel.equals(NONE_ABOVE_KEY)) {
      String userId = authManager.getAuthInfoFromContext().getId();
      return completedFuture(ok(results.render(form, licenceFinderService.getNoResultsView(userId))));
    }

    return contextParam.addParamsAndRedirect(routes.RegisterToUseController.renderRegisterToUseForm());
  }

  private CompletionStage<Result> renderWithForm(Form<ResultsForm> form) {
    String userId = authManager.getAuthInfoFromContext().getId();
    return completedFuture(ok(results.render(form, licenceFinderService.getResultsView(userId))));
  }

  public static class ResultsForm {
    @Required(message = "You must choose from the list of results below")
    public String chosenOgel;
  }

}

