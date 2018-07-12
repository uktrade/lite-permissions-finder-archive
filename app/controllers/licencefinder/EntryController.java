package controllers.licencefinder;

import com.google.inject.Inject;
import components.common.auth.SamlAuthorizer;
import components.common.auth.SpireAuthManager;
import components.common.auth.SpireSAML2Client;
import components.persistence.LicenceFinderDao;
import components.services.AccountService;
import exceptions.UnknownParameterException;
import models.AccountData;
import models.view.licencefinder.Customer;
import models.view.licencefinder.Site;
import org.apache.commons.lang.StringUtils;
import org.pac4j.play.java.Secure;
import play.mvc.Controller;
import play.mvc.Result;
import triage.session.SessionService;
import triage.session.TriageSession;
import uk.gov.bis.lite.customer.api.view.CustomerView;
import uk.gov.bis.lite.customer.api.view.SiteView;

import java.util.Optional;
import java.util.UUID;

@Secure(clients = SpireSAML2Client.CLIENT_NAME, authorizers = SamlAuthorizer.AUTHORIZER_NAME)
public class EntryController extends Controller {

  private final AccountService accountService;
  private final LicenceFinderDao licenceFinderDao;
  private final SpireAuthManager authManager;
  private final SessionService sessionService;

  @Inject
  public EntryController(AccountService accountService, LicenceFinderDao licenceFinderDao, SpireAuthManager authManager,
                         SessionService sessionService) {
    this.accountService = accountService;
    this.licenceFinderDao = licenceFinderDao;
    this.authManager = authManager;
    this.sessionService = sessionService;
  }

  public Result entry(String controlCode, String resumeCode) {
    String alphanumericResumeCode = resumeCode.replaceAll("[^0-9a-zA-Z]", "").toUpperCase();
    TriageSession triageSession = sessionService.getSessionByResumeCode(alphanumericResumeCode);
    if (triageSession == null) {
      throw UnknownParameterException.unknownResumeCode(resumeCode);
    } else if (!StringUtils.isAlphanumeric(controlCode)) {
      throw UnknownParameterException.unknownControlCode(controlCode);
    } else {
      String userId = authManager.getAuthInfoFromContext().getId();
      Optional<AccountData> accountDataOptional = accountService.getAccountData(userId);
      if (accountDataOptional.isPresent()) {
        AccountData accountData = accountDataOptional.get();
        String sessionId = UUID.randomUUID().toString();
        licenceFinderDao.saveControlCode(sessionId, controlCode);
        licenceFinderDao.saveResumeCode(sessionId, triageSession.getResumeCode());
        licenceFinderDao.saveUserId(sessionId, userId);

        CustomerView customerView = accountData.getCustomerView();
        SiteView siteView = accountData.getSiteView();
        Customer customer = new Customer(customerView.getCustomerId(), customerView.getCompanyName());
        licenceFinderDao.saveCustomer(sessionId, customer);
        Site site = new Site(siteView.getSiteId(), siteView.getAddress().getPlainText());
        licenceFinderDao.saveSite(sessionId, site);

        return redirect(routes.TradeController.renderTradeForm(sessionId));
      } else {
        return redirect(controllers.routes.StaticContentController.renderInvalidUserAccount());
      }
    }
  }

}

