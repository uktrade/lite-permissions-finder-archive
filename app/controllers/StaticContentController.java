package controllers;


import com.google.common.base.Charsets;
import com.google.common.io.Resources;
import play.mvc.Controller;
import play.mvc.Result;
import play.twirl.api.Html;
import views.html.staticContent;

import java.io.IOException;
import java.net.URL;

public class StaticContentController extends Controller {

  public enum StaticHtml {

    NO_LICENCE_CULTURAL("noLicenceCultural.html", "No licence required"),
    OGEL_NO_RESULTS("ogelNoResults.html", "We could not find a licence for you"),
    BROKERING_TRANSHIPMENT("tradetypes/brokeringTranshipment.html","Trade controls, trafficking and brokering"),
    IMPORT("tradetypes/import.html","Import licences"),
    CATEGORY_FOOD("categories/food.html","You need to check the rules for your export destination"),
    CATEGORY_ENDANGERED_ANIMAL("categories/endangeredAnimal.html", "You may need a CITES permit"),
    CATEGORY_NON_ENDANGERED_ANIMAL("categories/nonEndangeredAnimal.html", "You may need approval from the destination country"),
    CATEGORY_PLANT("categories/plant.html", "You may need approval from the destination country"),
    CATEGORY_MEDICINES_DRUGS("categories/medicinesDrugs.html", "You need a licence to export most drugs and medicines");

    StaticHtml(String filename, String title) {
      this.filename = filename;
      this.title = title;
    }

    private final String filename;
    private final String title;
  }

  public Result renderStaticHtml(StaticHtml staticHtml) {
    try {
      URL resource = getClass().getClassLoader().getResource("static/html/" + staticHtml.filename);
      if (resource == null) {
        throw new RuntimeException("Not a file: " + staticHtml.filename);
      }

      return ok(staticContent.render(staticHtml.title, new Html(Resources.toString(resource, Charsets.UTF_8))));

    } catch (IOException e) {
      throw new RuntimeException("Failed to read", e);
    }
  }
}
