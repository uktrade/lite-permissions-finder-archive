package components.services;

import com.google.inject.Inject;
import com.google.inject.name.Named;
import components.cms.dao.SessionOutcomeDao;
import components.common.auth.SpireAuthManager;
import components.common.client.userservice.UserServiceClientJwt;
import components.services.notification.PermissionsFinderNotificationClient;
import controllers.routes;
import models.enums.OutcomeType;
import models.view.AnswerView;
import models.view.BreadcrumbItemView;
import models.view.BreadcrumbView;
import play.twirl.api.Html;
import triage.config.ControlEntryConfig;
import triage.config.JourneyConfigService;
import triage.config.StageConfig;
import triage.session.SessionOutcome;
import uk.gov.bis.lite.customer.api.view.CustomerView;
import uk.gov.bis.lite.customer.api.view.SiteView;
import uk.gov.bis.lite.user.api.view.UserDetailsView;
import views.html.triage.decontrolBreadcrumb;
import views.html.triage.itemNotFoundBreadcrumb;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.ExecutionException;

public class SessionOutcomeServiceImpl implements SessionOutcomeService {

  private static final DateTimeFormatter DATE_TIME_FORMATTER = DateTimeFormatter.ofPattern("d MMMM uuuu");

  private final String ecjuEmailAddress;
  private final SpireAuthManager authManager;
  private final UserServiceClientJwt userService;
  private final CustomerService customerService;
  private final BreadcrumbViewService breadcrumbViewService;
  private final AnswerViewService answerViewService;
  private final JourneyConfigService journeyConfigService;
  private final SessionOutcomeDao sessionOutcomeDao;
  private final PermissionsFinderNotificationClient permissionsFinderNotificationClient;
  private final views.html.nlr.nlrLetter nlrLetter;

  @Inject
  public SessionOutcomeServiceImpl(@Named("ecjuEmailAddress") String ecjuEmailAddress, SpireAuthManager authManager,
                                   UserServiceClientJwt userService, CustomerService customerService,
                                   BreadcrumbViewService breadcrumbViewService, AnswerViewService answerViewService,
                                   JourneyConfigService journeyConfigService, SessionOutcomeDao sessionOutcomeDao,
                                   PermissionsFinderNotificationClient permissionsFinderNotificationClient,
                                   views.html.nlr.nlrLetter nlrLetter) {
    this.ecjuEmailAddress = ecjuEmailAddress;
    this.authManager = authManager;
    this.userService = userService;
    this.customerService = customerService;
    this.breadcrumbViewService = breadcrumbViewService;
    this.answerViewService = answerViewService;
    this.journeyConfigService = journeyConfigService;
    this.sessionOutcomeDao = sessionOutcomeDao;
    this.permissionsFinderNotificationClient = permissionsFinderNotificationClient;
    this.nlrLetter = nlrLetter;
  }

  @Override
  public void generateNotFoundNlrLetter(String sessionId, String controlEntryId, String resumeCode) {
    String userId = authManager.getAuthInfoFromContext().getId();
    CustomerView customerView = getCustomerId(userId);
    String customerId = customerView.getCustomerId();
    SiteView siteView = getSite(customerId, userId);
    UserDetailsView userDetailsView = getUserDetailsView(userId);
    SiteView.SiteViewAddress address = siteView.getAddress();
    String todayDate = DATE_TIME_FORMATTER.format(LocalDate.now());

    ControlEntryConfig controlEntryConfig = journeyConfigService.getControlEntryConfigById(controlEntryId);
    List<BreadcrumbItemView> breadcrumbItemViews = breadcrumbViewService.createBreadcrumbItemViews(sessionId, controlEntryConfig);

    Html html = nlrLetter.render(resumeCode, userDetailsView, todayDate, address, itemNotFoundBreadcrumb.render(breadcrumbItemViews));
    SessionOutcome sessionOutcome = new SessionOutcome(null, sessionId, userId, customerId, siteView.getSiteId(), OutcomeType.NLR_NOT_FOUND, html.toString());
    sessionOutcomeDao.insert(sessionOutcome);
    String url = routes.NlrController.renderOutcome(sessionId).toString();
    permissionsFinderNotificationClient.sendNlrDocumentToUserEmail(userDetailsView.getContactEmailAddress(), userDetailsView.getFullName(), url);
    permissionsFinderNotificationClient.sendNlrDocumentToEcjuEmail(ecjuEmailAddress, userDetailsView.getFullName(), url, resumeCode, customerView.getCompanyName(), address.getPlainText());
  }

  @Override
  public void generateDecontrolNlrLetter(String sessionId, String stageId, String resumeCode) {
    String userId = authManager.getAuthInfoFromContext().getId();
    CustomerView customerView = getCustomerId(userId);
    String customerId = customerView.getCustomerId();
    SiteView siteView = getSite(customerId, userId);
    UserDetailsView userDetailsView = getUserDetailsView(userId);
    SiteView.SiteViewAddress address = siteView.getAddress();
    String todayDate = DATE_TIME_FORMATTER.format(LocalDate.now());

    StageConfig stageConfig = journeyConfigService.getStageConfigById(stageId);
    List<AnswerView> answerViews = answerViewService.createAnswerViews(stageConfig, true);
    BreadcrumbView breadcrumbView = breadcrumbViewService.createBreadcrumbView(stageId, sessionId);

    Html html = nlrLetter.render(resumeCode, userDetailsView, todayDate, address, decontrolBreadcrumb.render(breadcrumbView, answerViews));
    SessionOutcome sessionOutcome = new SessionOutcome(null, sessionId, userId, customerId, siteView.getSiteId(), OutcomeType.NLR_DECONTROL, html.toString());
    sessionOutcomeDao.insert(sessionOutcome);
    String url = routes.NlrController.renderOutcome(sessionId).toString();
    permissionsFinderNotificationClient.sendNlrDocumentToUserEmail(userDetailsView.getContactEmailAddress(), userDetailsView.getFullName(), url);
    permissionsFinderNotificationClient.sendNlrDocumentToEcjuEmail(ecjuEmailAddress, userDetailsView.getFullName(), url, resumeCode, customerView.getCompanyName(), address.getPlainText());
  }

  private UserDetailsView getUserDetailsView(String userId) {
    try {
      return userService.getUserDetailsView(userId).toCompletableFuture().get();
    } catch (InterruptedException | ExecutionException exception) {
      throw new RuntimeException("Unable to get userDetailsView for userId " + userId, exception);
    }
  }

  private SiteView getSite(String customerId, String userId) {
    Optional<List<SiteView>> optSites = customerService.getSitesByCustomerIdUserId(customerId, userId);
    if (optSites.isPresent()) {
      List<SiteView> sites = optSites.get();
      if (sites.size() == 1) {
        return sites.get(0);
      } else {
        throw new RuntimeException("Expected user [" + userId + "] to only have 1 associated Site but found: " + sites.size());
      }
    }
    throw new RuntimeException("Not a single Site associated with user/customer: " + userId + "/" + customerId);
  }

  private CustomerView getCustomerId(String userId) {
    Optional<List<CustomerView>> optCustomers = customerService.getCustomersByUserId(userId);
    if (optCustomers.isPresent()) {
      List<CustomerView> customers = optCustomers.get();
      if (customers.size() == 1) {
        return customers.get(0);
      } else {
        throw new RuntimeException("Expected user [" + userId + "] to only have 1 associated Customer but found: " + customers.size());
      }
    }
    throw new RuntimeException("Not a single Customer associated with user: " + userId);
  }

}
