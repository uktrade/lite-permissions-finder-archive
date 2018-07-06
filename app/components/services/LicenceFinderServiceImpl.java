package components.services;

import com.google.inject.Inject;
import com.spotify.futures.CompletableFutures;
import components.common.client.userservice.UserServiceClientJwt;
import components.persistence.LicenceFinderDao;
import components.services.notification.PermissionsFinderNotificationClient;
import components.services.ogels.applicable.ApplicableOgelServiceClient;
import controllers.licencefinder.QuestionsController;
import exceptions.ServiceException;
import models.OgelActivityType;
import models.persistence.RegisterLicence;
import models.view.licencefinder.Customer;
import models.view.licencefinder.OgelView;
import models.view.licencefinder.Site;
import org.slf4j.LoggerFactory;
import uk.gov.bis.lite.customer.api.view.CustomerView;
import uk.gov.bis.lite.customer.api.view.SiteView;
import uk.gov.bis.lite.ogel.api.view.ApplicableOgelView;
import uk.gov.bis.lite.permissions.api.view.CallbackView;
import uk.gov.bis.lite.permissions.api.view.OgelRegistrationView;
import uk.gov.bis.lite.user.api.view.UserDetailsView;

import java.util.EnumSet;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.CompletionStage;
import java.util.concurrent.ExecutionException;
import java.util.stream.Collectors;

public class LicenceFinderServiceImpl implements LicenceFinderService {

  private static final org.slf4j.Logger LOGGER = LoggerFactory.getLogger(LicenceFinderServiceImpl.class);

  private final LicenceFinderDao licenceFinderDao;
  private final PermissionsService permissionsService;
  private final String permissionsFinderUrl;
  private final ApplicableOgelServiceClient applicableClient;
  private final PermissionsFinderNotificationClient notificationClient;
  private final UserServiceClientJwt userService;

  @Inject
  public LicenceFinderServiceImpl(LicenceFinderDao licenceFinderDao,
                                  PermissionsService permissionsService,
                                  @com.google.inject.name.Named("permissionsFinderUrl") String permissionsFinderUrl,
                                  ApplicableOgelServiceClient applicableClient,
                                  PermissionsFinderNotificationClient notificationClient,
                                  UserServiceClientJwt userService) {
    this.licenceFinderDao = licenceFinderDao;
    this.permissionsService = permissionsService;
    this.permissionsFinderUrl = permissionsFinderUrl;
    this.applicableClient = applicableClient;
    this.notificationClient = notificationClient;
    this.userService = userService;
  }

  // Stage1: EntryController
  // Stage2: TradeController
  // Stage3: DestinationsController
  // Stage4: QuestionsController
  // Stage5: ChooseOgelController
  // Stage6: RegisterToUseController

  @Override
  public boolean canAccessTradeController(String sessionId) {
    return licenceFinderDao.getControlCode(sessionId).isPresent()
        && licenceFinderDao.getResumeCode(sessionId).isPresent()
        && licenceFinderDao.getUserId(sessionId).isPresent()
        && licenceFinderDao.getCustomer(sessionId).isPresent()
        && licenceFinderDao.getSite(sessionId).isPresent();
  }

  @Override
  public boolean canAccessDestinationController(String sessionId) {
    return canAccessTradeController(sessionId)
        && licenceFinderDao.getTradeType(sessionId).isPresent()
        && licenceFinderDao.getSourceCountry(sessionId).isPresent();
  }

  @Override
  public boolean canAccessQuestionController(String sessionId) {
    return canAccessDestinationController(sessionId)
        && licenceFinderDao.getDestinationCountry(sessionId).isPresent()
        && licenceFinderDao.getMultipleCountries(sessionId).isPresent();
  }

  @Override
  public boolean canAccessChooseOgelController(String sessionId) {
    return canAccessQuestionController(sessionId) && licenceFinderDao.getQuestionsForm(sessionId).isPresent();
  }

  @Override
  public boolean canAccessRegisterToUseController(String sessionId) {
    return canAccessChooseOgelController(sessionId) && licenceFinderDao.getOgelId(sessionId).isPresent();
  }

  @Override
  public void saveDestinations(String sessionId, String destinationCountry, String firstConsigneeCountry,
                               boolean multipleCountries) {
    licenceFinderDao.saveFirstConsigneeCountry(sessionId, firstConsigneeCountry);
    licenceFinderDao.saveMultipleCountries(sessionId, multipleCountries);
    licenceFinderDao.saveDestinationCountry(sessionId, destinationCountry);
  }

  @Override
  public Map<String, String> getUserOgelIdReferenceMap(String sessionId) {
    return licenceFinderDao.getUserOgelIdReferenceMap(sessionId);
  }

  @Override
  public void updateUserOgelIdReferenceMap(String sessionId, String userId) {
    Map<String, String> userOgelIdReferenceMap = createUserOgelIdReferenceMap(userId);
    licenceFinderDao.saveUserOgelIdRefMap(sessionId, userOgelIdReferenceMap);
  }

