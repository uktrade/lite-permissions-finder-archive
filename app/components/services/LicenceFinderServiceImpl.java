package components.services;

import com.google.inject.Inject;
import components.common.auth.SpireAuthManager;
import components.common.cache.CountryProvider;
import components.common.persistence.StatelessRedisDao;
import components.persistence.LicenceFinderDao;
import components.services.ogels.applicable.ApplicableOgelServiceClient;
import controllers.licencefinder.QuestionsController;
import jodd.util.ThreadUtil;
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
import java.util.HashMap;
import java.util.HashSet;
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
  private final StatelessRedisDao statelessRedisDao;
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
                                  StatelessRedisDao statelessRedisDao, ApplicableOgelServiceClient applicableClient,
                                  @Named("countryProviderExport") CountryProvider countryProvider) {
    this.licenceFinderDao = licenceFinderDao;
    this.permissionsService = permissionsService;
    this.customerService = customerService;
    this.authManager = authManager;
    this.permissionsFinderUrl = permissionsFinderUrl;
    this.statelessRedisDao = statelessRedisDao;
    this.applicableClient = applicableClient;
    this.countryProvider = countryProvider;
  }

  /**
   * Returns results view with Ogel list omitted
   */
  public ResultsView getNoResultsView(String userId) {
    return doGetResultsView(userId, false);
  }

  /**
   * Returns results view containing users selectable Ogels
   */
  public ResultsView getResultsView(String userId) {
    return doGetResultsView(userId, true);
  }

  /**
   * Attempts to read callback reference set number times/period
   */
  public Optional<String> getRegistrationReference(String transactionId) {
    int count = 0;
    while(count < 5) {
      Optional<RegisterLicence> optRegisterLicence = getRegisterLicence(transactionId);
      if (optRegisterLicence.isPresent()) {
        RegisterLicence registerLicence = optRegisterLicence.get();
        String ref = registerLicence.getRegistrationReference();
        if (!StringUtils.isBlank(ref)) {
          return Optional.of(ref);
        }
      }
      ThreadUtil.sleep(1500);
      count++;
    }
    return Optional.empty();
  }

  /**
   * registerOgel
   */
  public CompletionStage<Void> registerOgel(String transactionId) {
    String userId = getUserId();
    String customerId = licenceFinderDao.getCustomerId();
    String siteId = licenceFinderDao.getSiteId();
    String ogelId = licenceFinderDao.getOgelId();
    String callbackUrl = permissionsFinderUrl + "/licencefinder/registration-callback?transactionId=" + transactionId;

    RegisterLicence registerLicence = new RegisterLicence();
    registerLicence.setTransactionId(transactionId);
    registerLicence.setUserId(userId);
    registerLicence.setOgelId(ogelId);
    registerLicence.setCustomerId(customerId);

    return permissionsService.registerOgel(userId, customerId, siteId, ogelId, callbackUrl)
        .thenAcceptAsync(response -> registrationResponseReceived(transactionId, response, registerLicence));

  }

  /**
   * handleCallback
   */
  public void handleCallback(String transactionId, CallbackView callbackView) {

    String registrationReference = callbackView.getRegistrationReference();

    Logger.info("handleCallback: " + transactionId);
    Logger.info("CallbackView: " + callbackView.getRequestId());
    Logger.info("RegistrationReference: " + registrationReference);

    Optional<RegisterLicence> optRegisterLicence = getRegisterLicence(transactionId);
    if(optRegisterLicence.isPresent()) {
      RegisterLicence registerLicence = optRegisterLicence.get();
      registerLicence.setRegistrationReference(registrationReference);
      saveRegisterLicence(registerLicence);
      Logger.info("RegisterLicence updated with registrationReference: " + registrationReference);
    }
  }

  /**
   * persistCustomerAndSiteData
   */
  public void persistCustomerAndSiteData() {
    String userId = getUserId();
    Optional<String> optCustomerId = getCustomerId(userId);
    if(optCustomerId.isPresent()) {
      String customerId = optCustomerId.get();
      licenceFinderDao.saveCustomerId(customerId); // persist customerId
      Logger.info("CustomerId persisted: " + customerId);
      Optional<String> optSiteId = getSiteId(userId, optCustomerId.get());
      if(optSiteId.isPresent()) {
        String siteId = optSiteId.get();
        licenceFinderDao.saveSiteId(siteId); // persist siteId
        Logger.info("SiteId persisted: " + siteId);
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

  private ResultsView doGetResultsView(String userId, boolean includeResults) {

    String controlCode = licenceFinderDao.getControlCode();
    String destinationCountry = licenceFinderDao.getDestinationCountry();
    String destinationCountryName = countryProvider.getCountry(destinationCountry).getCountryName();
    List<String> destinationCountries = getExportRouteCountries();
    String sourceCountry = licenceFinderDao.getSourceCountry();

    List<String> activities = Collections.emptyList();
    boolean showHistoricOgel = true; // set as default
    Optional<QuestionsController.QuestionsForm> optQuestionsForm = licenceFinderDao.getQuestionsForm();
    if (optQuestionsForm.isPresent()) {
      QuestionsController.QuestionsForm questionsForm = optQuestionsForm.get();
      activities = getActivityTypes(questionsForm);
      showHistoricOgel = questionsForm.beforeOrLess;
    }

    ResultsView resultView = new ResultsView(controlCode, destinationCountryName);
    CompletionStage<List<ApplicableOgelView>> stage = applicableClient.get(controlCode, sourceCountry, destinationCountries, activities, showHistoricOgel);

    try {
      List<OgelView> ogelViews = stage.thenApply(views -> getOgelViews(views, getExistingUserOgelIds(userId))).toCompletableFuture().get();
      if(!ogelViews.isEmpty() && includeResults) {
        resultView.setOgelViews(ogelViews);
      }

    } catch (InterruptedException | ExecutionException e) {
      Logger.error("getResultsView exception", e);
    }
    return resultView;
  }

  private List<String> getActivityTypes(QuestionsController.QuestionsForm questionsForm) {
    Map<OgelActivityType, String> map = new HashMap<>();
    map.put(OgelActivityType.DU_ANY, OgelActivityType.DU_ANY.value());
    map.put(OgelActivityType.EXHIBITION, OgelActivityType.EXHIBITION.value());
    map.put(OgelActivityType.MIL_ANY, OgelActivityType.MIL_ANY.value());
    map.put(OgelActivityType.MIL_GOV, OgelActivityType.MIL_GOV.value());
    map.put(OgelActivityType.REPAIR, OgelActivityType.REPAIR.value());
    if (!questionsForm.forRepair) {
      map.remove(OgelActivityType.REPAIR);
    }
    if (!questionsForm.forExhibition) {
      map.remove(OgelActivityType.EXHIBITION);
    }
    return map.entrySet().stream().map(Map.Entry::getValue).collect(Collectors.toList());
  }

  private List<String> getExportRouteCountries() {
    List<String> countries = new ArrayList<>();
    String destination = licenceFinderDao.getDestinationCountry();
    if (!org.apache.commons.lang3.StringUtils.isBlank(destination)) {
      countries.add(destination);
    }
    String first = licenceFinderDao.getFirstConsigneeCountry();
    if (!org.apache.commons.lang3.StringUtils.isBlank(first)) {
      countries.add(first);
    }
    return countries;
  }

  private Set<String> getExistingUserOgelIds(String userId) {
    Set<String> ogelIds = new HashSet<>();
    try {
      List<OgelRegistrationView> views = permissionsService.getOgelRegistrations(userId).toCompletableFuture().get();
      for(OgelRegistrationView view : views) {
        ogelIds.add(view.getOgelType());
      }
      ogelIds.add("OGL12"); // to enable testing with licence_finder_applicant@test.com user TODO remove once test data is updated to show an already registered Ogel
    } catch (InterruptedException | ExecutionException e) {
      Logger.error("OgelRegistration exception", e);
    }
    return ogelIds;
  }

  private List<OgelView> getOgelViews(List<ApplicableOgelView> applicableViews, Set<String> existingOgels) {
    List<OgelView> ogelViews = new ArrayList<>();
    existingOgels.forEach(System.out::println);
    for(ApplicableOgelView applicableView : applicableViews) {
      OgelView view = new OgelView(applicableView);
      String viewId = view.getId();
      Logger.info("viewId: " + viewId);
      if(existingOgels.contains(viewId)) {
        view.setAlreadyRegistered(true);
      }
      ogelViews.add(view);
    }

    return ogelViews;
  }

  private void registrationResponseReceived(String transactionId, PermissionsServiceImpl.RegistrationResponse response, RegisterLicence registerLicence) {
    Logger.info("Response: " + response.isSuccess());
    Logger.info("RequestId: " + response.getRequestId());
    registerLicence.setRequestId(response.getRequestId());
    saveRegisterLicence(registerLicence);
    statelessRedisDao.writeObject(transactionId, "REGISTER_LICENCE", registerLicence);
  }

  private void saveRegisterLicence(RegisterLicence registerLicence) {
    statelessRedisDao.writeObject(registerLicence.getTransactionId(), "REGISTER_LICENCE", registerLicence);
  }

  private Optional<RegisterLicence> getRegisterLicence(String transactionId) {
    return statelessRedisDao.readObject(transactionId, "REGISTER_LICENCE", RegisterLicence.class);
  }

  /**
   * We only return a CustomerId if there is only one Customer associated with the user
   */
  private Optional<String> getCustomerId(String userId) {
    Optional<List<CustomerView>> optCustomers = customerService.getCustomersByUserId(userId);
    if (optCustomers.isPresent()) {
      List<CustomerView> customers = optCustomers.get();

      // Check for single customer only TODO when we have single Customer user
      if (customers.size() > 0) {
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
      // Check for single site only TODO when we have single Site user
      if (sites.size() > 0) {
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
