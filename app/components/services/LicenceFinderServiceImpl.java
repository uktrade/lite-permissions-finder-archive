package components.services;

import com.google.inject.Inject;
import components.common.auth.SpireAuthManager;
import components.common.persistence.StatelessRedisDao;
import components.persistence.LicenceFinderDao;
import components.persistence.enums.SubmissionStatus;
import components.services.ogels.applicable.ApplicableOgelServiceClient;
import jodd.util.ThreadUtil;
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
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.CompletionStage;
import java.util.concurrent.ExecutionException;

public class LicenceFinderServiceImpl implements LicenceFinderService {

  private final LicenceFinderDao licenceFinderDao;
  private final StatelessRedisDao statelessRedisDao;
  private final CustomerService customerService;
  private final SpireAuthManager authManager;
  private final PermissionsService permissionsService;
  private final String permissionsFinderUrl;
  private final ApplicableOgelServiceClient applicableClient;

  @Inject
  public LicenceFinderServiceImpl(LicenceFinderDao licenceFinderDao, CustomerService customerService,
                                  PermissionsService permissionsService, SpireAuthManager authManager,
                                  @com.google.inject.name.Named("permissionsFinderUrl") String permissionsFinderUrl,
                                  StatelessRedisDao statelessRedisDao, ApplicableOgelServiceClient applicableClient) {
    this.licenceFinderDao = licenceFinderDao;
    this.permissionsService = permissionsService;
    this.customerService = customerService;
    this.authManager = authManager;
    this.permissionsFinderUrl = permissionsFinderUrl;
    this.statelessRedisDao = statelessRedisDao;
    this.applicableClient = applicableClient;
  }

  public ResultsView getResultsView(String controlCode, String sourceCountry, List<String> destinationCountries, List<String> activityTypes, boolean showHistoricOgel) {
    ResultsView view = new ResultsView();


    CompletionStage<List<ApplicableOgelView>> stage = applicableClient.get(controlCode, sourceCountry, destinationCountries, activityTypes, showHistoricOgel);

    CompletionStage<List<OgelView>> stage1 = stage.thenApply(x -> getOgelViews(x));

    return view;
  }

  private List<OgelView> getOgelViews(List<ApplicableOgelView> applicableViews) {
    List<OgelView> ogelViews = new ArrayList<>();

    return ogelViews;
  }

  public Set<String> getExistingUserOgelIds(String userId) {
    Set<String> ogelIds = new HashSet<>();
    try {
      List<OgelRegistrationView> views = permissionsService.getOgelRegistrations(userId).toCompletableFuture().get();
      for(OgelRegistrationView view : views) {
        Logger.info("ID: " + view.getOgelType());
        ogelIds.add(view.getOgelType());
      }
    } catch (InterruptedException | ExecutionException e) {
      Logger.error("OgelRegistration exception", e);
    }
    return ogelIds;
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

  public SubmissionStatus getSubmissionStatus(String transactionId) {
    return SubmissionStatus.COMPLETED;
  }

  public long getSecondsSinceRegistrationSubmission(String transactionId) {
    return 0L;
  }

  public Optional<CallbackView.Result> getCallbackResult(String transactionId) {
    return Optional.empty();
  }

  public String getRegistrationRef(String transactionId) {
    return "";
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
