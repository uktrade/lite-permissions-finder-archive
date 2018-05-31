package controllers.licencefinder;

import static java.util.concurrent.CompletableFuture.completedFuture;

import com.google.inject.Inject;
import components.auth.SamlAuthorizer;
import components.client.OgelService;
import components.common.auth.SpireSAML2Client;
import components.common.cache.CountryProvider;
import components.common.transaction.TransactionManager;
import components.persistence.LicenceFinderDao;
import components.services.LicenceFinderService;
import components.services.controlcode.frontend.FrontendServiceClient;
import components.services.controlcode.frontend.FrontendServiceResult;
import components.services.ogels.applicable.ApplicableOgelServiceClient;
import components.services.ogels.conditions.OgelConditionsServiceClient;
import exceptions.FormStateException;
import models.OgelActivityType;
import models.TradeType;
import models.ogel.OgelResultsDisplay;
import models.view.QuestionView;
import models.view.RegisterResultView;
import org.apache.commons.lang3.StringUtils;
import org.pac4j.play.java.Secure;
import play.data.Form;
import play.data.FormFactory;
import play.data.validation.Constraints;
import play.data.validation.Constraints.Required;
import play.libs.concurrent.HttpExecutionContext;
import play.mvc.Controller;
import play.mvc.Result;
import uk.gov.bis.lite.countryservice.api.CountryView;
import utils.CountryUtils;
import views.html.licencefinder.destination;
import views.html.licencefinder.questions;
import views.html.licencefinder.registerResult;
import views.html.licencefinder.registerToUse;
import views.html.licencefinder.results;
import views.html.licencefinder.trade;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.CompletionStage;
import java.util.concurrent.ExecutionException;
import java.util.function.Function;
import java.util.stream.Collectors;

import javax.inject.Named;

@Secure(clients = SpireSAML2Client.CLIENT_NAME, authorizers = SamlAuthorizer.AUTHORIZER_NAME)
public class LicenceFinderController extends Controller {

  private final FormFactory formFactory;
  private final LicenceFinderDao dao;
  private final TransactionManager transactionManager;
  private final CountryProvider countryProvider;
  private final OgelConditionsServiceClient conditionsClient;
  private final HttpExecutionContext httpContext;
  private final FrontendServiceClient frontendClient;
  private final ApplicableOgelServiceClient applicableClient;
  private final String dashboardUrl;
  private final OgelService ogelService;
  private final LicenceFinderService licenceFinderService;

  public static final String DESTINATION_QUESTION = "Where is the final destination of your items?";
  public static final String DESTINATION_MULTIPLE_QUESTION = "Will your items be received by anyone in a different country or territory, such as a consignee, before reaching their final destination?";

  public static final String NONE_ABOVE_KEY = "NONE_ABOVE_KEY";

  private final String CONTROL_CODE_QUESTION = "What Control list entry describes your goods?";
  private final String GOODS_GOING_QUESTION = "Where are your goods going?";

  private final String FIRST_COUNTRY = "First country or territory that will receive the items";
  private final String REPAIR_QUESTION = "Are you exporting goods for or after repair or replacement?";
  private final String EXHIBITION_QUESTION = "Are you exporting goods for or after exhibition or demonstration?";
  private final String BEFORE_OR_LESS_QUESTION = "Were your goods manufactured before 1897, and worth less than £30,000?";

  public static final String DESTINATION_COUNTRY = "destinationCountry";
  public static final String FIRST_CONSIGNEE_COUNTRY = "firstConsigneeCountry";
  public static final String MULTIPLE_COUNTRIES = "multipleCountries";
  private static final String UNITED_KINGDOM = "CTRY0";

  @Inject
  public LicenceFinderController(TransactionManager transactionManager, FormFactory formFactory,
                                 HttpExecutionContext httpContext,
                                 LicenceFinderDao dao, @Named("countryProviderExport") CountryProvider countryProvider,
                                 OgelConditionsServiceClient conditionsClient, FrontendServiceClient frontendClient,
                                 ApplicableOgelServiceClient applicableClient,
                                 @com.google.inject.name.Named("dashboardUrl") String dashboardUrl,
                                 OgelService ogelService, LicenceFinderService licenceFinderService) {
    this.transactionManager = transactionManager;
    this.formFactory = formFactory;
    this.httpContext = httpContext;
    this.dao = dao;
    this.countryProvider = countryProvider;
    this.conditionsClient = conditionsClient;
    this.frontendClient = frontendClient;
    this.applicableClient = applicableClient;
    this.dashboardUrl = dashboardUrl;
    this.ogelService = ogelService;
    this.licenceFinderService = licenceFinderService;
  }

