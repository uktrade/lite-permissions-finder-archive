package components.services;

import com.google.inject.Inject;
import com.google.inject.name.Named;
import components.cms.dao.SessionOutcomeDao;
import components.common.client.userservice.UserServiceClientJwt;
import components.services.notification.PermissionsFinderNotificationClient;
import controllers.routes;
import exceptions.InvalidUserAccountException;
import models.enums.OutcomeType;
import models.view.AnswerView;
import models.view.BreadcrumbItemView;
import models.view.BreadcrumbView;
import models.view.SubAnswerView;
import play.twirl.api.Html;
import triage.config.ControlEntryConfig;
import triage.config.JourneyConfigService;
import triage.config.StageConfig;
import triage.session.SessionOutcome;
import triage.text.HtmlRenderOption;
import uk.gov.bis.lite.customer.api.view.CustomerView;
import uk.gov.bis.lite.customer.api.view.SiteView;
import uk.gov.bis.lite.user.api.view.UserDetailsView;
import views.html.triage.decontrolBreadcrumb;
import views.html.triage.itemNotFoundBreadcrumb;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.ExecutionException;

public class SessionOutcomeServiceImpl implements SessionOutcomeService {

  private static final DateTimeFormatter DATE_TIME_FORMATTER = DateTimeFormatter.ofPattern("d MMMM uuuu");

  private final String permissionsFinderUrl;
  private final String ecjuEmailAddress;
  private final UserServiceClientJwt userService;
  private final CustomerService customerService;
  private final BreadcrumbViewService breadcrumbViewService;
  private final AnswerViewService answerViewService;
  private final JourneyConfigService journeyConfigService;
  private final SessionOutcomeDao sessionOutcomeDao;
  private final PermissionsFinderNotificationClient permissionsFinderNotificationClient;
  private final RenderService renderService;
  private final views.html.nlr.nlrLetter nlrLetter;
  private final views.html.triage.listedOutcomeJourney listedOutcomeJourney;

  @Inject
  public SessionOutcomeServiceImpl(@Named("permissionsFinderUrl") String permissionsFinderUrl,
                                   @Named("ecjuEmailAddress") String ecjuEmailAddress, UserServiceClientJwt userService,
                                   CustomerService customerService, BreadcrumbViewService breadcrumbViewService,
                                   AnswerViewService answerViewService, JourneyConfigService journeyConfigService,
                                   SessionOutcomeDao sessionOutcomeDao,
                                   PermissionsFinderNotificationClient permissionsFinderNotificationClient,
                                   RenderService renderService, views.html.nlr.nlrLetter nlrLetter,
                                   views.html.triage.listedOutcomeJourney listedOutcomeJourney) {
    this.permissionsFinderUrl = permissionsFinderUrl;
    this.ecjuEmailAddress = ecjuEmailAddress;
    this.userService = userService;
    this.customerService = customerService;
    this.breadcrumbViewService = breadcrumbViewService;
    this.answerViewService = answerViewService;
    this.journeyConfigService = journeyConfigService;
    this.sessionOutcomeDao = sessionOutcomeDao;
    this.permissionsFinderNotificationClient = permissionsFinderNotificationClient;
    this.renderService = renderService;
    this.nlrLetter = nlrLetter;
    this.listedOutcomeJourney = listedOutcomeJourney;
  }

  @Override
  public void generateItemListedOutcome(String userId, String sessionId,
                                        String controlEntryId) throws InvalidUserAccountException {
    ControlEntryConfig controlEntryConfig = journeyConfigService.getControlEntryConfigById(controlEntryId);
    List<BreadcrumbItemView> breadcrumbViews = breadcrumbViewService.createBreadcrumbItemViews(sessionId, controlEntryConfig, false, HtmlRenderOption.OMIT_LINKS);
    String controlCode = controlEntryConfig.getControlCode();
    String description = renderService.getFullDescription(controlEntryConfig, HtmlRenderOption.OMIT_LINKS);
    List<SubAnswerView> subAnswerViews = answerViewService.createSubAnswerViews(controlEntryConfig, false);
    Html html = listedOutcomeJourney.render(breadcrumbViews, controlCode, description, subAnswerViews);

    CustomerView customerView = getCustomerId(userId);
    String customerId = customerView.getCustomerId();
    SiteView siteView = getSite(customerId, userId);
    String id = createOutcomeId();
    SessionOutcome sessionOutcome = new SessionOutcome(id, sessionId, userId, customerId, siteView.getSiteId(),
        OutcomeType.CONTROL_ENTRY_FOUND, html.toString());
    sessionOutcomeDao.insert(sessionOutcome);
  }

