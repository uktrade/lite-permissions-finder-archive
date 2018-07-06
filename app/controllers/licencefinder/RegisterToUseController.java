package controllers.licencefinder;

import com.google.inject.Inject;
import components.common.auth.SamlAuthorizer;
import components.common.auth.SpireAuthManager;
import components.common.auth.SpireSAML2Client;
import components.common.cache.CountryProvider;
import components.persistence.LicenceFinderDao;
import components.services.LicenceFinderService;
import components.services.OgelService;
import controllers.guard.LicenceFinderAwaitGuardAction;
import controllers.guard.LicenceFinderUserGuardAction;
import exceptions.UnknownParameterException;
import models.TradeType;
import models.view.QuestionView;
import models.view.licencefinder.Customer;
import models.view.licencefinder.Site;
import org.pac4j.play.java.Secure;
import play.data.Form;
import play.data.FormFactory;
import play.data.validation.Constraints;
import play.libs.concurrent.HttpExecutionContext;
import play.mvc.Controller;
import play.mvc.Result;
import play.mvc.With;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;

import javax.inject.Named;

@Secure(clients = SpireSAML2Client.CLIENT_NAME, authorizers = SamlAuthorizer.AUTHORIZER_NAME)
@With({LicenceFinderUserGuardAction.class, LicenceFinderAwaitGuardAction.class})
public class RegisterToUseController extends Controller {

  private static final String CONTROL_CODE_QUESTION = "What control list entry describes your goods?";
  private static final String GOODS_GOING_QUESTION = "Where are your goods going?";
  private static final String FIRST_COUNTRY = "First country or territory that will receive the items";
  private static final String REPAIR_QUESTION = "Are you exporting goods for or after repair or replacement?";
  private static final String EXHIBITION_QUESTION = "Are you exporting goods for or after exhibition or demonstration?";
  private static final String BEFORE_OR_LESS_QUESTION = "Were your goods manufactured before 1897 and are they worth less than £35,000?";

  private static final String YES = "Yes";
  private static final String NO = "No";

  private final SpireAuthManager spireAuthManager;
  private final FormFactory formFactory;
  private final LicenceFinderDao licenceFinderDao;
  private final CountryProvider countryProvider;
  private final HttpExecutionContext httpContext;
  private final OgelService ogelService;
  private final LicenceFinderService licenceFinderService;
  private final views.html.licencefinder.registerToUse registerToUse;

  @Inject
  public RegisterToUseController(SpireAuthManager spireAuthManager, FormFactory formFactory,
                                 HttpExecutionContext httpContext,
                                 LicenceFinderDao licenceFinderDao,
                                 @Named("countryProviderExport") CountryProvider countryProvider,
                                 OgelService ogelService, LicenceFinderService licenceFinderService,
                                 views.html.licencefinder.registerToUse registerToUse) {
    this.spireAuthManager = spireAuthManager;
    this.formFactory = formFactory;
    this.httpContext = httpContext;
    this.licenceFinderDao = licenceFinderDao;
    this.countryProvider = countryProvider;
    this.ogelService = ogelService;
    this.licenceFinderService = licenceFinderService;
    this.registerToUse = registerToUse;
  }

  public CompletionStage<Result> renderRegisterToUseForm(String sessionId) {
    if (licenceFinderService.canAccessRegisterToUseController(sessionId)) {
      String ogelId = licenceFinderDao.getOgelId(sessionId).orElseThrow(UnknownParameterException::unknownOgelRegistrationOrder);
      return renderWithRegisterToUseForm(formFactory.form(RegisterToUseForm.class), sessionId, ogelId);
    } else {
      throw UnknownParameterException.unknownOgelRegistrationOrder();
    }
  }