  /**
   * Test entry point
   */
  public CompletionStage<Result> testEntry(String controlCode) {
    transactionManager.createTransaction();
    dao.saveControlCode(controlCode);
    dao.saveApplicationCode("ABCD-1234");
    return renderTradeForm();
  }

  /************************************************************************************************
   * 'Trade' page
   *******************************************************************************************/
  public CompletionStage<Result> renderTradeForm() {
    TradeTypeForm form = new TradeTypeForm();
    dao.getTradeType().ifPresent((e) -> form.tradeType = e.toString());
    return completedFuture(ok(trade.render(formFactory.form(TradeTypeForm.class).fill(form), dao.getControlCode())));
  }

  public CompletionStage<Result> handleTradeSubmit() {
    Form<TradeTypeForm> form = formFactory.form(TradeTypeForm.class).bindFromRequest();
    String controlCode = dao.getControlCode();
    if (form.hasErrors()) {
      return completedFuture(ok(trade.render(form, controlCode)));
    }

    TradeType tradeType = TradeType.valueOf(form.get().tradeType);
    dao.saveTradeType(tradeType);

    switch (tradeType) {
      case EXPORT:
        dao.saveSourceCountry(UNITED_KINGDOM);
        return renderDestinationForm();
      case TRANSSHIPMENT:
        return completedFuture(redirect(controllers.routes.StaticContentController.renderTranshipment()));
      case BROKERING:
        return completedFuture(redirect(controllers.routes.StaticContentController.renderBrokering()));
      default:
        throw new FormStateException("Unknown trade type " + tradeType);
    }
  }

  /************************************************************************************************
   * 'Destination' page
   *******************************************************************************************/
  public CompletionStage<Result> renderDestinationForm() {
    DestinationForm form = new DestinationForm();
    form.destinationCountry = dao.getDestinationCountry();
    form.firstConsigneeCountry = dao.getFirstConsigneeCountry();
    dao.getMultipleCountries().ifPresent(aBoolean -> form.multipleCountries = aBoolean);
    return completedFuture(ok(destination.render(formFactory.form(DestinationForm.class).fill(form), getCountries(), getFieldOrder(form))));
  }

  public CompletionStage<Result> handleDestinationSubmit() {

    Form<DestinationForm> destinationForm = formFactory.form(DestinationForm.class).bindFromRequest();

    DestinationForm form = destinationForm.get();
    List<CountryView> countries = getCountries();

    if (form.destinationCountry == null || form.destinationCountry.isEmpty()) {
      destinationForm.reject(DESTINATION_COUNTRY, "Enter a country or territory");
    }

    if (form.multipleCountries == null) {
      destinationForm.reject(MULTIPLE_COUNTRIES, "Please answer whether your items will be received by anyone in a different country or territory");
    } else if(form.multipleCountries) {
      if (form.firstConsigneeCountry == null || form.firstConsigneeCountry.isEmpty()) {
        destinationForm.reject(FIRST_CONSIGNEE_COUNTRY, "Enter a country or territory");
      }
    }

    if (destinationForm.hasErrors()) {
      return completedFuture(ok(destination.render(destinationForm, countries, getFieldOrder(form))));
    }

    if (countries.stream().noneMatch(country -> country.getCountryRef().equals(form.destinationCountry))) {
      throw new FormStateException("Invalid value for " + DESTINATION_COUNTRY + " \"" + form.destinationCountry + "\"");
    }

    if (form.multipleCountries && countries.stream().noneMatch(country -> country.getCountryRef().equals(form.firstConsigneeCountry))) {
      throw new FormStateException("Invalid value for " + FIRST_CONSIGNEE_COUNTRY + " \"" + form.firstConsigneeCountry + "\"");
    }

    dao.saveFirstConsigneeCountry(form.firstConsigneeCountry);
    dao.saveMultipleCountries(form.multipleCountries);
    dao.saveDestinationCountry(form.destinationCountry);

    return renderQuestionsForm();
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
      return renderResultsForm();
    }
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
    dao.saveOgelId(chosenOgel);

