package controllers.admin;

import actions.BasicAuthAction;
import com.google.inject.Inject;
import components.persistence.AdminDao;
import models.admin.ApplicationCodeInfo;
import models.admin.TransactionInfo;
import play.libs.Json;
import play.mvc.Controller;
import play.mvc.Result;
import play.mvc.With;

/**
 * Actions for system administrators
 */
public class AdminController extends Controller {
  private final AdminDao adminDao;

  @Inject
  public AdminController(AdminDao adminDao) {
    this.adminDao = adminDao;
  }

  public Result buildInfo() {
    //Use the auto-generated BuildInfo object to produce some JSON describing the build (configured in build.sbt)
    return ok(buildinfo.BuildInfo$.MODULE$.toJson()).as("application/json");
  }

  @With(BasicAuthAction.class)
  public Result permissionsFinderTransactions() {
    return ok(Json.toJson(adminDao.getPermissionsFinderTransactions()));
  }

  @With(BasicAuthAction.class)
  public Result permissionsFinderTransactionById(String id) {
    TransactionInfo info = adminDao.getPermissionsFinderTransactionById(id);
    if (info != null) {
      return ok(Json.toJson(info));
    } else {
      return notFound(Json.newObject());
    }
  }

  @With(BasicAuthAction.class)
  public Result applicationCodes() {
    return ok(Json.toJson(adminDao.getApplicationCodes()));
  }

  @With(BasicAuthAction.class)
  public Result applicationCodeByCode(String code) {
    ApplicationCodeInfo info = adminDao.getApplicationCodeByCode(code);
    if (info != null) {
      return ok(Json.toJson(info));
    } else {
      return notFound(Json.newObject());
    }
  }

}
