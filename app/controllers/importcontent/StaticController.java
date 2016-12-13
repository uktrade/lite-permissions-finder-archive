package controllers.importcontent;


import com.google.common.base.Charsets;
import com.google.common.io.Resources;
import com.google.inject.Inject;
import components.persistence.ImportJourneyDao;
import play.mvc.Controller;
import play.mvc.Result;
import play.twirl.api.Html;
import views.html.importcontent.staticContent;

import java.io.IOException;
import java.net.URL;

public class StaticController extends Controller {

  private final ImportJourneyDao importJourneyDao;

  @Inject
  public StaticController(ImportJourneyDao importJourneyDao) {
    this.importJourneyDao = importJourneyDao;
  }

  public Result render(String key) {
    try {
      URL resource = getClass().getClassLoader().getResource("static/html/importcontent/" + key + ".html");
      if (resource == null) {
        throw new RuntimeException("Not a file: " + key);
      } else {
        return ok(staticContent.render("Importing - Import & Export Licensing", showImportingFromCrimea(),
            new Html(Resources.toString(resource, Charsets.UTF_8))));
      }
    } catch (IOException e) {
      throw new RuntimeException("Failed to read", e);
    }
  }

  private boolean showImportingFromCrimea() {
    String country = importJourneyDao.getImportCountrySelected();
    return country.equals(ImportController.UKRAINE_SPIRE_CODE) || country.equals(ImportController.RUSSIA_SPIRE_CODE);
  }
}

