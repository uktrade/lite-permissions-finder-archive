package importcontent;

import components.common.journey.BackLink;
import components.common.journey.JourneyDefinitionBuilder;
import components.common.journey.JourneyStage;
import controllers.routes;
import importcontent.models.ImportFoodWhat;
import importcontent.models.ImportWhat;
import importcontent.models.ImportWhatWhereIron;
import importcontent.models.ImportWhatWhereTextiles;
import importcontent.models.ImportWhere;
import importcontent.models.ImportYesNo;
import journey.JourneyDefinitionNames;

import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

public class ImportJourneyDefinitionBuilder extends JourneyDefinitionBuilder {

  // Import key/JourneyStage map
  private Map<String, JourneyStage> stageMap;

  /**
   * Create import JourneyStages
   */
  public void initStages() {

    stageMap = new HashMap<>();

    // Create non-static JourneyStages
    stageMap.put(ImportQuestion.WHERE.key(), initStage(ImportQuestion.WHERE));
    stageMap.put(ImportQuestion.WHAT.key(), initStage(ImportQuestion.WHAT));
    stageMap.put(ImportQuestion.CHARCOAL.key(), initStage(ImportQuestion.CHARCOAL));
    stageMap.put(ImportQuestion.MILITARY_IRAN.key(), initStage(ImportQuestion.MILITARY_IRAN));
    stageMap.put(ImportQuestion.MILITARY_RUSSIA.key(), initStage(ImportQuestion.MILITARY_RUSSIA));
    stageMap.put(ImportQuestion.MILITARY_MYANMAR.key(), initStage(ImportQuestion.MILITARY_MYANMAR));
    stageMap.put(ImportQuestion.SHOT.key(), initStage(ImportQuestion.SHOT));
    stageMap.put(ImportQuestion.SUBSTANCES.key(), initStage(ImportQuestion.SUBSTANCES));
    stageMap.put(ImportQuestion.OZONE.key(), initStage(ImportQuestion.OZONE));
    stageMap.put(ImportQuestion.DRUGS.key(), initStage(ImportQuestion.DRUGS));
    stageMap.put(ImportQuestion.FOOD_WHAT.key(), initStage(ImportQuestion.FOOD_WHAT));
    stageMap.put(ImportQuestion.ENDANGERED.key(), initStage(ImportQuestion.ENDANGERED));
    stageMap.put(ImportQuestion.WHAT_WHERE_IRON.key(), initStage(ImportQuestion.WHAT_WHERE_IRON));
    stageMap.put(ImportQuestion.WHAT_WHERE_TEXTILES.key(), initStage(ImportQuestion.WHAT_WHERE_TEXTILES));
    stageMap.put(ImportQuestion.BELARUS_TEXTILES.key(), initStage(ImportQuestion.BELARUS_TEXTILES));

    // Create static JourneyStages (static files named importEp1, importEp2 .. to importEp31)
    for (String key : IntStream.range(1, 32).boxed().map(n -> "importEp" + n).collect(Collectors.toList())) {
      stageMap.put(key, initStaticStage(key));
    }

  }

