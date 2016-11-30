package utils;

import static importcontent.ImportEvents.IMPORT_FOOD_WHAT_SELECTED;
import static importcontent.ImportEvents.IMPORT_WHAT_SELECTED;
import static importcontent.ImportEvents.IMPORT_WHAT_WHERE_IRON_SELECTED;
import static importcontent.ImportEvents.IMPORT_WHAT_WHERE_TEXTILES_SELECTED;
import static importcontent.ImportEvents.IMPORT_WHERE_SELECTED;
import static importcontent.ImportEvents.IMPORT_YES_NO_SELECTED;
import static importcontent.ImportJourneyDefinitionBuilder.KEY_BELARUS_TEXTILES;
import static importcontent.ImportJourneyDefinitionBuilder.KEY_BELARUS_TEXTILES_QUESTION;
import static importcontent.ImportJourneyDefinitionBuilder.KEY_CHARCOAL;
import static importcontent.ImportJourneyDefinitionBuilder.KEY_CHARCOAL_QUESTION;
import static importcontent.ImportJourneyDefinitionBuilder.KEY_DRUGS;
import static importcontent.ImportJourneyDefinitionBuilder.KEY_DRUGS_QUESTION;
import static importcontent.ImportJourneyDefinitionBuilder.KEY_ENDANGERED;
import static importcontent.ImportJourneyDefinitionBuilder.KEY_ENDANGERED_QUESTION;
import static importcontent.ImportJourneyDefinitionBuilder.KEY_FOOD_WHAT;
import static importcontent.ImportJourneyDefinitionBuilder.KEY_FOOD_WHAT_QUESTION;
import static importcontent.ImportJourneyDefinitionBuilder.KEY_MILITARY_IRAN;
import static importcontent.ImportJourneyDefinitionBuilder.KEY_MILITARY_MYANMAR;
import static importcontent.ImportJourneyDefinitionBuilder.KEY_MILITARY_QUESTION;
import static importcontent.ImportJourneyDefinitionBuilder.KEY_MILITARY_RUSSIA;
import static importcontent.ImportJourneyDefinitionBuilder.KEY_OZONE;
import static importcontent.ImportJourneyDefinitionBuilder.KEY_OZONE_QUESTION;
import static importcontent.ImportJourneyDefinitionBuilder.KEY_SHOT;
import static importcontent.ImportJourneyDefinitionBuilder.KEY_SHOT_QUESTION;
import static importcontent.ImportJourneyDefinitionBuilder.KEY_SUBSTANCES;
import static importcontent.ImportJourneyDefinitionBuilder.KEY_SUBSTANCES_QUESTION;
import static importcontent.ImportJourneyDefinitionBuilder.KEY_WHAT;
import static importcontent.ImportJourneyDefinitionBuilder.KEY_WHAT_QUESTION;
import static importcontent.ImportJourneyDefinitionBuilder.KEY_WHAT_WHERE_IRON;
import static importcontent.ImportJourneyDefinitionBuilder.KEY_WHAT_WHERE_IRON_QUESTION;
import static importcontent.ImportJourneyDefinitionBuilder.KEY_WHAT_WHERE_TEXTILES;
import static importcontent.ImportJourneyDefinitionBuilder.KEY_WHAT_WHERE_TEXTILES_QUESTION;
import static importcontent.ImportJourneyDefinitionBuilder.KEY_WHERE;
import static importcontent.ImportJourneyDefinitionBuilder.KEY_WHERE_QUESTION;

import components.common.journey.JourneyManager;
import importcontent.models.ImportFoodWhat;
import importcontent.models.ImportWhat;
import importcontent.models.ImportWhatWhereIron;
import importcontent.models.ImportWhatWhereTextiles;
import importcontent.models.ImportWhere;
import importcontent.models.ImportYesNo;
import models.importcontent.ImportStageData;
import play.mvc.Result;
import utils.common.SelectOption;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletionStage;
import java.util.function.BiFunction;

public class ImportUtils {

  public static Map<String, ImportStageData> getStageData() {

    Map<String, ImportStageData> dataMap = new HashMap<>();

    // Stages with their own select options
    dataMap.put(KEY_WHERE, new ImportStageData(KEY_WHERE_QUESTION,
        ImportWhere.getSelectOptions(), (jm, opt) -> jm.performTransition(IMPORT_WHERE_SELECTED, ImportWhere.valueOf(opt))));
    dataMap.put(KEY_WHAT, new ImportStageData(KEY_WHAT_QUESTION,
        ImportWhat.getSelectOptions(), (jm, opt) -> jm.performTransition(IMPORT_WHAT_SELECTED, ImportWhat.valueOf(opt))));
    dataMap.put(KEY_FOOD_WHAT, new ImportStageData(KEY_FOOD_WHAT_QUESTION,
        ImportFoodWhat.getSelectOptions(), (jm, opt) -> jm.performTransition(IMPORT_FOOD_WHAT_SELECTED, ImportFoodWhat.valueOf(opt))));
    dataMap.put(KEY_WHAT_WHERE_IRON, new ImportStageData(KEY_WHAT_WHERE_IRON_QUESTION,
        ImportWhatWhereIron.getSelectOptions(), (jm, opt) -> jm.performTransition(IMPORT_WHAT_WHERE_IRON_SELECTED, ImportWhatWhereIron.valueOf(opt))));
    dataMap.put(KEY_WHAT_WHERE_TEXTILES, new ImportStageData(KEY_WHAT_WHERE_TEXTILES_QUESTION,
        ImportWhatWhereTextiles.getSelectOptions(), (jm, opt) -> jm.performTransition(IMPORT_WHAT_WHERE_TEXTILES_SELECTED, ImportWhatWhereTextiles.valueOf(opt))));

    // Stages with yes/no select options
    BiFunction<JourneyManager, String, CompletionStage<Result>> yesNoBiFunction = (jm, opt) -> jm.performTransition(IMPORT_YES_NO_SELECTED, ImportYesNo.valueOf(opt));
    List<SelectOption> options = ImportYesNo.getSelectOptions();
    dataMap.put(KEY_CHARCOAL, new ImportStageData(KEY_CHARCOAL_QUESTION, options, yesNoBiFunction));
    dataMap.put(KEY_MILITARY_IRAN, new ImportStageData(KEY_MILITARY_QUESTION, options, yesNoBiFunction));
    dataMap.put(KEY_MILITARY_RUSSIA, new ImportStageData(KEY_MILITARY_QUESTION, options, yesNoBiFunction));
    dataMap.put(KEY_MILITARY_MYANMAR, new ImportStageData(KEY_MILITARY_QUESTION, options, yesNoBiFunction));
    dataMap.put(KEY_SHOT, new ImportStageData(KEY_SHOT_QUESTION, options, yesNoBiFunction));
    dataMap.put(KEY_SUBSTANCES, new ImportStageData(KEY_SUBSTANCES_QUESTION, options, yesNoBiFunction));
    dataMap.put(KEY_OZONE, new ImportStageData(KEY_OZONE_QUESTION, options, yesNoBiFunction));
    dataMap.put(KEY_DRUGS, new ImportStageData(KEY_DRUGS_QUESTION, options, yesNoBiFunction));
    dataMap.put(KEY_ENDANGERED, new ImportStageData(KEY_ENDANGERED_QUESTION, options, yesNoBiFunction));
    dataMap.put(KEY_BELARUS_TEXTILES, new ImportStageData(KEY_BELARUS_TEXTILES_QUESTION, options, yesNoBiFunction));

    return dataMap;
  }
}
