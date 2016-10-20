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
    BROKERING_TRANSHIPMENT("tradetypes/brokeringTranshipment.html","Trade controls, trafficking and brokering"),
    IMPORT("tradetypes/import.html","Import licences"),
    CATEGORY_ARTS_CULTURAL_HISTORIC("categories/artsCultural/historic.html", "You may need an Arts Council licence"),
    CATEGORY_ARTS_CULTURAL_NON_HISTORIC("categories/artsCultural/nonHistoric.html", "You need an Arts Council licence to export specific items"),
    CATEGORY_FOOD("categories/food.html","You need to check the rules for your export destination"),
    CATEGORY_ENDANGERED_ANIMALS("categories/endangeredAnimal.html", "You may need a CITES permit"),
    CATEGORY_NON_ENDANGERED_ANIMALS("categories/nonEndangeredAnimal.html", "You may need approval from the destination country"),
    CATEGORY_NON_MILITARY_FIREARMS_CHECK_DESTINATION("categories/nonMilitaryFirearms/needToCheckDestination.html", "You need to check the rules for your destination country"),
    CATEGORY_NON_MILITARY_FIREARMS_NEED_EXPORT_LICENCE("categories/nonMilitaryFirearms/needExportLicence.html", "You need an export licence"),
    CATEGORY_PLANTS("categories/plant.html", "You may need approval from the destination country"),
    CATEGORY_MEDICINES_DRUGS("categories/medicinesDrugs.html", "You need a licence to export most drugs and medicines"),
    CATEGORY_WASTE("categories/waste.html", "You must have a licence to export most types of waste"),
    NOT_IMPLEMENTED("notImplemented.html", "This section is currently under development"),
    VIRTUAL_EU("virtualEU.html", "You do not need a licence");

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

  public Result renderBrokeringTranshipment() {
    return renderStaticHtml(StaticHtml.BROKERING_TRANSHIPMENT);
  }

  public Result renderImport() {
    return renderStaticHtml(StaticHtml.IMPORT);
  }

  public Result renderCategoryArtsCulturalHistoric() {
    return renderStaticHtml(StaticHtml.CATEGORY_ARTS_CULTURAL_HISTORIC);
  }

  public Result renderCategoryArtsCulturalNonHistoric() {
    return renderStaticHtml(StaticHtml.CATEGORY_ARTS_CULTURAL_NON_HISTORIC);
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

  public Result renderCategoryNonMilitaryFirearmsCheckDestination() {
    return renderStaticHtml(StaticHtml.CATEGORY_NON_MILITARY_FIREARMS_CHECK_DESTINATION);
  }

  public Result renderCategoryNonMilitaryFirearmsNeedExportLicence() {
    return renderStaticHtml(StaticHtml.CATEGORY_NON_MILITARY_FIREARMS_NEED_EXPORT_LICENCE);
  }

  public Result renderCategoryPlants() {
    return renderStaticHtml(StaticHtml.CATEGORY_PLANTS);
  }

  public Result renderCategoryMedicinesDrugs() {
    return renderStaticHtml(StaticHtml.CATEGORY_MEDICINES_DRUGS);
  }

  public Result renderCategoryWaste() {
    return renderStaticHtml(StaticHtml.CATEGORY_WASTE);
  }

  public Result renderNotImplemented() {
    return renderStaticHtml(StaticHtml.NOT_IMPLEMENTED);
  }

  public Result renderVirtualEU() {
    return renderStaticHtml(StaticHtml.VIRTUAL_EU);
  }

}
