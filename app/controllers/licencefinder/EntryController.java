package controllers.licencefinder;

import static java.util.concurrent.CompletableFuture.completedFuture;

import com.google.inject.Inject;
import components.common.auth.SamlAuthorizer;
import components.common.auth.SpireAuthManager;
import components.common.auth.SpireSAML2Client;
import components.persistence.LicenceFinderDao;
import components.services.LicenceFinderService;
import org.apache.commons.lang3.StringUtils;
import org.pac4j.play.java.Secure;
import play.mvc.Controller;
import play.mvc.Result;

import java.util.UUID;
import java.util.concurrent.CompletionStage;

@Secure(clients = SpireSAML2Client.CLIENT_NAME, authorizers = SamlAuthorizer.AUTHORIZER_NAME)
public class EntryController extends Controller {

  private final LicenceFinderDao licenceFinderDao;
  private final SpireAuthManager authManager;
  private final LicenceFinderService licenceFinderService;

  @Inject
  public EntryController(LicenceFinderDao licenceFinderDao, SpireAuthManager authManager, LicenceFinderService licenceFinderService) {
    this.licenceFinderDao = licenceFinderDao;
    this.authManager = authManager;
    this.licenceFinderService = licenceFinderService;
  }

  /**
   * Licence finder flow entry point
   */
  public CompletionStage<Result> entry(String controlCode, String resumeCode) {
    String sessionId = UUID.randomUUID().toString();
    licenceFinderDao.saveControlCode(sessionId, controlCode);
    if(!StringUtils.isBlank(resumeCode)) {
      licenceFinderDao.saveResumeCode(sessionId, resumeCode);
    }
    licenceFinderDao.saveUserId(sessionId, authManager.getAuthInfoFromContext().getId());

    // Take this opportunity in flow to save users CustomerId and SiteId
    licenceFinderService.persistCustomerAndSiteData(sessionId);

    return completedFuture(redirect(routes.TradeController.renderTradeForm(sessionId)));
  }

}

