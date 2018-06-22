package controllers.licencefinder;

import static java.util.concurrent.CompletableFuture.completedFuture;

import com.google.inject.Inject;
import components.auth.SamlAuthorizer;
import components.common.auth.SpireAuthManager;
import components.common.auth.SpireSAML2Client;
import components.persistence.LicenceFinderDao;
import org.pac4j.play.java.Secure;
import play.mvc.Controller;
import play.mvc.Result;

import java.util.UUID;
import java.util.concurrent.CompletionStage;

@Secure(clients = SpireSAML2Client.CLIENT_NAME, authorizers = SamlAuthorizer.AUTHORIZER_NAME)
public class EntryController extends Controller {

  private final LicenceFinderDao licenceFinderDao;
  private final SpireAuthManager authManager;

  @Inject
  public EntryController(LicenceFinderDao licenceFinderDao, SpireAuthManager authManager) {
    this.licenceFinderDao = licenceFinderDao;
    this.authManager = authManager;
  }

  /**
   * Licence finder flow entry point
   */
  public CompletionStage<Result> entry(String controlCode) {
    String sessionId = UUID.randomUUID().toString();
    licenceFinderDao.saveControlCode(sessionId, controlCode);
    licenceFinderDao.saveUserId(sessionId, authManager.getAuthInfoFromContext().getId());
    return completedFuture(redirect(routes.TradeController.renderTradeForm(sessionId)));
  }

}

