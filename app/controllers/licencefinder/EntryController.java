package controllers.licencefinder;

import com.google.inject.Inject;
import components.common.auth.SamlAuthorizer;
import components.common.auth.SpireAuthManager;
import components.common.auth.SpireSAML2Client;
import components.persistence.LicenceFinderDao;
import components.services.AccountService;
import components.services.LicenceFinderService;
import exceptions.UnknownParameterException;
import models.AccountData;
import org.pac4j.play.java.Secure;
import play.mvc.Controller;
import play.mvc.Result;
import triage.config.ControlEntryConfig;
import triage.config.ControllerConfigService;
import triage.session.SessionService;
import triage.session.TriageSession;

import java.util.Optional;
import java.util.UUID;

@Secure(clients = SpireSAML2Client.CLIENT_NAME, authorizers = SamlAuthorizer.AUTHORIZER_NAME)
public class EntryController extends Controller {

  private final AccountService accountService;
  private final LicenceFinderDao licenceFinderDao;
  private final SpireAuthManager authManager;
  private final LicenceFinderService licenceFinderService;
  private final SessionService sessionService;
  private final ControllerConfigService controllerConfigService;

  @Inject
  public EntryController(AccountService accountService, LicenceFinderDao licenceFinderDao, SpireAuthManager authManager,
                         LicenceFinderService licenceFinderService, SessionService sessionService,
                         ControllerConfigService controllerConfigService) {
    this.accountService = accountService;
    this.licenceFinderDao = licenceFinderDao;
    this.authManager = authManager;
    this.licenceFinderService = licenceFinderService;
    this.sessionService = sessionService;
    this.controllerConfigService = controllerConfigService;
  }

  public Result entry(String controlCode, String resumeCode) {
    String alphanumericResumeCode = resumeCode.replaceAll("[^0-9a-zA-Z]", "").toUpperCase();
    TriageSession triageSession = sessionService.getSessionByResumeCode(alphanumericResumeCode);
    if (triageSession == null) {
      throw UnknownParameterException.unknownResumeCode(resumeCode);
    } else {
      String userId = authManager.getAuthInfoFromContext().getId();
      ControlEntryConfig controlEntryConfig = controllerConfigService.getControlEntryConfigByControlCode(controlCode);
      Optional<AccountData> accountDataOptional = accountService.getAccountData(userId);
      if (accountDataOptional.isPresent()) {
        AccountData accountData = accountDataOptional.get();
        String sessionId = UUID.randomUUID().toString();
        licenceFinderDao.saveControlCode(sessionId, controlEntryConfig.getControlCode());
        licenceFinderDao.saveResumeCode(sessionId, triageSession.getResumeCode());
        licenceFinderDao.saveUserId(sessionId, userId);
        licenceFinderService.persistCustomerAndSiteData(sessionId, accountData.getCustomerView(), accountData.getSiteView());
        return redirect(routes.TradeController.renderTradeForm(sessionId));
      } else {
        return redirect(controllers.routes.StaticContentController.renderInvalidUserAccount());
      }
    }
  }

}

