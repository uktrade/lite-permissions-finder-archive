package controllers.admin;

import actions.BasicAuthAction;
import com.google.inject.Inject;
import components.services.PingService;
import org.slf4j.LoggerFactory;
import play.mvc.Controller;
import play.mvc.Result;
import play.mvc.With;

/**
 * Actions for system administrators
 */
public class AdminController extends Controller {

  private static final org.slf4j.Logger LOGGER = LoggerFactory.getLogger(AdminController.class);
  private final PingService pingService;

  @Inject
  public AdminController(PingService pingService) {
    this.pingService = pingService;
  }

  @With(BasicAuthAction.class)
  public Result buildInfo() {
    return ok(buildinfo.BuildInfo$.MODULE$.toJson()).as("application/json");
  }

  //@With(BasicAuthAction.class)
  public Result ping() {
    LOGGER.info("ping");
    pingService.pingAudit();

    return ok("ping action completed");
  }
}
