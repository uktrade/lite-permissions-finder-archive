package components.services;

import com.google.inject.Inject;
import com.google.inject.name.Named;
import components.cms.dao.SessionOutcomeDao;
import components.common.client.UserServiceClientJwt;
import components.services.notification.PermissionsFinderNotificationClient;
import controllers.routes;
import exceptions.ServiceException;
import models.AccountData;
import models.enums.SessionOutcomeType;
import models.view.AnswerView;
import models.view.BreadcrumbItemView;
import models.view.BreadcrumbView;
import models.view.SubAnswerView;
import play.twirl.api.Html;
import triage.config.ControlEntryConfig;
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
import java.util.UUID;
import java.util.concurrent.ExecutionException;

public class SessionOutcomeServiceImpl implements SessionOutcomeService {

  private static final DateTimeFormatter DATE_TIME_FORMATTER = DateTimeFormatter.ofPattern("d MMMM uuuu");

  private final String permissionsFinderUrl;
  private final UserServiceClientJwt userService;
  private final BreadcrumbViewService breadcrumbViewService;
  private final AnswerViewService answerViewService;
  private final SessionOutcomeDao sessionOutcomeDao;
  private final PermissionsFinderNotificationClient permissionsFinderNotificationClient;
  private final RenderService renderService;
  private final views.html.nlr.nlrLetter nlrLetter;
  private final views.html.triage.listedOutcomeJourney listedOutcomeJourney;

  @Inject
  public SessionOutcomeServiceImpl(@Named("permissionsFinderUrl") String permissionsFinderUrl,
                                   UserServiceClientJwt userService, BreadcrumbViewService breadcrumbViewService,
                                   AnswerViewService answerViewService, SessionOutcomeDao sessionOutcomeDao,
                                   PermissionsFinderNotificationClient permissionsFinderNotificationClient,
                                   RenderService renderService, views.html.nlr.nlrLetter nlrLetter,
                                   views.html.triage.listedOutcomeJourney listedOutcomeJourney) {
    this.permissionsFinderUrl = permissionsFinderUrl;
    this.userService = userService;
    this.breadcrumbViewService = breadcrumbViewService;
    this.answerViewService = answerViewService;
    this.sessionOutcomeDao = sessionOutcomeDao;
    this.permissionsFinderNotificationClient = permissionsFinderNotificationClient;
    this.renderService = renderService;
    this.nlrLetter = nlrLetter;
    this.listedOutcomeJourney = listedOutcomeJourney;
  }

  @Override
  public void generateItemListedOutcome(String sessionId, String userId, AccountData accountData,
                                        ControlEntryConfig controlEntryConfig) {
    List<BreadcrumbItemView> breadcrumbViews = breadcrumbViewService.createBreadcrumbItemViews(sessionId, controlEntryConfig, false, HtmlRenderOption.OMIT_LINKS);
    String controlCode = controlEntryConfig.getControlCode();
    String description = renderService.getFullDescription(controlEntryConfig, HtmlRenderOption.OMIT_LINKS);
    List<SubAnswerView> subAnswerViews = answerViewService.createSubAnswerViews(controlEntryConfig, false);
    Html html = listedOutcomeJourney.render(breadcrumbViews, controlCode, description, subAnswerViews);
    String id = createOutcomeId();
    SessionOutcome sessionOutcome = new SessionOutcome(id, sessionId, userId,
        accountData.getCustomerView().getCustomerId(), accountData.getSiteView().getSiteId(),
        SessionOutcomeType.CONTROL_ENTRY_FOUND, html.toString());
    sessionOutcomeDao.insert(sessionOutcome);
  }

  @Override
  public String generateNotFoundNlrLetter(String sessionId, String userId, AccountData accountData,
                                          ControlEntryConfig controlEntryConfig, String resumeCode, Html description) {
    List<BreadcrumbItemView> breadcrumbItemViews = breadcrumbViewService.createBreadcrumbItemViews(sessionId, controlEntryConfig, false, HtmlRenderOption.OMIT_LINKS);
    Html nlrBreadcrumb = itemNotFoundBreadcrumb.render(breadcrumbItemViews, null);

    return generateLetter(sessionId, userId, accountData, resumeCode, SessionOutcomeType.NLR_NOT_FOUND, nlrBreadcrumb, description);
  }

  @Override
  public String generateDecontrolNlrLetter(String sessionId, String userId, AccountData accountData,
                                           StageConfig stageConfig, String resumeCode, Html description) {
    List<AnswerView> answerViews = answerViewService.createAnswerViews(stageConfig, true);
    BreadcrumbView breadcrumbView = breadcrumbViewService.createBreadcrumbView(stageConfig, sessionId, false, HtmlRenderOption.OMIT_LINKS);
    Html nlrBreadcrumb = decontrolBreadcrumb.render(null, breadcrumbView, answerViews);

    return generateLetter(sessionId, userId, accountData, resumeCode, SessionOutcomeType.NLR_DECONTROL, nlrBreadcrumb, description);
  }

  private String generateLetter(String sessionId, String userId, AccountData accountData, String resumeCode,
                                SessionOutcomeType outcomeType, Html nlrBreadcrumb, Html description) {
    CustomerView customerView = accountData.getCustomerView();
    SiteView siteView = accountData.getSiteView();

    UserDetailsView userDetailsView = getUserDetailsView(userId);
    SiteView.SiteViewAddress address = siteView.getAddress();
    String todayDate = DATE_TIME_FORMATTER.format(LocalDate.now());

    Html html = nlrLetter.render(resumeCode, userDetailsView, customerView, todayDate, address, nlrBreadcrumb, description);
    String id = createOutcomeId();
    SessionOutcome sessionOutcome = new SessionOutcome(id, sessionId, userId, customerView.getCustomerId(),
        siteView.getSiteId(), outcomeType, html.toString());
    sessionOutcomeDao.insert(sessionOutcome);
    String url = permissionsFinderUrl + routes.ViewOutcomeController.renderOutcome(id).toString();
    permissionsFinderNotificationClient.sendNlrDocumentToUserEmail(userDetailsView.getContactEmailAddress(),
        userDetailsView.getFullName(), url);
    permissionsFinderNotificationClient.sendNlrDocumentToEcjuEmail(userDetailsView.getContactEmailAddress(),
        userDetailsView.getFullName(), url, resumeCode, customerView.getCompanyName(), address.getPlainText());
    return id;
  }

  private String createOutcomeId() {
    return "out_" + UUID.randomUUID().toString().replace("-", "");
  }

  private UserDetailsView getUserDetailsView(String userId) {
    try {
      return userService.getUserDetailsView(userId).toCompletableFuture().get();
    } catch (InterruptedException | ExecutionException exception) {
      throw new ServiceException("Unable to get userDetailsView for userId " + userId, exception);
    }
  }

}
