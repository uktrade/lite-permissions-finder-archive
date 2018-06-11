package controllers.licencefinder;

import com.google.inject.Inject;
import components.auth.SamlAuthorizer;
import components.common.auth.SpireSAML2Client;
import components.common.cache.CountryProvider;
import components.common.state.ContextParamManager;
import components.common.transaction.TransactionManager;
import components.persistence.LicenceFinderDao;
import components.services.LicenceFinderService;
import components.services.OgelService;
import components.services.ogels.conditions.OgelConditionsServiceClient;
import models.view.QuestionView;
import models.view.RegisterResultView;
import org.pac4j.play.java.Secure;
import play.Logger;
import play.data.Form;
import play.data.FormFactory;
import play.data.validation.Constraints;
import play.libs.concurrent.HttpExecutionContext;
import play.mvc.Controller;
import play.mvc.Result;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.CompletionStage;
import java.util.function.Function;

import javax.inject.Named;

@Secure(clients = SpireSAML2Client.CLIENT_NAME, authorizers = SamlAuthorizer.AUTHORIZER_NAME)
public class RegisterToUseController extends Controller {

  private final FormFactory formFactory;
  private final LicenceFinderDao dao;
  private final TransactionManager transactionManager;
  private final CountryProvider countryProvider;
  private final OgelConditionsServiceClient conditionsClient;
  private final HttpExecutionContext httpContext;
  private final ContextParamManager contextParamManager;
  private final String dashboardUrl;
  private final OgelService ogelService;
  private final LicenceFinderService licenceFinderService;
  private final views.html.licencefinder.registerResult registerResult;
  private final views.html.licencefinder.registerToUse registerToUse;
  private final views.html.licencefinder.registerWait registerWait;

  private final String CONTROL_CODE_QUESTION = "What Control list entry describes your goods?";
  private final String GOODS_GOING_QUESTION = "Where are your goods going?";
  private final String FIRST_COUNTRY = "First country or territory that will receive the items";
  private final String REPAIR_QUESTION = "Are you exporting goods for or after repair or replacement?";
  private final String EXHIBITION_QUESTION = "Are you exporting goods for or after exhibition or demonstration?";
  private final String BEFORE_OR_LESS_QUESTION = "Were your goods manufactured before 1897, and worth less than £30,000?";

  private final String YES = "Yes";
  private final String NO = "No";

  @Inject
  public RegisterToUseController(TransactionManager transactionManager, FormFactory formFactory,
                                 HttpExecutionContext httpContext,
                                 LicenceFinderDao dao, @Named("countryProviderExport") CountryProvider countryProvider,
                                 OgelConditionsServiceClient conditionsClient,
                                 @com.google.inject.name.Named("dashboardUrl") String dashboardUrl,
                                 OgelService ogelService, LicenceFinderService licenceFinderService,
                                 views.html.licencefinder.registerResult registerResult,
                                 views.html.licencefinder.registerToUse registerToUse,
                                 views.html.licencefinder.registerWait registerWait, ContextParamManager contextParamManager) {
    this.transactionManager = transactionManager;
    this.formFactory = formFactory;
    this.httpContext = httpContext;
    this.dao = dao;
    this.countryProvider = countryProvider;
    this.conditionsClient = conditionsClient;
    this.dashboardUrl = dashboardUrl;
    this.ogelService = ogelService;
    this.licenceFinderService = licenceFinderService;
    this.registerResult = registerResult;
    this.registerToUse = registerToUse;
    this.registerWait = registerWait;
    this.contextParamManager = contextParamManager;
  }

  public Result renderAwaitResult() {

    Logger.info("renderAwaitResult");
    String transactionId = transactionManager.getTransactionId();

    return ok(registerWait.render(false));
    /*
    SubmissionStatus status = submissionService.getSubmissionStatus(transactionId);

    switch (status) {
      case SUBMITTED:
        boolean showLongRunningPrompt = submissionService.getSecondsSinceRegistrationSubmission(transactionId) >= 15;
        return ok(registerWait.render(showLongRunningPrompt));
      case COMPLETED:
        return handleSubmissionCompleted(transactionId);
      case FAILED:
      default:
        return ok(registrationRejection.render(submissionService.getCallbackResult(transactionId).orElse(null)));
    }
    */
  }

  private Result handleSubmissionCompleted(String transactionId) {
    Logger.info("handleSubmissionCompleted");
    return ok(registerWait.render(false));
    /*
    CallbackView.Result result = submissionService.getCallbackResult(transactionId).orElse(CallbackView.Result.FAILED);
    switch (result) {
      case SUCCESS:
        return ok(registrationConfirmation.render(submissionService.getRegistrationRef(transactionId)));
      case PERMISSION_DENIED:
        return ok(permissionDenied.render());
      default:
        return ok(registrationRejection.render(result));
    }
    */
  }

