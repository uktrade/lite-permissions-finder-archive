package components.services;

import com.google.inject.Inject;
import components.common.auth.SpireAuthManager;
import components.common.cache.CountryProvider;
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
import models.view.licencefinder.ResultsView;
import models.view.licencefinder.Site;
import org.apache.commons.lang.StringUtils;
import org.slf4j.LoggerFactory;
import uk.gov.bis.lite.customer.api.view.CustomerView;
import uk.gov.bis.lite.customer.api.view.SiteView;
import uk.gov.bis.lite.ogel.api.view.ApplicableOgelView;
import uk.gov.bis.lite.permissions.api.view.CallbackView;
import uk.gov.bis.lite.permissions.api.view.OgelRegistrationView;
import uk.gov.bis.lite.user.api.view.UserDetailsView;

import java.util.ArrayList;
import java.util.Collections;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.CompletionStage;
import java.util.concurrent.ExecutionException;
import java.util.stream.Collectors;

import javax.inject.Named;

public class LicenceFinderServiceImpl implements LicenceFinderService {

  private static final org.slf4j.Logger LOGGER = LoggerFactory.getLogger(LicenceFinderServiceImpl.class);

  private final LicenceFinderDao licenceFinderDao;
  private final CustomerService customerService;
  private final SpireAuthManager authManager;
  private final PermissionsService permissionsService;
  private final String permissionsFinderUrl;
  private final ApplicableOgelServiceClient applicableClient;
  private final CountryProvider countryProvider;
  private final PermissionsFinderNotificationClient notificationClient;
  private final UserServiceClientJwt userService;

  @Inject
  public LicenceFinderServiceImpl(LicenceFinderDao licenceFinderDao, CustomerService customerService,
                                  PermissionsService permissionsService, SpireAuthManager authManager,
                                  @com.google.inject.name.Named("permissionsFinderUrl") String permissionsFinderUrl,
                                  ApplicableOgelServiceClient applicableClient,
                                  @Named("countryProviderExport") CountryProvider countryProvider,
                                  PermissionsFinderNotificationClient notificationClient,
                                  UserServiceClientJwt userService) {
    this.licenceFinderDao = licenceFinderDao;
    this.permissionsService = permissionsService;
    this.customerService = customerService;
    this.authManager = authManager;
    this.permissionsFinderUrl = permissionsFinderUrl;
    this.applicableClient = applicableClient;
    this.countryProvider = countryProvider;
    this.notificationClient = notificationClient;
    this.userService = userService;
  }

  public void updateUsersOgelIdRefMap(String sessionId, String userId) {
    // Store map of already registered Ogel Id to reference map for user
    licenceFinderDao.saveUserOgelIdRefMap(sessionId, getUserOgelIdRefMap(userId));
  }

  public boolean isOgelIdAlreadyRegistered(String sessionId, String ogelId) {
    return licenceFinderDao.getUserOgelIdRefMap(sessionId).keySet().contains(ogelId);
  }

  public Optional<String> getUserOgelReference(String sessionId, String ogelId) {
    Map<String, String> ogelIdRefMap = licenceFinderDao.getUserOgelIdRefMap(sessionId);
    if (ogelIdRefMap.keySet().contains(ogelId)) {
      return Optional.of(ogelIdRefMap.get(ogelId));
    }
    return Optional.empty();
  }

  /**
   * Returns results view with Ogel list omitted
   */
  public ResultsView getNoResultsView(String sessionId) {
    return doGetResultsView(sessionId, false);
  }

  /**
   * Returns results view containing users selectable Ogels
   */
  public ResultsView getResultsView(String sessionId) {
    return doGetResultsView(sessionId, true);
  }

  /**
   * Attempts to read callback reference set number times/period
   */
  public Optional<String> getRegistrationReference(String sessionId) {

    Optional<RegisterLicence> optRegisterLicence = licenceFinderDao.getRegisterLicence(sessionId);
    if (optRegisterLicence.isPresent()) {
      String ref = optRegisterLicence.get().getRegistrationReference();
      if (!StringUtils.isBlank(ref)) {
        return Optional.of(ref);
      }
    }
    return Optional.empty();
  }