    String controlCode = dao.getControlCode();
    String sourceCountry = dao.getSourceCountry();
    String destinationCountry = dao.getDestinationCountry();

    Optional<LicenceFinderController.QuestionsForm> ogelQuestionsFormOptional = dao.getQuestionsForm();
    List<String> activities = Collections.emptyList();
    Optional<QuestionsForm> optQuestionsForm = dao.getQuestionsForm();
    if (optQuestionsForm.isPresent()) {
      activities = getActivityTypes(optQuestionsForm.get());
    }

    String destinationCountryName = countryProvider.getCountry(destinationCountry).getCountryName();

    // Return No licences available when 'None of the above' chosen
    if (chosenOgel.equals(NONE_ABOVE_KEY)) {
      try {
        FrontendServiceResult result = frontendClient.get(controlCode).toCompletableFuture().get();
        OgelResultsDisplay display = new OgelResultsDisplay(Collections.emptyList(), result.getFrontendControlCode(), null, controlCode, destinationCountryName);
        return completedFuture(ok(results.render(form, display)));
      } catch (InterruptedException | ExecutionException e) {
        e.printStackTrace();
      }
    }

    CompletionStage<Void> checkOgelStage = applicableClient.get(controlCode, sourceCountry, getExportRouteCountries(), activities)
        .thenAcceptAsync(result -> {
          if (!result.stream().filter(ogelView -> StringUtils.equals(ogelView.getId(), chosenOgel)).findFirst().isPresent()) {
            throw new FormStateException(String.format("Chosen OGEL %s is not valid according to the applicable OGEL service response", chosenOgel));
          }
        }, httpContext.current());