  public CompletionStage<Result> handleRegisterToUseSubmit(String sessionId) {
    if (licenceFinderService.canAccessRegisterToUseController(sessionId)) {
      String ogelId = licenceFinderDao.getOgelId(sessionId).orElseThrow(UnknownParameterException::unknownOgelRegistrationOrder);
      Form<RegisterToUseForm> form = formFactory.form(RegisterToUseForm.class).bindFromRequest();
      if (form.hasErrors()) {
        return renderWithRegisterToUseForm(form, sessionId, ogelId);
      } else {
        String userId = spireAuthManager.getAuthInfoFromContext().getId();
        Customer customer = licenceFinderDao.getCustomer(sessionId).orElseThrow(UnknownParameterException::unknownOgelRegistrationOrder);
        Site site = licenceFinderDao.getSite(sessionId).orElseThrow(UnknownParameterException::unknownOgelRegistrationOrder);
        licenceFinderService.registerOgel(sessionId, userId, ogelId, customer.getId(), site.getId());
        Optional<String> referenceOptional = licenceFinderService.getRegistrationReference(sessionId);
        if (referenceOptional.isPresent()) {
          String reference = referenceOptional.get();
          return CompletableFuture.completedFuture(redirect(routes.RegisterAwaitController.registrationSuccess(sessionId, reference)));
        } else {
          return CompletableFuture.completedFuture(redirect(routes.RegisterAwaitController.renderAwaitResult(sessionId)));
        }
      }
    } else {
      throw UnknownParameterException.unknownOgelRegistrationOrder();
    }
  }

  private CompletionStage<Result> renderWithRegisterToUseForm(Form<RegisterToUseForm> form, String sessionId,
                                                              String ogelId) {
    return ogelService.getById(ogelId).thenApplyAsync(ogelFullView ->
            ok(registerToUse.render(form, ogelFullView, getLicenceFinderAnswers(sessionId), sessionId)),
        httpContext.current());
  }

  private List<QuestionView> getLicenceFinderAnswers(String sessionId) {
    List<QuestionView> views = new ArrayList<>();

    String controlCode = licenceFinderDao.getControlCode(sessionId)
        .orElseThrow(UnknownParameterException::unknownOgelRegistrationOrder);
    views.add(new QuestionView(CONTROL_CODE_QUESTION, controlCode));

    TradeType tradeType = licenceFinderDao.getTradeType(sessionId)
        .orElseThrow(UnknownParameterException::unknownOgelRegistrationOrder);
    views.add(new QuestionView(GOODS_GOING_QUESTION, tradeType.getTitle()));

    String destinationCountry = licenceFinderDao.getDestinationCountry(sessionId)
        .orElseThrow(UnknownParameterException::unknownOgelRegistrationOrder);
    String destinationCountryName = countryProvider.getCountry(destinationCountry).getCountryName();
    views.add(new QuestionView(DestinationController.DESTINATION_QUESTION, destinationCountryName));

    boolean multipleCountries = licenceFinderDao.getMultipleCountries(sessionId)
        .orElseThrow(UnknownParameterException::unknownOgelRegistrationOrder);
    views.add(new QuestionView(DestinationController.DESTINATION_MULTIPLE_QUESTION, toAnswer(multipleCountries)));
    if (multipleCountries) {
      String firstConsigneeCountry = licenceFinderDao.getFirstConsigneeCountry(sessionId)
          .orElseThrow(UnknownParameterException::unknownOgelRegistrationOrder);
      String firstConsigneeCountryName = countryProvider.getCountry(firstConsigneeCountry).getCountryName();
      views.add(new QuestionView(FIRST_COUNTRY, firstConsigneeCountryName));
    }

    QuestionsController.QuestionsForm questionsForm = licenceFinderDao.getQuestionsForm(sessionId)
        .orElseThrow(UnknownParameterException::unknownOgelRegistrationOrder);
    views.add(new QuestionView(REPAIR_QUESTION, toAnswer(questionsForm.forRepair)));
    views.add(new QuestionView(EXHIBITION_QUESTION, toAnswer(questionsForm.forExhibition)));
    views.add(new QuestionView(BEFORE_OR_LESS_QUESTION, toAnswer(questionsForm.beforeOrLess)));
    return views;
  }

  private String toAnswer(boolean bool) {
    return bool ? YES : NO;
  }

  public static class RegisterToUseForm {
    @Constraints.Required(message = "Confirm you have read the OGEL and its criteria in full.")
    public Boolean confirmRead;

    @Constraints.Required(message = "Confirm your export complies with the OGEL criteria stated.")
    public Boolean confirmComplies;
  }
}
