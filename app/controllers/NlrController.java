package controllers;

import static play.mvc.Results.badRequest;
import static play.mvc.Results.ok;

import com.google.inject.Inject;
import components.auth.SamlAuthorizer;
import components.common.auth.SpireAuthManager;
import components.common.auth.SpireSAML2Client;
import components.common.client.userservice.UserServiceClientJwt;
import components.services.AnswerViewService;
import components.services.BreadcrumbViewService;
import components.services.CustomerService;
import models.view.AnswerView;
import models.view.BreadcrumbItemView;
import models.view.BreadcrumbView;
import org.pac4j.play.java.Secure;
import play.Logger;
import play.mvc.Result;
import triage.config.ControlEntryConfig;
import triage.config.JourneyConfigService;
import triage.config.StageConfig;
import triage.session.SessionService;
import uk.gov.bis.lite.customer.api.view.CustomerView;
import uk.gov.bis.lite.customer.api.view.SiteView;
import uk.gov.bis.lite.user.api.view.UserDetailsView;
import views.html.nlr.nlrLetter;
import views.html.nlr.nlrRegisterSuccess;
import views.html.triage.decontrolBreadcrumb;
import views.html.triage.itemNotFoundBreadcrumb;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.ExecutionException;

@Secure(clients = SpireSAML2Client.CLIENT_NAME, authorizers = SamlAuthorizer.AUTHORIZER_NAME)
public class NlrController {

  private final SessionService sessionService;
  private final SpireAuthManager authManager;
  private final UserServiceClientJwt userService;
  private final CustomerService customerService;
  private final BreadcrumbViewService breadcrumbViewService;
  private final AnswerViewService answerViewService;
  private final JourneyConfigService journeyConfigService;

  @Inject
  public NlrController(SessionService sessionService, SpireAuthManager authManager,
                       UserServiceClientJwt userService, CustomerService customerService,
                       BreadcrumbViewService breadcrumbViewService, AnswerViewService answerViewService,
                       JourneyConfigService journeyConfigService) {
    this.sessionService = sessionService;
    this.authManager = authManager;
    this.userService = userService;
    this.customerService = customerService;
    this.breadcrumbViewService = breadcrumbViewService;
    this.answerViewService = answerViewService;
    this.journeyConfigService = journeyConfigService;
  }

  public Result registerNlr(String sessionId, String stageId, String outcomeType) {
    String resumeCode = sessionService.getSessionById(sessionId).getResumeCode();
    return ok(nlrRegisterSuccess.render(resumeCode, outcomeType, stageId));
  }

  public Result generateNlrLetter(String stageIdOrControlEntryId, String resumeCode, String outcomeType) throws ExecutionException, InterruptedException {
    String userId = getUserId();
    UserDetailsView userDetailsView = userService.getUserDetailsView(userId).toCompletableFuture().get();
    Optional<SiteView.SiteViewAddress> optSiteAddress = getSiteAddress(userId);

    SiteView.SiteViewAddress address = optSiteAddress.orElse(new SiteView.SiteViewAddress());
    String todayDate = getDate();

    switch (outcomeType) {
      case "DECONTROL": {
        StageConfig stageConfig = journeyConfigService.getStageConfigById(stageIdOrControlEntryId);
        List<AnswerView> answerViews = answerViewService.createAnswerViews(stageConfig, true);
        BreadcrumbView breadcrumbView = breadcrumbViewService.createBreadcrumbView(stageIdOrControlEntryId);
        return ok(nlrLetter.render(resumeCode, userDetailsView, todayDate, address, outcomeType, decontrolBreadcrumb.render(breadcrumbView, answerViews)));
      }
      case "ITEM_NOT_FOUND": {
        ControlEntryConfig controlEntryConfig = journeyConfigService.getControlEntryConfigById(stageIdOrControlEntryId);
        List<BreadcrumbItemView> breadcrumbItemViews = breadcrumbViewService.createBreadcrumbItemViews(controlEntryConfig);
        return ok(nlrLetter.render(resumeCode, userDetailsView, todayDate, address, outcomeType, itemNotFoundBreadcrumb.render(breadcrumbItemViews)));
      }
      default: {
        return badRequest("Failed to generate NLR letter");
      }
    }
  }

  private Optional<SiteView.SiteViewAddress> getSiteAddress(String userId) {
    Optional<String> optCustomerId = getCustomer(userId);

    if (optCustomerId.isPresent()) {
      Optional<SiteView> optSiteView = getSite(optCustomerId.get(), userId);
      if (optSiteView.isPresent()) {
        return Optional.of(optSiteView.get().getAddress());
      }
    }
    return Optional.empty();
  }

  private Optional<SiteView> getSite(String customerId, String userId) {
    Optional<List<SiteView>> optSites = customerService.getSitesByCustomerIdUserId(customerId, userId);
    if (optSites.isPresent()) {
      List<SiteView> sites = optSites.get();

      if (sites.size() == 1) {
        return Optional.of(sites.get(0));
      } else {
        throw new RuntimeException("Expected user [" + userId + "] to only have 1 associated Site but found: " + sites.size());
      }
    }
    Logger.warn("Not a single Site associated with user/customer: " + userId + "/" + customerId);
    return Optional.empty();
  }

  private Optional<String> getCustomer(String userId) {
    Optional<List<CustomerView>> optCustomers = customerService.getCustomersByUserId(userId);

    if (optCustomers.isPresent()) {
      List<CustomerView> customers = optCustomers.get();
      if (customers.size() == 1) {
        return Optional.of(customers.get(0).getCustomerId());
      } else {
        throw new RuntimeException("Expected user [" + userId + "] to only have 1 associated Customer but found: " + customers.size());
      }
    }
    Logger.warn("Not a single Customer associated with user: " + userId);
    return Optional.empty();
  }

  private String getDate() {
    DateTimeFormatter formatter = DateTimeFormatter.ofPattern("d MMMM uuuu");
    LocalDate localDate = LocalDate.now();
    return formatter.format(localDate);
  }

  private String getUserId() {
    return authManager.getAuthInfoFromContext().getId();
  }

}