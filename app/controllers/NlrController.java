package controllers;

import static play.mvc.Results.ok;

import com.google.inject.Inject;
import components.auth.SamlAuthorizer;
import components.client.CustomerService;
import components.common.auth.SpireAuthManager;
import components.common.auth.SpireSAML2Client;
import components.common.client.userservice.UserServiceClientJwt;
import org.pac4j.play.java.Secure;
import play.Logger;
import play.mvc.Result;
import triage.session.SessionService;
import uk.gov.bis.lite.customer.api.view.CustomerView;
import uk.gov.bis.lite.customer.api.view.SiteView;
import uk.gov.bis.lite.user.api.view.UserDetailsView;
import views.html.nlr.nlrLetter;
import views.html.nlr.nlrRegisterSuccess;

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

  @Inject
  public NlrController(SessionService sessionService, SpireAuthManager authManager, UserServiceClientJwt userService, CustomerService customerService) {
    this.sessionService = sessionService;
    this.authManager = authManager;
    this.userService = userService;
    this.customerService = customerService;
  }

  public Result registerNlr(String sessionId, String stageId) {
    String resumeCode = sessionService.getSessionById(sessionId).getResumeCode();
    return ok(nlrRegisterSuccess.render(resumeCode, sessionId));
  }

  //todo
  public Result generateNlrLetter(String sessionId, String resumeCode) throws ExecutionException, InterruptedException {

    String userId = getUserId();
    UserDetailsView userDetailsView = userService.getUserDetailsView(userId).toCompletableFuture().get();
    Optional<SiteView.SiteViewAddress> optSiteAddress = getSiteAddress(userId);

    SiteView.SiteViewAddress address = new SiteView.SiteViewAddress();
    if (optSiteAddress.isPresent()) {
      address = optSiteAddress.get();
    }
    String todayDate = getDate();

    return ok(nlrLetter.render(resumeCode, userDetailsView, todayDate, address));
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