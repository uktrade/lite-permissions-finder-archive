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
    BROKERING_TRANSHIPMENT("tradetypes/brokeringTranshipment.html","Trade controls, trafficking and brokering"),
    IMPORT("tradetypes/import.html","Import licences"),
    CATEGORY_FOOD("categories/food.html","You need to check the rules for your export destination"),
    CATEGORY_ENDANGERED_ANIMALS("categories/endangeredAnimal.html", "You may need a CITES permit"),
    CATEGORY_NON_ENDANGERED_ANIMALS("categories/nonEndangeredAnimal.html", "You may need approval from the destination country"),
    CATEGORY_PLANTS("categories/plant.html", "You may need approval from the destination country"),
    CATEGORY_MEDICINES_DRUGS("categories/medicinesDrugs.html", "You need a licence to export most drugs and medicines"),
    NOT_IMPLEMENTED("notImplemented.html", "This page has not been implemented yet");

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

  public Result renderNoLicenceCultural() {
    return renderStaticHtml(StaticHtml.NO_LICENCE_CULTURAL);
  }

  public Result renderBrokeringTranshipment() {
    return renderStaticHtml(StaticHtml.BROKERING_TRANSHIPMENT);
  }

  public Result renderImport() {
    return renderStaticHtml(StaticHtml.IMPORT);
  }

  public Result renderCategoryFood() {
    return renderStaticHtml(StaticHtml.CATEGORY_FOOD);
  }

  public Result renderCategoryEndangeredAnimals() {
    return renderStaticHtml(StaticHtml.CATEGORY_ENDANGERED_ANIMALS);
  }

  public Result renderCategoryNonEndangeredAnimals() {
    return renderStaticHtml(StaticHtml.CATEGORY_NON_ENDANGERED_ANIMALS);
  }

  public Result renderCategoryPlants() {
    return renderStaticHtml(StaticHtml.CATEGORY_PLANTS);
  }

  public Result renderCategoryMedicinesDrugs() {
    return renderStaticHtml(StaticHtml.CATEGORY_MEDICINES_DRUGS);
  }

  public Result renderNotImplemented() {
    return renderStaticHtml(StaticHtml.NOT_IMPLEMENTED);
  }

}