  /**
   * registerOgel
   */
  public void registerOgel(String sessionId) {
    String userId = getUserId();

    Optional<Customer> optCustomer = licenceFinderDao.getCustomer(sessionId);
    Optional<Site> optSite = licenceFinderDao.getSite(sessionId);
    if (!optCustomer.isPresent() || !optSite.isPresent()) {
      throw new ServiceException("Customer and/or Site could not be determined - a user can only have one associated Customer and only one associated Site");
    }

    String customerId = optCustomer.get().getId();
    String siteId = optSite.get().getId();
    String ogelId = licenceFinderDao.getOgelId(sessionId);

    String callbackUrl = permissionsFinderUrl + controllers.licencefinder.routes.RegistrationController.handleRegistrationCallback(sessionId);

    if (StringUtils.isBlank(customerId) || StringUtils.isBlank(siteId)) {
      throw new ServiceException("Customer and/or Site could not be determined - a user can only have one associated Customer and only one associated Site");
    }

    RegisterLicence registerLicence = new RegisterLicence();
    registerLicence.setSessionId(sessionId);
    registerLicence.setUserId(userId);
    registerLicence.setOgelId(ogelId);
    registerLicence.setCustomerId(customerId);

    // Add user information to licence application data
    UserDetailsView userDetailsView = getUserDetails(registerLicence.getUserId());
    registerLicence.setUserEmailAddress(userDetailsView.getContactEmailAddress());
    registerLicence.setUserFullName(userDetailsView.getFullName());

    permissionsService.registerOgel(userId, customerId, siteId, ogelId, callbackUrl)
        .thenAcceptAsync(response -> registrationResponseReceived(sessionId, response, registerLicence));

  }

  private UserDetailsView getUserDetails(String userId) {
    try {
      return userService.getUserDetailsView(userId).toCompletableFuture().get();
    } catch (InterruptedException | ExecutionException exception) {
      throw new ServiceException("Unable to get userDetailsView for userId " + userId);
    }
  }

  /**
   * handleCallback handles callback from permission service after Ogel submission
   * Updates licence registration reference
   * Handles logic to send confirmation emails to user and to ECJU
   */
  public void handleCallback(String sessionId, CallbackView callbackView) {
    String regRef = callbackView.getRegistrationReference();
    Optional<RegisterLicence> optRegisterLicence = licenceFinderDao.getRegisterLicence(sessionId);
    if (optRegisterLicence.isPresent()) {
      RegisterLicence registerLicence = optRegisterLicence.get();
      registerLicence.setRegistrationReference(regRef);
      licenceFinderDao.saveRegisterLicence(sessionId, registerLicence);

      // Send confirmation emails
      Optional<Customer> optCustomer = licenceFinderDao.getCustomer(sessionId);
      Optional<Site> optSite = licenceFinderDao.getSite(sessionId);
      if (optCustomer.isPresent() && optSite.isPresent()) {
        Customer customer = optCustomer.get();
        Site site = optSite.get();
        String registrationRef = registerLicence.getRegistrationReference();

        // Send confirmation email to user
        String ogelUrl = permissionsFinderUrl + controllers.licencefinder.routes.ViewOgelController.viewOgel(registrationRef);
        String userEmailAddress = registerLicence.getUserEmailAddress();
        String applicantName = registerLicence.getUserFullName();
        notificationClient.sendRegisteredOgelToUserEmail(userEmailAddress, applicantName, ogelUrl);

        // Send confirmation email to Ecju
        String resumeCode = licenceFinderDao.getResumeCode(sessionId);
        String companyName = customer.getCompanyName();
        String siteAddress = site.getAddress();
        notificationClient.sendRegisteredOgelEmailToEcju(userEmailAddress, applicantName, resumeCode, companyName, siteAddress, ogelUrl);
      } else {
        LOGGER.info("Missing Customer/Site information for licence registration reference: " + registerLicence.getRegistrationReference());
      }
    }
  }

  /**
   * Persists current users' Customer/Site data so it can be used later in flow
   */
  public void persistCustomerAndSiteData(String sessionId) {

    String userId = getUserId();
    Optional<Customer> optCustomer = getCustomer(userId);
    if (optCustomer.isPresent()) {
      Customer customer = optCustomer.get();
      licenceFinderDao.saveCustomer(sessionId, customer); // persist customer
      Optional<Site> optSite = getSite(userId, customer.getId());
      if (optSite.isPresent()) {
        Site site = optSite.get();
        licenceFinderDao.saveSite(sessionId, site); // persist site
      } else {
        LOGGER.warn("Not a single Site associated with user/customer: " + userId + "/" + customer.getId());
      }
    } else {
      LOGGER.warn("Not a single Customer associated with user: " + userId);
    }
  }

  /**
   * Private methods
   */

