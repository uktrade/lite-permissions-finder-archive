package importcontent;

import components.common.journey.BackLink;
import components.common.journey.JourneyDefinitionBuilder;
import components.common.journey.JourneyStage;
import controllers.importcontent.ImportEvents;
import controllers.importcontent.StaticController;
import controllers.routes;
import importcontent.models.ImportFoodWhat;
import importcontent.models.ImportWhat;
import importcontent.models.ImportWhatWhereIron;
import importcontent.models.ImportWhatWhereTextiles;
import importcontent.models.ImportWhere;
import importcontent.models.ImportYesNo;
import journey.JourneyDefinitionNames;
import play.Logger;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

public class ImportJourneyDefinitionBuilder extends JourneyDefinitionBuilder {

  public static final String KEY_WHERE = "importWhere";
  public static final String KEY_WHERE_QUESTION = "Where are you importing from?";
  public static final String KEY_WHAT = "importWhat";
  public static final String KEY_WHAT_QUESTION = "What are you importing?";
  public static final String KEY_CHARCOAL = "importCharcoal";
  public static final String KEY_CHARCOAL_QUESTION = "Are you importing charcoal or charcoal products?";
  public static final String KEY_MILITARY_IRAN = "importMilitaryIran";
  public static final String KEY_MILITARY_RUSSIA = "importMilitaryRussia";
  public static final String KEY_MILITARY_MYANMAR = "importMilitaryMyanmar";
  public static final String KEY_MILITARY_QUESTION = "Are you importing military goods or technology?";
  public static final String KEY_SHOT = "importShot";
  public static final String KEY_SHOT_QUESTION = "Are you importing single-shot rifles or shotguns?";
  public static final String KEY_SUBSTANCES = "importSubstances";
  public static final String KEY_SUBSTANCES_QUESTION = "Are you importing substances that potentially cause cancer, eg asbestos?";
  public static final String KEY_OZONE = "importOzone";
  public static final String KEY_OZONE_QUESTION = "Are you importing ozone-depleting substances?";
  public static final String KEY_DRUGS = "importDrugs";
  public static final String KEY_DRUGS_QUESTION = "Are you importing controlled drugs?";
  public static final String KEY_FOOD_WHAT = "importFoodWhat";
  public static final String KEY_FOOD_WHAT_QUESTION = "What are you importing?";
  public static final String KEY_ENDANGERED = "importEndangered";
  public static final String KEY_ENDANGERED_QUESTION = "Are the animals endangered?";
  public static final String KEY_WHAT_WHERE_IRON = "importWhatWhereIron";
  public static final String KEY_WHAT_WHERE_IRON_QUESTION = "Where are you importing the iron/steel from?";
  public static final String KEY_WHAT_WHERE_TEXTILES = "importWhatWhereTextiles";
  public static final String KEY_WHAT_WHERE_TEXTILES_QUESTION = "Where are you importing the textiles from?";
  public static final String KEY_BELARUS_TEXTILES = "importBelarusTextiles";
  public static final String KEY_BELARUS_TEXTILES_QUESTION = "Are you sending textiles to Belarus for processing before being returned to the UK?";

  private Map<String, JourneyStage> stages = new TreeMap<>();


  public void initStages() {

    stages = new HashMap<>();
    stages.put(KEY_WHERE, initStage(KEY_WHERE, KEY_WHERE_QUESTION));
    stages.put(KEY_WHAT, initStage(KEY_WHAT, KEY_WHAT_QUESTION));
    stages.put(KEY_CHARCOAL, initStage(KEY_CHARCOAL, KEY_CHARCOAL_QUESTION));
    stages.put(KEY_MILITARY_IRAN, initStage(KEY_MILITARY_IRAN, KEY_MILITARY_QUESTION));
    stages.put(KEY_MILITARY_RUSSIA, initStage(KEY_MILITARY_RUSSIA, KEY_MILITARY_QUESTION));
    stages.put(KEY_MILITARY_MYANMAR, initStage(KEY_MILITARY_MYANMAR, KEY_MILITARY_QUESTION));
    stages.put(KEY_SHOT, initStage(KEY_SHOT, KEY_SHOT_QUESTION));
    stages.put(KEY_SUBSTANCES, initStage(KEY_SUBSTANCES, KEY_SUBSTANCES_QUESTION));
    stages.put(KEY_OZONE, initStage(KEY_OZONE, KEY_OZONE_QUESTION));
    stages.put(KEY_DRUGS, initStage(KEY_DRUGS, KEY_DRUGS_QUESTION));
    stages.put(KEY_FOOD_WHAT, initStage(KEY_FOOD_WHAT, KEY_FOOD_WHAT_QUESTION));
    stages.put(KEY_ENDANGERED, initStage(KEY_ENDANGERED, KEY_ENDANGERED_QUESTION));
    stages.put(KEY_WHAT_WHERE_IRON, initStage(KEY_WHAT_WHERE_IRON, KEY_WHAT_WHERE_IRON_QUESTION));
    stages.put(KEY_WHAT_WHERE_TEXTILES, initStage(KEY_WHAT_WHERE_TEXTILES, KEY_WHAT_WHERE_TEXTILES_QUESTION));
    stages.put(KEY_BELARUS_TEXTILES, initStage(KEY_BELARUS_TEXTILES, KEY_BELARUS_TEXTILES_QUESTION));

    List<String> staticKeys = StaticController.getImportStaticKeys();
    for (String key : staticKeys) {
      stages.put(key, initStaticStage(key));
    }

    stages.forEach((key, stage) -> {
      Logger.info("Key : " + key);
    });
  }

