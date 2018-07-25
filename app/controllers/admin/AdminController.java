package controllers.admin;

import actions.BasicAuthAction;
import com.google.inject.Inject;
import components.services.PingService;
import models.admin.PingResult;
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

  private final String PING_XML_TEMPLATE = "<pingdom_http_custom_check><status>%s</status><detail>%s</detail></pingdom_http_custom_check>";

  @Inject
  public AdminController(PingService pingService) {
    this.pingService = pingService;
  }

  @With(BasicAuthAction.class)
  public Result buildInfo() {
    //Use the auto-generated BuildInfo object to produce some JSON describing the build (configured in build.sbt)
    return ok(buildinfo.BuildInfo$.MODULE$.toJson()).as("application/json");
  }

  @With(BasicAuthAction.class)
  public Result cascadePing() {
    LOGGER.info("Admin check request received - getting results from dependent service...");
    PingResult result = pingService.pingServices();
    return ok(String.format(PING_XML_TEMPLATE, result.getStatus(), result.getDetail())).as("application/xml");
  }
}