  @Override
  protected void journeys() {

    defineJourney(JourneyDefinitionNames.IMPORT, stage(ImportQuestion.WHERE), BackLink.to(routes.TradeTypeController.renderForm(), "Where are your items going?"));

    // Where are you importing from?
    atStage(stage(ImportQuestion.WHERE))
        .onEvent(ImportEvents.IMPORT_WHERE_SELECTED)
        .branch()
        .when(ImportWhere.OTHER, moveTo(stage(ImportQuestion.WHAT)))
        .when(ImportWhere.CRIMEA, moveTo(stage("importEp3")))
        .when(ImportWhere.EU, moveTo(stage("importEp15")))
        .when(ImportWhere.SOMALIA, moveTo(stage(ImportQuestion.CHARCOAL)))
        .when(ImportWhere.SYRIA, moveTo(stage("importEp6")))
        .when(ImportWhere.RUSSIA, moveTo(stage(ImportQuestion.MILITARY_RUSSIA)))
        .when(ImportWhere.IRAN, moveTo(stage(ImportQuestion.MILITARY_IRAN)))
        .when(ImportWhere.MYANMAR, moveTo(stage(ImportQuestion.MILITARY_MYANMAR)));

    // Are you importing charcoal or charcoal products?
    atStage(stage(ImportQuestion.CHARCOAL))
        .onEvent(ImportEvents.IMPORT_YES_NO_SELECTED)
        .branch()
        .when(ImportYesNo.YES, moveTo(stage("importEp5")))
        .when(ImportYesNo.NO, moveTo(stage(ImportQuestion.WHAT)));

    // Are you importing military goods or technology? (Iran)
    atStage(stage(ImportQuestion.MILITARY_IRAN))
        .onEvent(ImportEvents.IMPORT_YES_NO_SELECTED)
        .branch()
        .when(ImportYesNo.YES, moveTo(stage("importEp1")))
        .when(ImportYesNo.NO, moveTo(stage(ImportQuestion.WHAT)));

    // Are you importing military goods or technology? (Russia)
    atStage(stage(ImportQuestion.MILITARY_RUSSIA))
        .onEvent(ImportEvents.IMPORT_YES_NO_SELECTED)
        .branch()
        .when(ImportYesNo.YES, moveTo(stage("importEp2")))
        .when(ImportYesNo.NO, moveTo(stage(ImportQuestion.WHAT)));

    // Are you importing military goods or technology? (Myanmar)
    atStage(stage(ImportQuestion.MILITARY_MYANMAR))
        .onEvent(ImportEvents.IMPORT_YES_NO_SELECTED)
        .branch()
        .when(ImportYesNo.YES, moveTo(stage("importEp4")))
        .when(ImportYesNo.NO, moveTo(stage(ImportQuestion.WHAT)));

    // What are you importing?
    atStage(stage(ImportQuestion.WHAT))
        .onEvent(ImportEvents.IMPORT_WHAT_SELECTED)
        .branch()
        .when(ImportWhat.FIREARMS, moveTo(stage(ImportQuestion.SHOT)))
        .when(ImportWhat.TEXTILES, moveTo(stage(ImportQuestion.WHAT_WHERE_TEXTILES)))
        .when(ImportWhat.IRON, moveTo(stage(ImportQuestion.WHAT_WHERE_IRON)))
        .when(ImportWhat.FOOD, moveTo(stage(ImportQuestion.FOOD_WHAT)))
        .when(ImportWhat.MEDICINES, moveTo(stage(ImportQuestion.DRUGS)))
        .when(ImportWhat.NUCLEAR, moveTo(stage("importEp16")))
        .when(ImportWhat.EXPLOSIVES, moveTo(stage("importEp17")))
        .when(ImportWhat.DIAMONDS, moveTo(stage("importEp20")))
        .when(ImportWhat.TORTURE, moveTo(stage("importEp21")))
        .when(ImportWhat.LAND_MINES, moveTo(stage("importEp22")))
        .when(ImportWhat.CHEMICALS, moveTo(stage(ImportQuestion.SUBSTANCES)))
        .when(ImportWhat.NONE_ABOVE, moveTo(stage("importEp31")));

    // Are you importing single-shot rifles or shotguns?
    atStage(stage(ImportQuestion.SHOT))
        .onEvent(ImportEvents.IMPORT_YES_NO_SELECTED)
        .branch()
        .when(ImportYesNo.YES, moveTo(stage("importEp7")))
        .when(ImportYesNo.NO, moveTo(stage("importEp8")));

    // Are you importing substances that potentially cause cancer, eg asbestos?
    atStage(stage(ImportQuestion.SUBSTANCES))
        .onEvent(ImportEvents.IMPORT_YES_NO_SELECTED)
        .branch()
        .when(ImportYesNo.YES, moveTo(stage("importEp28")))
        .when(ImportYesNo.NO, moveTo(stage(ImportQuestion.OZONE)));

    // Are you importing ozone-depleting substances?
    atStage(stage(ImportQuestion.OZONE))
        .onEvent(ImportEvents.IMPORT_YES_NO_SELECTED)
        .branch()
        .when(ImportYesNo.YES, moveTo(stage("importEp29")))
        .when(ImportYesNo.NO, moveTo(stage("importEp30")));

    // Are you importing controlled drugs?
    atStage(stage(ImportQuestion.DRUGS))
        .onEvent(ImportEvents.IMPORT_YES_NO_SELECTED)
        .branch()
        .when(ImportYesNo.YES, moveTo(stage("importEp18")))
        .when(ImportYesNo.NO, moveTo(stage("importEp19")));

    // What are you importing? (food)
    atStage(stage(ImportQuestion.FOOD_WHAT))
        .onEvent(ImportEvents.IMPORT_FOOD_WHAT_SELECTED)
        .branch()
        .when(ImportFoodWhat.FOOD, moveTo(stage("importEp24")))
        .when(ImportFoodWhat.NON_FOOD, moveTo(stage("importEp25")))
        .when(ImportFoodWhat.ANIMALS, moveTo(stage(ImportQuestion.ENDANGERED)))
        .when(ImportFoodWhat.NON_EDIBLE, moveTo(stage("importEp23")));

    // Are the animals endangered?
    atStage(stage(ImportQuestion.ENDANGERED))
        .onEvent(ImportEvents.IMPORT_YES_NO_SELECTED)
        .branch()
        .when(ImportYesNo.YES, moveTo(stage("importEp26")))
        .when(ImportYesNo.NO, moveTo(stage("importEp27")));

    // Where are you importing the iron/steel from?
    atStage(stage(ImportQuestion.WHAT_WHERE_IRON))
        .onEvent(ImportEvents.IMPORT_WHAT_WHERE_IRON_SELECTED)
        .branch()
        .when(ImportWhatWhereIron.KAZAKHSTAN, moveTo(stage("importEp13")))
        .when(ImportWhatWhereIron.OTHER, moveTo(stage("importEp14")));

    // Where are you importing the textiles from?
    atStage(stage(ImportQuestion.WHAT_WHERE_TEXTILES))
        .onEvent(ImportEvents.IMPORT_WHAT_WHERE_TEXTILES_SELECTED)
        .branch()
        .when(ImportWhatWhereTextiles.BELARUS, moveTo(stage(ImportQuestion.BELARUS_TEXTILES)))
        .when(ImportWhatWhereTextiles.NORTH_KOREA, moveTo(stage("importEp10")))
        .when(ImportWhatWhereTextiles.OTHER, moveTo(stage("importEp11")));

    // Are you sending textiles to Belarus for processing before being returned to the UK?
    atStage(stage(ImportQuestion.BELARUS_TEXTILES))
        .onEvent(ImportEvents.IMPORT_YES_NO_SELECTED)
        .branch()
        .when(ImportYesNo.YES, moveTo(stage("importEp9")))
        .when(ImportYesNo.NO, moveTo(stage("importEp12")));
  }

  private JourneyStage initStage(ImportQuestion importQuestion) {
    String stageKey = importQuestion.key();
    return defineStage(stageKey, importQuestion.question(), controllers.importcontent.routes.ImportController.renderForm(stageKey));
  }

  private JourneyStage initStaticStage(String key) {
    return defineStage(key, key, controllers.importcontent.routes.StaticController.render(key));
  }

  private JourneyStage stage(String key) {
    return stageMap.get(key);
  }

  private JourneyStage stage(ImportQuestion importQuestion) {
    return stageMap.get(importQuestion.key());
  }
}