  private ResultsView doGetResultsView(String sessionId, boolean includeResults) {

    String controlCode = licenceFinderDao.getControlCode(sessionId);
    String destinationCountry = licenceFinderDao.getDestinationCountry(sessionId);
    String destinationCountryName = countryProvider.getCountry(destinationCountry).getCountryName();
    List<String> destinationCountries = getExportRouteCountries(sessionId);
    String sourceCountry = licenceFinderDao.getSourceCountry(sessionId);

    List<String> activities = Collections.emptyList();
    boolean showHistoricOgel = true; // set as default
    Optional<QuestionsController.QuestionsForm> optQuestionsForm = licenceFinderDao.getQuestionsForm(sessionId);
    if (optQuestionsForm.isPresent()) {
      QuestionsController.QuestionsForm questionsForm = optQuestionsForm.get();
      activities = getActivityTypes(questionsForm);
      showHistoricOgel = questionsForm.beforeOrLess;
    }

    ResultsView resultView = new ResultsView(controlCode, destinationCountryName);
    CompletionStage<List<ApplicableOgelView>> stage = applicableClient.get(controlCode, sourceCountry, destinationCountries, activities, showHistoricOgel);

    try {
      List<OgelView> ogelViews = stage.thenApply(views -> getOgelViews(views, licenceFinderDao.getUserOgelIdRefMap(sessionId).keySet())).toCompletableFuture().get();
      if (!ogelViews.isEmpty() && includeResults) {
        resultView.setOgelViews(ogelViews);
      }

    } catch (InterruptedException | ExecutionException e) {
      LOGGER.error("getResultsView exception", e);
    }
    return resultView;
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

  private List<String> getExportRouteCountries(String sessionId) {
    List<String> countries = new ArrayList<>();
    String destination = licenceFinderDao.getDestinationCountry(sessionId);
    if (!org.apache.commons.lang3.StringUtils.isBlank(destination)) {
      countries.add(destination);
    }
    String first = licenceFinderDao.getFirstConsigneeCountry(sessionId);
    if (!org.apache.commons.lang3.StringUtils.isBlank(first)) {
      countries.add(first);
    }
    return countries;
  }

  private Map<String, String> getUserOgelIdRefMap(String userId) {
    Map<String, String> ogelIdRefMap = new HashMap<>();
    try {
      List<OgelRegistrationView> views = permissionsService.getOgelRegistrations(userId).toCompletableFuture().get();
      for (OgelRegistrationView view : views) {
        ogelIdRefMap.put(view.getOgelType(), view.getRegistrationReference());
      }
    } catch (InterruptedException | ExecutionException e) {
      LOGGER.error("OgelRegistration exception", e);
    }
    return ogelIdRefMap;
  }

  private List<OgelView> getOgelViews(List<ApplicableOgelView> applicableViews, Set<String> existingOgels) {
    List<OgelView> ogelViews = new ArrayList<>();
    for (ApplicableOgelView applicableView : applicableViews) {
      OgelView view = new OgelView(applicableView);
      if (existingOgels.contains(view.getId())) {
        view.setAlreadyRegistered(true);
      }
      ogelViews.add(view);
    }

    return ogelViews;
  }

  private void registrationResponseReceived(String sessionId, PermissionsServiceImpl.RegistrationResponse response,
                                            RegisterLicence registerLicence) {
    registerLicence.setRequestId(response.getRequestId());
    LOGGER.info("Saving RegisterLicence: " + registerLicence.getUserId());
    licenceFinderDao.saveRegisterLicence(sessionId, registerLicence);
  }

  /**
   * We only return a Customer if there is only one Customer associated with the user
   */
  private Optional<Customer> getCustomer(String userId) {
    Optional<List<CustomerView>> optCustomers = customerService.getCustomersByUserId(userId);
    if (optCustomers.isPresent()) {
      List<CustomerView> customerViews = optCustomers.get();

      // Check for single customer only TODO change when requirement changes
      if (customerViews.size() == 1) {
        CustomerView customerView = customerViews.get(0);
        return Optional.of(new Customer(customerView.getCustomerId(), customerView.getCompanyName()));
      } else {
        LOGGER.warn("Expected user [" + userId + "] to only have 1 associated Customer but found: " + customerViews.size());
      }
    }
    return Optional.empty();
  }

  /**
   * We only return a Site if there is only one Site associated with the user/customer
   */
  private Optional<Site> getSite(String userId, String customerId) {
    Optional<List<SiteView>> optSites = customerService.getSitesByCustomerIdUserId(customerId, userId);
    if (optSites.isPresent()) {
      List<SiteView> sites = optSites.get();
      // Check for single site only TODO change when requirement changes
      if (sites.size() == 1) {
        SiteView siteView = sites.get(0);
        return Optional.of(new Site(siteView.getSiteId(), siteView.getAddress().getPlainText()));
      } else {
        LOGGER.warn("Expected user [" + userId + "] to only have 1 associated Site but found: " + sites.size());
      }
    }
    return Optional.empty();
  }

  private String getUserId() {
    return authManager.getAuthInfoFromContext().getId();
  }

}
