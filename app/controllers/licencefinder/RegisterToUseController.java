package controllers.licencefinder;

import com.google.inject.Inject;
import com.spotify.futures.CompletableFutures;
import components.client.OgelServiceClient;
import components.client.PermissionsServiceClient;
import components.common.auth.SamlAuthorizer;
import components.common.auth.SpireAuthManager;
import components.common.auth.SpireSAML2Client;
import components.common.cache.CountryProvider;
import components.common.client.userservice.UserServiceClientJwt;
import components.persistence.LicenceFinderDao;
import controllers.guard.LicenceFinderAwaitGuardAction;
import controllers.guard.LicenceFinderUserGuardAction;
import exceptions.UnknownParameterException;
import models.TradeType;
import models.persistence.RegisterLicence;
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
import uk.gov.bis.lite.user.api.view.UserDetailsView;

import java.util.ArrayList;
import java.util.List;
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
  private final OgelServiceClient ogelServiceClient;
  private final String permissionsFinderUrl;
  private final PermissionsServiceClient permissionsServiceClient;
  private final UserServiceClientJwt userServiceClientJwt;
  private final views.html.licencefinder.registerToUse registerToUse;

  @Inject
  public RegisterToUseController(SpireAuthManager spireAuthManager, FormFactory formFactory,
                                 HttpExecutionContext httpContext, LicenceFinderDao licenceFinderDao,
                                 @Named("countryProviderExport") CountryProvider countryProvider,
                                 OgelServiceClient ogelServiceClient,
                                 @com.google.inject.name.Named("permissionsFinderUrl") String permissionsFinderUrl,
                                 PermissionsServiceClient permissionsServiceClient,
                                 UserServiceClientJwt userServiceClientJwt,
                                 views.html.licencefinder.registerToUse registerToUse) {
    this.spireAuthManager = spireAuthManager;
    this.formFactory = formFactory;
    this.httpContext = httpContext;
    this.licenceFinderDao = licenceFinderDao;
    this.countryProvider = countryProvider;
    this.ogelServiceClient = ogelServiceClient;
    this.permissionsFinderUrl = permissionsFinderUrl;
    this.permissionsServiceClient = permissionsServiceClient;
    this.userServiceClientJwt = userServiceClientJwt;
    this.registerToUse = registerToUse;
  }

  public CompletionStage<Result> renderRegisterToUseForm(String sessionId) {
    String ogelId = licenceFinderDao.getOgelId(sessionId).orElseThrow(UnknownParameterException::unknownLicenceFinderOrder);
    return renderWithRegisterToUseForm(formFactory.form(RegisterToUseForm.class), sessionId, ogelId);
  }

  public CompletionStage<Result> handleRegisterToUseSubmit(String sessionId) {
    String ogelId = licenceFinderDao.getOgelId(sessionId).orElseThrow(UnknownParameterException::unknownLicenceFinderOrder);
    Form<RegisterToUseForm> form = formFactory.form(RegisterToUseForm.class).bindFromRequest();
    if (form.hasErrors()) {
      return renderWithRegisterToUseForm(form, sessionId, ogelId);
    } else {
      String userId = spireAuthManager.getAuthInfoFromContext().getId();
      Customer customer = licenceFinderDao.getCustomer(sessionId).orElseThrow(UnknownParameterException::unknownLicenceFinderOrder);
      Site site = licenceFinderDao.getSite(sessionId).orElseThrow(UnknownParameterException::unknownLicenceFinderOrder);
      registerOgel(sessionId, userId, ogelId, customer.getId(), site.getId());
      return CompletableFuture.completedFuture(redirect(routes.RegisterAwaitController.renderAwaitResult(sessionId)));
    }
  }

  private CompletionStage<Result> renderWithRegisterToUseForm(Form<RegisterToUseForm> form, String sessionId,
                                                              String ogelId) {
    return ogelServiceClient.getById(ogelId).thenApplyAsync(ogelFullView ->
            ok(registerToUse.render(form, ogelFullView, getLicenceFinderAnswers(sessionId), sessionId)),
        httpContext.current());
  }

  private List<QuestionView> getLicenceFinderAnswers(String sessionId) {
    List<QuestionView> views = new ArrayList<>();

    String controlCode = licenceFinderDao.getControlCode(sessionId)
        .orElseThrow(UnknownParameterException::unknownLicenceFinderOrder);
    views.add(new QuestionView(CONTROL_CODE_QUESTION, controlCode));

    TradeType tradeType = licenceFinderDao.getTradeType(sessionId)
        .orElseThrow(UnknownParameterException::unknownLicenceFinderOrder);
    views.add(new QuestionView(GOODS_GOING_QUESTION, tradeType.getTitle()));

    String destinationCountry = licenceFinderDao.getDestinationCountry(sessionId)
        .orElseThrow(UnknownParameterException::unknownLicenceFinderOrder);
    String destinationCountryName = countryProvider.getCountry(destinationCountry).getCountryName();
    views.add(new QuestionView(DestinationController.DESTINATION_QUESTION, destinationCountryName));

    boolean multipleCountries = licenceFinderDao.getMultipleCountries(sessionId)
        .orElseThrow(UnknownParameterException::unknownLicenceFinderOrder);
    views.add(new QuestionView(DestinationController.DESTINATION_MULTIPLE_QUESTION, toAnswer(multipleCountries)));
    if (multipleCountries) {
      String firstConsigneeCountry = licenceFinderDao.getFirstConsigneeCountry(sessionId)
          .orElseThrow(UnknownParameterException::unknownLicenceFinderOrder);
      String firstConsigneeCountryName = countryProvider.getCountry(firstConsigneeCountry).getCountryName();
      views.add(new QuestionView(FIRST_COUNTRY, firstConsigneeCountryName));
    }

    QuestionsController.QuestionsForm questionsForm = licenceFinderDao.getQuestionsForm(sessionId)
        .orElseThrow(UnknownParameterException::unknownLicenceFinderOrder);
    views.add(new QuestionView(REPAIR_QUESTION, toAnswer(questionsForm.forRepair)));
    views.add(new QuestionView(EXHIBITION_QUESTION, toAnswer(questionsForm.forExhibition)));
    views.add(new QuestionView(BEFORE_OR_LESS_QUESTION, toAnswer(questionsForm.beforeOrLess)));
    return views;
  }

  private void registerOgel(String sessionId, String userId, String customerId, String siteId, String ogelId) {
    String callbackUrl = permissionsFinderUrl + controllers.licencefinder.routes.RegistrationController.handleRegistrationCallback(sessionId);
    CompletionStage<String> registrationResponseStage = permissionsServiceClient.registerOgel(userId, customerId, siteId, ogelId, callbackUrl);
    CompletionStage<UserDetailsView> userDetailsViewStage = userServiceClientJwt.getUserDetailsView(userId);

    CompletableFutures.combine(registrationResponseStage, userDetailsViewStage, (requestId, userDetailsView) -> {
      RegisterLicence registerLicence = new RegisterLicence();
      registerLicence.setSessionId(sessionId);
      registerLicence.setUserId(userId);
      registerLicence.setOgelId(ogelId);
      registerLicence.setCustomerId(customerId);
      registerLicence.setUserEmailAddress(userDetailsView.getContactEmailAddress());
      registerLicence.setUserFullName(userDetailsView.getFullName());
      registerLicence.setRequestId(requestId);
      licenceFinderDao.saveRegisterLicence(sessionId, registerLicence);
      return null;
    });
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