  @Override
  public String generateNotFoundNlrLetter(String userId, String sessionId, String controlEntryId, String resumeCode,
                                          String description) throws InvalidUserAccountException {
    ControlEntryConfig controlEntryConfig = journeyConfigService.getControlEntryConfigById(controlEntryId);
    List<BreadcrumbItemView> breadcrumbItemViews = breadcrumbViewService.createBreadcrumbItemViews(sessionId, controlEntryConfig, false, HtmlRenderOption.OMIT_LINKS);
    Html nlrBreadcrumb = itemNotFoundBreadcrumb.render(breadcrumbItemViews, null);

    return generateLetter(userId, sessionId, resumeCode, OutcomeType.NLR_NOT_FOUND, nlrBreadcrumb, description);
  }

  @Override
  public String generateDecontrolNlrLetter(String userId, String sessionId, String stageId, String resumeCode,
                                           String description) throws InvalidUserAccountException {
    StageConfig stageConfig = journeyConfigService.getStageConfigById(stageId);
    List<AnswerView> answerViews = answerViewService.createAnswerViews(stageConfig, true);
    BreadcrumbView breadcrumbView = breadcrumbViewService.createBreadcrumbView(stageId, sessionId, false, HtmlRenderOption.OMIT_LINKS);
    Html nlrBreadcrumb = decontrolBreadcrumb.render(null, breadcrumbView, answerViews);

    return generateLetter(userId, sessionId, resumeCode, OutcomeType.NLR_DECONTROL, nlrBreadcrumb, description);
  }

  private String generateLetter(String userId, String sessionId, String resumeCode, OutcomeType outcomeType,
                                Html nlrBreadcrumb, String description) throws InvalidUserAccountException {
    CustomerView customerView = getCustomerId(userId);
    String customerId = customerView.getCustomerId();
    SiteView siteView = getSite(customerId, userId);
    UserDetailsView userDetailsView = getUserDetailsView(userId);
    SiteView.SiteViewAddress address = siteView.getAddress();
    String todayDate = DATE_TIME_FORMATTER.format(LocalDate.now());

    Html html = nlrLetter.render(resumeCode, userDetailsView, todayDate, address, nlrBreadcrumb, description);
    String id = createOutcomeId();
    SessionOutcome sessionOutcome = new SessionOutcome(id, sessionId, userId, customerId, siteView.getSiteId(), outcomeType, html.toString());
    sessionOutcomeDao.insert(sessionOutcome);
    String url = permissionsFinderUrl + routes.ViewOutcomeController.renderOutcome(id).toString();
    permissionsFinderNotificationClient.sendNlrDocumentToUserEmail(userDetailsView.getContactEmailAddress(),
        userDetailsView.getFullName(), url);
    permissionsFinderNotificationClient.sendNlrDocumentToEcjuEmail(ecjuEmailAddress,
        userDetailsView.getContactEmailAddress(), userDetailsView.getFullName(), url, resumeCode,
        customerView.getCompanyName(), address.getPlainText());
    return id;
  }

  private String createOutcomeId() {
    return "out_" + UUID.randomUUID().toString().replace("-", "");
  }

  private UserDetailsView getUserDetailsView(String userId) {
    try {
      return userService.getUserDetailsView(userId).toCompletableFuture().get();
    } catch (InterruptedException | ExecutionException exception) {
      throw new RuntimeException("Unable to get userDetailsView for userId " + userId, exception);
    }
  }

  private SiteView getSite(String customerId, String userId) throws InvalidUserAccountException {
    Optional<List<SiteView>> optSites = customerService.getSitesByCustomerIdUserId(customerId, userId);
    if (optSites.isPresent()) {
      List<SiteView> sites = optSites.get();
      if (sites.size() == 1) {
        return sites.get(0);
      } else {
        throw new InvalidUserAccountException("Expected user [" + userId + "] to only have 1 associated Site but found: " + sites.size());
      }
    }
    throw new InvalidUserAccountException("Not a single Site associated with user/customer: " + userId + "/" + customerId);
  }

  private CustomerView getCustomerId(String userId) throws InvalidUserAccountException {
    Optional<List<CustomerView>> optCustomers = customerService.getCustomersByUserId(userId);
    if (optCustomers.isPresent()) {
      List<CustomerView> customers = optCustomers.get();
      if (customers.size() == 1) {
        return customers.get(0);
      } else {
        throw new InvalidUserAccountException("Expected user [" + userId + "] to only have 1 associated Customer but found: " + customers.size());
      }
    }
    throw new InvalidUserAccountException("Not a single Customer associated with user: " + userId);
  }

}
