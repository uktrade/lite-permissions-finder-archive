package controllers.ogel;

import static play.mvc.Results.ok;

import com.google.inject.Inject;
import components.common.client.CountryServiceClient;
import components.persistence.PermissionsFinderDao;
import play.libs.concurrent.HttpExecutionContext;
import play.mvc.Result;
import views.html.ogel.ogelNoResults;

public class OgelNoResultsController {

  private final PermissionsFinderDao dao;
  private final HttpExecutionContext ec;
  private final CountryServiceClient countryServiceClient;

  @Inject
  public OgelNoResultsController(PermissionsFinderDao dao, HttpExecutionContext ec, CountryServiceClient countryServiceClient) {
    this.dao = dao;
    this.ec = ec;
    this.countryServiceClient = countryServiceClient;
  }

  public Result render() {
      // TODO IELS-608 look up via country service (or cache) for country name, fix when multi country design variant completed
      return ok(ogelNoResults.render("DUMMY RATING", "DUMMY COUNTRY"));
  }
}
