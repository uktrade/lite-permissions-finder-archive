package components.services;

import com.google.inject.Inject;
import components.common.auth.SpireAuthManager;
import components.common.cache.CountryProvider;
import components.persistence.LicenceFinderDao;
import components.services.ogels.applicable.ApplicableOgelServiceClient;
import controllers.licencefinder.QuestionsController;
import exceptions.ServiceException;
import models.OgelActivityType;
import models.persistence.RegisterLicence;
import models.view.licencefinder.OgelView;
import models.view.licencefinder.ResultsView;
import org.apache.commons.lang.StringUtils;
import play.Logger;
import uk.gov.bis.lite.customer.api.view.CustomerView;
import uk.gov.bis.lite.customer.api.view.SiteView;
import uk.gov.bis.lite.ogel.api.view.ApplicableOgelView;
import uk.gov.bis.lite.permissions.api.view.CallbackView;
import uk.gov.bis.lite.permissions.api.view.OgelRegistrationView;

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

  private final LicenceFinderDao licenceFinderDao;
  private final CustomerService customerService;
  private final SpireAuthManager authManager;
  private final PermissionsService permissionsService;
  private final String permissionsFinderUrl;
  private final ApplicableOgelServiceClient applicableClient;
  private final CountryProvider countryProvider;

  @Inject
  public LicenceFinderServiceImpl(LicenceFinderDao licenceFinderDao, CustomerService customerService,
                                  PermissionsService permissionsService, SpireAuthManager authManager,
                                  @com.google.inject.name.Named("permissionsFinderUrl") String permissionsFinderUrl,
                                  ApplicableOgelServiceClient applicableClient,
                                  @Named("countryProviderExport") CountryProvider countryProvider) {
    this.licenceFinderDao = licenceFinderDao;
    this.permissionsService = permissionsService;
    this.customerService = customerService;
    this.authManager = authManager;
    this.permissionsFinderUrl = permissionsFinderUrl;
    this.applicableClient = applicableClient;
    this.countryProvider = countryProvider;
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
    return doGetResultsView(sessionId,false);
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
  public CompletionStage<Void> registerOgel(String sessionId) {
    String userId = getUserId();
    String customerId = licenceFinderDao.getCustomerId(sessionId);
    String siteId = licenceFinderDao.getSiteId(sessionId);
    String ogelId = licenceFinderDao.getOgelId(sessionId);
    String callbackUrl = permissionsFinderUrl + "/licencefinder/registration-callback?sessionId=" + sessionId;


    if (StringUtils.isBlank(customerId) || StringUtils.isBlank(siteId)) {
      throw new ServiceException("Customer and/or Site could not be determined - a user can only have one associated Customer and only one associated Site");
    }

    RegisterLicence registerLicence = new RegisterLicence();
    registerLicence.setSessionId(sessionId);
    registerLicence.setUserId(userId);
    registerLicence.setOgelId(ogelId);
    registerLicence.setCustomerId(customerId);

    return permissionsService.registerOgel(userId, customerId, siteId, ogelId, callbackUrl)
        .thenAcceptAsync(response -> registrationResponseReceived(sessionId, response, registerLicence));
  }

  /**
   * handleCallback
   */
  public void handleCallback(String sessionId, CallbackView callbackView) {
    String regRef = callbackView.getRegistrationReference();
    Optional<RegisterLicence> optRegisterLicence = licenceFinderDao.getRegisterLicence(sessionId);
    if (optRegisterLicence.isPresent()) {
      RegisterLicence registerLicence = optRegisterLicence.get();
      registerLicence.setRegistrationReference(regRef);
      licenceFinderDao.saveRegisterLicence(sessionId, registerLicence);
      Logger.info("RegisterLicence updated with registrationReference: " + regRef);
    }
  }

  /**
   * persistCustomerAndSiteData
   */
  public void persistCustomerAndSiteData(String sessionId) {
    String userId = getUserId();
    Optional<String> optCustomerId = getCustomerId(userId);
    if (optCustomerId.isPresent()) {
      String customerId = optCustomerId.get();
      licenceFinderDao.saveCustomerId(sessionId, customerId); // persist customerId
      Optional<String> optSiteId = getSiteId(userId, optCustomerId.get());
      if (optSiteId.isPresent()) {
        String siteId = optSiteId.get();
        licenceFinderDao.saveSiteId(sessionId, siteId); // persist siteId
      } else {
        Logger.warn("Not a single Site associated with user/customer: " + userId + "/" + customerId);
      }
    } else {
      Logger.warn("Not a single Customer associated with user: " + userId);
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
      Logger.error("getResultsView exception", e);
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
      Logger.error("OgelRegistration exception", e);
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
    licenceFinderDao.saveRegisterLicence(sessionId, registerLicence);
  }

  /**
   * We only return a CustomerId if there is only one Customer associated with the user
   */
  private Optional<String> getCustomerId(String userId) {
    Optional<List<CustomerView>> optCustomers = customerService.getCustomersByUserId(userId);
    if (optCustomers.isPresent()) {
      List<CustomerView> customers = optCustomers.get();

      // Check for single customer only TODO change when requirement changes
      if (customers.size() == 1) {
        return Optional.of(customers.get(0).getCustomerId());
      } else {
        Logger.warn("Expected user [" + userId + "] to only have 1 associated Customer but found: " + customers.size());
      }
    }
    return Optional.empty();
  }

  /**
   * We only return a SiteId if there is only one Site associated with the user/customer
   */
  private Optional<String> getSiteId(String userId, String customerId) {
    Optional<List<SiteView>> optSites = customerService.getSitesByCustomerIdUserId(customerId, userId);
    if (optSites.isPresent()) {
      List<SiteView> sites = optSites.get();
      // Check for single site only TODO change when requirement changes
      if (sites.size() == 1) {
        return Optional.of(sites.get(0).getSiteId());
      } else {
        Logger.warn("Expected user [" + userId + "] to only have 1 associated Site but found: " + sites.size());
      }
    }
    return Optional.empty();
  }

  private String getUserId() {
    return authManager.getAuthInfoFromContext().getId();
  }

}
