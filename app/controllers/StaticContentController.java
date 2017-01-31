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
    BROKERING("tradetypes/brokering.html","Trade controls, trafficking and brokering"),
    TRANSHIPMENT("tradetypes/transhipment.html","Transhipment"),
    CATEGORY_ARTS_CULTURAL_HISTORIC("categories/artsCultural/historic.html", "You may need an Arts Council licence"),
    CATEGORY_ARTS_CULTURAL_NON_HISTORIC("categories/artsCultural/nonHistoric.html", "You need an Arts Council licence to export specific items"),
    CATEGORY_FOOD("categories/food.html","You need to check the rules for your export destination"),
    CATEGORY_ENDANGERED_ANIMALS("categories/endangeredAnimal.html", "You may need a CITES permit"),
    CATEGORY_NON_ENDANGERED_ANIMALS("categories/nonEndangeredAnimal.html", "You may need approval from the destination country"),
    CATEGORY_NON_MILITARY_TAKING("categories/nonMilitaryFirearms/takingLicence.html", "You need to check the rules for your destination country"),
    CATEGORY_NON_MILITARY_SENDING("categories/nonMilitaryFirearms/sendingLicence.html", "You need to check the rules for your destination country"),
    CATEGORY_NON_MILITARY_NEED_LICENCE("categories/nonMilitaryFirearms/needLicence.html", "You need an export licence"),
    CATEGORY_PLANTS("categories/plant.html", "You may need approval from the destination country"),
    CATEGORY_MEDICINES_DRUGS("categories/medicinesDrugs.html", "You need a licence to export most drugs and medicines"),
    CATEGORY_WASTE("categories/waste.html", "You must have a licence to export most types of waste"),
    NOT_APPLICABLE("notApplicable.html", "No licence available"),
    NOT_IMPLEMENTED("notImplemented.html", "This section is currently under development"),
    SOFTWARE_EXEMPTIONS_NLR1("software/exemptionsNLR1.html", "No licence available"),
    SOFTWARE_EXEMPTIONS_NLR2("software/exemptionsNLR2.html", "No licence available"),
    SOFTWARE_JOURNEY_END_NLR("software/journeyEndNLR.html", "No licence available"),
    TECHNOLOGY_EXEMPTIONS_NLR("technology/exemptionsNLR.html", "No licence available"),
    TECHNOLOGY_JOURNEY_END_NLR("technology/journeyEndNLR.html", "No licence available"),
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

  public Result renderBrokering() {
    return renderStaticHtml(StaticHtml.BROKERING);
  }

  public Result renderTranshipment() {
    return renderStaticHtml(StaticHtml.TRANSHIPMENT);
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

  public Result renderCategoryNonMilitaryTaking() {
    return renderStaticHtml(StaticHtml.CATEGORY_NON_MILITARY_TAKING);
  }

  public Result renderCategoryNonMilitarySending() {
    return renderStaticHtml(StaticHtml.CATEGORY_NON_MILITARY_SENDING);
  }

  public Result renderCategoryNonMilitaryNeedLicence() {
    return renderStaticHtml(StaticHtml.CATEGORY_NON_MILITARY_NEED_LICENCE);
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

  public Result renderNotApplicable() {
    return renderStaticHtml(StaticHtml.NOT_APPLICABLE);
  }

  public Result renderNotImplemented() {
    return renderStaticHtml(StaticHtml.NOT_IMPLEMENTED);
  }

  public Result renderSoftwareExemptionsNLR1() {
    return renderStaticHtml(StaticHtml.SOFTWARE_EXEMPTIONS_NLR1);
  }

  public Result renderSoftwareExemptionsNLR2() {
    return renderStaticHtml(StaticHtml.SOFTWARE_EXEMPTIONS_NLR2);
  }

  public Result renderSoftwareJourneyEndNLR() {
    return renderStaticHtml(StaticHtml.SOFTWARE_JOURNEY_END_NLR);
  }

  public Result renderTechnologyExemptionsNLR() {
    return renderStaticHtml(StaticHtml.TECHNOLOGY_EXEMPTIONS_NLR);
  }

  public Result renderTechnologyJourneyEndNLR() {
    return renderStaticHtml(StaticHtml.TECHNOLOGY_JOURNEY_END_NLR);
  }

  public Result renderVirtualEU() {
    return renderStaticHtml(StaticHtml.VIRTUAL_EU);
  }

}