  private JourneyStage initStage(String key, String question) {
    return defineStage(key, question, controllers.importcontent.routes.ImportController.renderForm(key));
  }

  private JourneyStage initStaticStage(String key) {
    return defineStage(key, key, controllers.importcontent.routes.StaticController.render(key));
  }

  private JourneyStage stage(String key) {
    return stages.get(key);
  }

  @Override
  protected void journeys() {

    //initStages();

    defineJourney(JourneyDefinitionNames.IMPORT, stage("importWhere"), BackLink.to(routes.TradeTypeController.renderForm(), "Where are your items going?"));

    // Where are you importing from?
    atStage(stage("importWhere"))
        .onEvent(ImportEvents.IMPORT_WHERE_SELECTED)
        .branch()
        .when(ImportWhere.OTHER, moveTo(stage("importWhat")))
        .when(ImportWhere.CRIMEA, moveTo(stage("importEp3")))
        .when(ImportWhere.EU, moveTo(stage("importEp15")))
        .when(ImportWhere.SOMALIA, moveTo(stage("importCharcoal")))
        .when(ImportWhere.SYRIA, moveTo(stage("importEp6")))
        .when(ImportWhere.RUSSIA, moveTo(stage("importMilitaryRussia")))
        .when(ImportWhere.IRAN, moveTo(stage("importMilitaryIran")))
        .when(ImportWhere.MYANMAR, moveTo(stage("importMilitaryMyanmar")));

    // Are you importing charcoal or charcoal products?
    atStage(stage("importCharcoal"))
        .onEvent(ImportEvents.IMPORT_CHARCOAL_SELECTED)
        .branch()
        .when(ImportYesNo.YES, moveTo(stage("importEp5")))
        .when(ImportYesNo.NO, moveTo(stage("importWhat")));

    // Are you importing military goods or technology? (Iran)
    atStage(stage("importMilitaryIran"))
        .onEvent(ImportEvents.IMPORT_MILITARY_IRAN_SELECTED)
        .branch()
        .when(ImportYesNo.YES, moveTo(stage("importEp1")))
        .when(ImportYesNo.NO, moveTo(stage("importWhat")));

    // Are you importing military goods or technology? (Russia)
    atStage(stage("importMilitaryRussia"))
        .onEvent(ImportEvents.IMPORT_MILITARY_RUSSIA_SELECTED)
        .branch()
        .when(ImportYesNo.YES, moveTo(stage("importEp2")))
        .when(ImportYesNo.NO, moveTo(stage("importWhat")));

    // Are you importing military goods or technology? (Myanmar)
    atStage(stage("importMilitaryMyanmar"))
        .onEvent(ImportEvents.IMPORT_MILITARY_MYANMAR_SELECTED)
        .branch()
        .when(ImportYesNo.YES, moveTo(stage("importEp4")))
        .when(ImportYesNo.NO, moveTo(stage("importWhat")));

    // What are you importing?
    atStage(stage("importWhat"))
        .onEvent(ImportEvents.IMPORT_WHAT_SELECTED)
        .branch()
        .when(ImportWhat.FIREARMS, moveTo(stage("importShot")))
        .when(ImportWhat.TEXTILES, moveTo(stage("importWhatWhereTextiles")))
        .when(ImportWhat.IRON, moveTo(stage("importWhatWhereIron")))
        .when(ImportWhat.FOOD, moveTo(stage("importFoodWhat")))
        .when(ImportWhat.MEDICINES, moveTo(stage("importDrugs")))
        .when(ImportWhat.NUCLEAR, moveTo(stage("importEp16")))
        .when(ImportWhat.EXPLOSIVES, moveTo(stage("importEp17")))
        .when(ImportWhat.DIAMONDS, moveTo(stage("importEp20")))
        .when(ImportWhat.TORTURE, moveTo(stage("importEp21")))
        .when(ImportWhat.LAND_MINES, moveTo(stage("importEp22")))
        .when(ImportWhat.CHEMICALS, moveTo(stage("importSubstances")))
        .when(ImportWhat.NONE_ABOVE, moveTo(stage("importEp31")));

    // Are you importing single-shot rifles or shotguns?
    atStage(stage("importShot"))
        .onEvent(ImportEvents.IMPORT_SHOT_SELECTED)
        .branch()
        .when(ImportYesNo.YES, moveTo(stage("importEp7")))
        .when(ImportYesNo.NO, moveTo(stage("importEp8")));

    // Are you importing substances that potentially cause cancer, eg asbestos?
    atStage(stage("importSubstances"))
        .onEvent(ImportEvents.IMPORT_SUBSTANCES_SELECTED)
        .branch()
        .when(ImportYesNo.YES, moveTo(stage("importEp28")))
        .when(ImportYesNo.NO, moveTo(stage("importOzone")));

    // Are you importing ozone-depleting substances?
    atStage(stage("importOzone"))
        .onEvent(ImportEvents.IMPORT_OZONE_SELECTED)
        .branch()
        .when(ImportYesNo.YES, moveTo(stage("importEp29")))
        .when(ImportYesNo.NO, moveTo(stage("importEp30")));

    // Are you importing controlled drugs?
    atStage(stage("importDrugs"))
        .onEvent(ImportEvents.IMPORT_DRUGS_SELECTED)
        .branch()
        .when(ImportYesNo.YES, moveTo(stage("importEp18")))
        .when(ImportYesNo.NO, moveTo(stage("importEp19")));

    // What are you importing? (food)
    atStage(stage("importFoodWhat"))
        .onEvent(ImportEvents.IMPORT_FOOD_WHAT_SELECTED)
        .branch()
        .when(ImportFoodWhat.FOOD, moveTo(stage("importEp24")))
        .when(ImportFoodWhat.NON_FOOD, moveTo(stage("importEp25")))
        .when(ImportFoodWhat.ANIMALS, moveTo(stage("importEndangered")))
        .when(ImportFoodWhat.NON_EDIBLE, moveTo(stage("importEp23")));

    // Are the animals endangered?
    atStage(stage("importEndangered"))
        .onEvent(ImportEvents.IMPORT_ENDANGERED_SELECTED)
        .branch()
        .when(ImportYesNo.YES, moveTo(stage("importEp26")))
        .when(ImportYesNo.NO, moveTo(stage("importEp27")));

    // Where are you importing the iron/steel from?
    atStage(stage("importWhatWhereIron"))
        .onEvent(ImportEvents.IMPORT_WHAT_WHERE_IRON_SELECTED)
        .branch()
        .when(ImportWhatWhereIron.KAZAKHSTAN, moveTo(stage("importEp13")))
        .when(ImportWhatWhereIron.OTHER, moveTo(stage("importEp14")));

    // Where are you importing the textiles from?
    atStage(stage("importWhatWhereTextiles"))
        .onEvent(ImportEvents.IMPORT_WHAT_WHERE_TEXTILES_SELECTED)
        .branch()
        .when(ImportWhatWhereTextiles.BELARUS, moveTo(stage("importBelarusTextiles")))
        .when(ImportWhatWhereTextiles.NORTH_KOREA, moveTo(stage("importEp10")))
        .when(ImportWhatWhereTextiles.OTHER, moveTo(stage("importEp11")));

    // Are you sending textiles to Belarus for processing before being returned to the UK?
    atStage(stage("importBelarusTextiles"))
        .onEvent(ImportEvents.IMPORT_BELARUS_TEXTILES_SELECTED)
        .branch()
        .when(ImportYesNo.YES, moveTo(stage("importEp9")))
        .when(ImportYesNo.NO, moveTo(stage("importEp12")));
  }
}