  @Override
  public List<ApplicableOgelView> getApplicableOgelViews(String controlCode, String sourceCountry,
                                                         List<String> destinationCountries,
                                                         QuestionsController.QuestionsForm questionsForm) {
    List<String> activities = getActivityTypes(questionsForm);
    boolean showHistoricOgel = questionsForm.beforeOrLess;
    CompletionStage<List<ApplicableOgelView>> stage = applicableClient.get(controlCode, sourceCountry, destinationCountries, activities, showHistoricOgel);
    try {
      return stage.toCompletableFuture().get();
    } catch (Exception exception) {
      throw new ServiceException("Unable to get applicable views", exception);
    }
  }

  @Override
  public Optional<String> getRegistrationReference(String sessionId) {
    return licenceFinderDao.getRegisterLicence(sessionId)
        .map(RegisterLicence::getRegistrationReference);
  }

  @Override
  public void registerOgel(String sessionId, String userId, String customerId, String siteId, String ogelId) {
    String callbackUrl = permissionsFinderUrl + controllers.licencefinder.routes.RegistrationController.handleRegistrationCallback(sessionId);

    CompletionStage<String> registrationResponseStage = permissionsService.registerOgel(userId, customerId, siteId, ogelId, callbackUrl);
    CompletionStage<UserDetailsView> userDetailsViewStage = userService.getUserDetailsView(userId);

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
    }).toCompletableFuture();
    // TODO what now
  }

  /**
   * handleCallback handles callback from permission service after Ogel submission
   * Updates licence registration reference
   * Handles logic to send confirmation emails to user and to ECJU
   */
  @Override
  public void handleCallback(String sessionId, CallbackView callbackView) {
    Optional<RegisterLicence> optRegisterLicence = licenceFinderDao.getRegisterLicence(sessionId);
    if (optRegisterLicence.isPresent()) {
      String registrationReference = callbackView.getRegistrationReference();

      RegisterLicence registerLicence = optRegisterLicence.get();
      registerLicence.setRegistrationReference(registrationReference);
      licenceFinderDao.saveRegisterLicence(sessionId, registerLicence);

      // Send confirmation emails
      Optional<Customer> optCustomer = licenceFinderDao.getCustomer(sessionId);
      Optional<Site> optSite = licenceFinderDao.getSite(sessionId);
      Optional<String> optResumeCode = licenceFinderDao.getResumeCode(sessionId);
      if (optCustomer.isPresent() && optSite.isPresent() && optResumeCode.isPresent()) {
        Customer customer = optCustomer.get();
        Site site = optSite.get();
        String resumeCode = optResumeCode.get();

        // Send confirmation email to user
        String ogelUrl = permissionsFinderUrl + controllers.licencefinder.routes.ViewOgelController.viewOgel(registrationReference);
        String userEmailAddress = registerLicence.getUserEmailAddress();
        String applicantName = registerLicence.getUserFullName();
        notificationClient.sendRegisteredOgelToUserEmail(userEmailAddress, applicantName, ogelUrl);

        // Send confirmation email to Ecju
        String companyName = customer.getCompanyName();
        String siteAddress = site.getAddress();
        notificationClient.sendRegisteredOgelEmailToEcju(userEmailAddress, applicantName, resumeCode, companyName, siteAddress, ogelUrl);
      } else {
        LOGGER.warn("Missing customer or site or resumeCode information for licence registration reference " + registrationReference);
      }
    }
  }

  @Override
  public void persistCustomerAndSiteData(String sessionId, CustomerView customerView, SiteView siteView) {
    Customer customer = new Customer(customerView.getCustomerId(), customerView.getCompanyName());
    licenceFinderDao.saveCustomer(sessionId, customer);
    Site site = new Site(siteView.getSiteId(), siteView.getAddress().getPlainText());
    licenceFinderDao.saveSite(sessionId, site);
  }

  @Override
  public List<OgelView> getOgelViews(List<ApplicableOgelView> applicableViews, Set<String> existingOgels) {
    return applicableViews.stream()
        .map(applicableOgelView -> {
          boolean alreadyRegistered = existingOgels.contains(applicableOgelView.getId());

          OgelView view = new OgelView();
          view.setId(applicableOgelView.getId());
          view.setName(applicableOgelView.getName());
          view.setUsageSummary(applicableOgelView.getUsageSummary());
          view.setAlreadyRegistered(alreadyRegistered);
          return view;
        }).collect(Collectors.toList());
  }

  private List<String> getActivityTypes(QuestionsController.QuestionsForm questionsForm) {
    Set<OgelActivityType> set = EnumSet.of(OgelActivityType.DU_ANY, OgelActivityType.MIL_ANY, OgelActivityType.MIL_GOV);
    if (questionsForm.forRepair) {
      set.add(OgelActivityType.REPAIR);
    }
    if (questionsForm.forExhibition) {
      set.add(OgelActivityType.EXHIBITION);
    }
    return set.stream().map(OgelActivityType::toString).collect(Collectors.toList());
  }

  private Map<String, String> createUserOgelIdReferenceMap(String userId) {
    try {
      Map<String, String> ogelIdReferenceMap = new HashMap<>();
      List<OgelRegistrationView> views = permissionsService.getOgelRegistrations(userId).toCompletableFuture().get();
      for (OgelRegistrationView view : views) {
        ogelIdReferenceMap.put(view.getOgelType(), view.getRegistrationReference());
      }
      return ogelIdReferenceMap;
    } catch (InterruptedException | ExecutionException exception) {
      throw new ServiceException("Unable to get userOgelIdReferenceMap for userId " + userId, exception);
    }
  }

}