  /**
   * Handles the RegistrationInterval form submission
   */
  public CompletionStage<Result> handleRegistrationProcessed() {
    return contextParamManager.addParamsAndRedirect(routes.RegisterToUseController.renderAwaitResult());
  }

  /************************************************************************************************
   * 'RegisterToUse' page
   *******************************************************************************************/
  public CompletionStage<Result> renderRegisterToUseForm() {
    return renderWithRegisterToUseForm(formFactory.form(RegisterToUseForm.class));
  }

  public CompletionStage<Result> handleRegisterToUseSubmit() {
    Form<RegisterToUseForm> form = formFactory.form(RegisterToUseForm.class).bindFromRequest();
    if (form.hasErrors()) {
      return renderWithRegisterToUseForm(form);
    }

    String transactionId = transactionManager.getTransactionId();
    licenceFinderService.registerOgel(transactionId);

    /*
    return licenceFinderService.registerOgel(transactionId).thenApplyAsync(conditionsResult ->
            ogelService.get(dao.getOgelId())
                .thenApplyAsync(ogelFullView -> {
                  RegisterResultView view = new RegisterResultView("Your registration for Open general export licence (" + ogelFullView.getName() + ") has been received. Please go to your dashboard for confirmation.");
                  Optional<String> regRef = licenceFinderService.getRegistrationReference(transactionId);
                  if (regRef.isPresent()) {
                    view = new RegisterResultView("You have successfully registered to use Open general export licence (" + ogelFullView.getName() + ") ", regRef.get());
                  }

                  return ok(registerResult.render(view, ogelFullView, dashboardUrl));
                }, httpContext.current())
        , httpContext.current())
        .thenCompose(Function.identity());
        */

    Optional<String> regRef = licenceFinderService.getRegistrationReference(transactionId);
    if (regRef.isPresent()) {
      return ogelService.get(dao.getOgelId())
          .thenApplyAsync(ogelFullView -> {
            RegisterResultView view = new RegisterResultView("You have successfully registered to use Open general export licence (" + ogelFullView.getName() + ") ", regRef.get());
            return ok(registerResult.render(view, ogelFullView, dashboardUrl));
          }, httpContext.current());
    }

    return contextParamManager.addParamsAndRedirect(routes.RegisterToUseController.renderAwaitResult());
  }

  /**
   * Private methods
   */
  private CompletionStage<Result> renderWithRegisterToUseForm(Form<RegisterToUseForm> form) {
    String ogelId = dao.getOgelId();
    String controlCode = dao.getControlCode();

    return conditionsClient.get(ogelId, controlCode)
        .thenApplyAsync(conditionsResult ->
                ogelService.get(ogelId)
                    .thenApplyAsync(ogelResult -> ok(registerToUse.render(form, ogelResult, controlCode, true, getLicenceFinderAnswers())), httpContext.current())
            , httpContext.current())
        .thenCompose(Function.identity());
  }

  private List<QuestionView> getLicenceFinderAnswers() {
    List<QuestionView> views = new ArrayList<>();

    views.add(new QuestionView(CONTROL_CODE_QUESTION, dao.getControlCode()));
    dao.getTradeType().ifPresent(tradeType -> views.add(new QuestionView(GOODS_GOING_QUESTION, tradeType.getTitle())));
    views.add(new QuestionView(DestinationController.DESTINATION_QUESTION, countryProvider.getCountry(dao.getDestinationCountry()).getCountryName()));
    dao.getMultipleCountries().ifPresent(aBoolean -> views.add(new QuestionView(DestinationController.DESTINATION_MULTIPLE_QUESTION, aBoolean ? "Yes" : "No")));

    Optional<Boolean> optMultipleCountries = dao.getMultipleCountries();
    if (optMultipleCountries.isPresent()) {
      boolean isMultiple = optMultipleCountries.get();
      if (isMultiple) {
        views.add(new QuestionView(FIRST_COUNTRY, countryProvider.getCountry(dao.getFirstConsigneeCountry()).getCountryName()));
      }
    }

    Optional<QuestionsController.QuestionsForm> optForm = dao.getQuestionsForm();
    if (optForm.isPresent()) {
      QuestionsController.QuestionsForm form = optForm.get();
      views.add(new QuestionView(REPAIR_QUESTION, form.forRepair ? YES : NO));
      views.add(new QuestionView(EXHIBITION_QUESTION, form.forExhibition ? YES : NO));
      views.add(new QuestionView(BEFORE_OR_LESS_QUESTION, form.beforeOrLess ? YES : NO));
    }
    return views;
  }

  public static class RegisterToUseForm {

    @Constraints.Required(message = "Confirm you have read the OGEL and its criteria in full.")
    public Boolean confirmRead;

    @Constraints.Required(message = "Confirm your export complies with the OGEL criteria stated.")
    public Boolean confirmComplies;

  }

}