    return renderRegisterToUseForm();
  }

  private CompletionStage<Result> renderWithForm(Form<ResultsForm> form) {

    String controlCode = dao.getControlCode();
    String destinationCountry = dao.getDestinationCountry();
    String destinationCountryName = countryProvider.getCountry(destinationCountry).getCountryName();
    List<String> exportRouteCountries = getExportRouteCountries();

    List<String> activities = Collections.emptyList();
    Optional<QuestionsForm> optQuestionsForm = dao.getQuestionsForm();
    if (optQuestionsForm.isPresent()) {
      activities = getActivityTypes(optQuestionsForm.get());
    }

    CompletionStage<FrontendServiceResult> frontendServiceStage = frontendClient.get(controlCode);

    return applicableClient.get(controlCode, dao.getSourceCountry(), exportRouteCountries, activities)
        .thenCombineAsync(frontendServiceStage, (applicableOgelView, frontendServiceResult) -> {
          if (!applicableOgelView.isEmpty()) {
            OgelResultsDisplay display = new OgelResultsDisplay(applicableOgelView, frontendServiceResult.getFrontendControlCode(),
                null, controlCode, destinationCountryName);
            return ok(results.render(form, display));
          } else {
            List<String> countryNames = CountryUtils.getFilteredCountries(CountryUtils.getSortedCountries(countryProvider.getCountries()), exportRouteCountries).stream()
                .map(CountryView::getCountryName)
                .collect(Collectors.toList());
            OgelResultsDisplay display = new OgelResultsDisplay(applicableOgelView, frontendServiceResult.getFrontendControlCode(),
                countryNames, controlCode, destinationCountryName);

            return ok(results.render(form, display));
          }
        }, httpContext.current());
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

    return licenceFinderService.registerOgel(transactionId).thenApplyAsync(conditionsResult ->
            ogelService.get(dao.getOgelId())
                .thenApplyAsync(ogelFullView -> {
                  RegisterResultView view = new RegisterResultView("You have successfully registered to use Open general export licence (" + ogelFullView.getName() + ")" );
                  Optional<String> regRef = licenceFinderService.getRegistrationReference(transactionId);
                  if(regRef.isPresent()) {
                    view = new RegisterResultView("You have successfully registered to use Open general export licence (" + ogelFullView.getName() + ") ", regRef.get());
                  }

                  return ok(registerResult.render(view, ogelFullView, dashboardUrl));
                }, httpContext.current())
        , httpContext.current())
        .thenCompose(Function.identity());
  }

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

  /**
   * Private methods
   */
  private List<String> getFieldOrder(DestinationForm form) {
    List<String> fields = new ArrayList<>();
    fields.add(DESTINATION_COUNTRY);
    fields.add(MULTIPLE_COUNTRIES);
    fields.add(FIRST_CONSIGNEE_COUNTRY);
    return fields;
  }

  private List<CountryView> getCountries() {
    return CountryUtils.getFilteredCountries(CountryUtils.getSortedCountries(countryProvider.getCountries()),
        Collections.singletonList(UNITED_KINGDOM), true);
  }

  private List<String> getActivityTypes(QuestionsForm questionsForm) {
    Map<OgelActivityType, String> map = new HashMap<>();
    map.put(OgelActivityType.DU_ANY, OgelActivityType.DU_ANY.value());
    map.put(OgelActivityType.EXHIBITION, OgelActivityType.EXHIBITION.value());
    map.put(OgelActivityType.MIL_ANY, OgelActivityType.MIL_ANY.value());
    map.put(OgelActivityType.MIL_GOV, OgelActivityType.MIL_GOV.value());
    map.put(OgelActivityType.REPAIR, OgelActivityType.REPAIR.value());
    if ("false".equals(questionsForm.forRepair)) {
      map.remove(OgelActivityType.REPAIR);
    }
    if ("false".equals(questionsForm.forExhibition)) {
      map.remove(OgelActivityType.EXHIBITION);
    }
    return map.entrySet().stream().map(Map.Entry::getValue).collect(Collectors.toList());
  }

  private List<QuestionView> getLicenceFinderAnswers() {
    List<QuestionView> views = new ArrayList<>();

    views.add(new QuestionView(CONTROL_CODE_QUESTION, dao.getControlCode()));
    dao.getTradeType().ifPresent(tradeType -> views.add(new QuestionView(GOODS_GOING_QUESTION, tradeType.getTitle())));
    views.add(new QuestionView(DESTINATION_QUESTION, countryProvider.getCountry(dao.getDestinationCountry()).getCountryName()));
    dao.getMultipleCountries().ifPresent(aBoolean -> views.add(new QuestionView(DESTINATION_MULTIPLE_QUESTION, aBoolean ? "Yes" : "No")));

    Optional<Boolean> optMultipleCountries = dao.getMultipleCountries();
    if(optMultipleCountries.isPresent()) {
      boolean isMultiple = optMultipleCountries.get();
      if(isMultiple) {
        views.add(new QuestionView(FIRST_COUNTRY, countryProvider.getCountry(dao.getFirstConsigneeCountry()).getCountryName()));
      }
    }

    Optional<LicenceFinderController.QuestionsForm> optForm = dao.getQuestionsForm();
    if (optForm.isPresent()) {
      LicenceFinderController.QuestionsForm form = optForm.get();
      views.add(new QuestionView(REPAIR_QUESTION, form.forRepair.equals("true") ? "Yes" : "No"));
      views.add(new QuestionView(EXHIBITION_QUESTION, form.forExhibition.equals("true") ? "Yes" : "No"));
      views.add(new QuestionView(BEFORE_OR_LESS_QUESTION, form.beforeOrLess.equals("true") ? "Yes" : "No"));
    }
    return views;
  }

  private List<String> getExportRouteCountries() {
    List<String> countries = new ArrayList<>();
    String destination = dao.getDestinationCountry();
    if(!StringUtils.isBlank(destination)) {
      countries.add(destination);
    }
    String first = dao.getDestinationCountry();
    if(!StringUtils.isBlank(first)) {
      countries.add(first);
    }
    return countries;
  }

  /**
   * Form definitions
   */

  public static class TradeTypeForm {
    @Required(message = "Select where your items are going")
    public String tradeType;
  }

  public static class DestinationForm {
    public String destinationCountry;
    public String firstConsigneeCountry;
    public Boolean multipleCountries;
  }

  public static class QuestionsForm {

    @Required(message = "Select whether you are exporting goods for or after repair or replacement")
    public String forRepair;

    @Required(message = "Select whether you are exporting goods for or after exhibition or demonstration")
    public String forExhibition;

    @Required(message = "Select whether your goods were manufactured before 1897, or are worth less than £30,000")
    public String beforeOrLess;

  }

  public static class ResultsForm {
    @Required(message = "You must choose from the list of results below")
    public String chosenOgel;
  }

  public static class RegisterToUseForm {

    @Constraints.Required(message = "Confirm you have read the OGEL and its criteria in full.")
    public String confirmRead;

    @Constraints.Required(message = "Confirm your export complies with the OGEL criteria stated.")
    public String confirmComplies;

  }

}
