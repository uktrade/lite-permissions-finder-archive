package importcontent;

import static importcontent.ImportEvents.IMPORT_FOOD_WHAT_SELECTED;
import static importcontent.ImportEvents.IMPORT_WHAT_SELECTED;
import static importcontent.ImportEvents.IMPORT_YES_NO_SELECTED;

import components.common.journey.JourneyManager;
import importcontent.models.ImportFoodWhat;
import importcontent.models.ImportWhat;
import models.importcontent.ImportStageData;
import play.mvc.Result;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.CompletionStage;
import java.util.function.BiFunction;

public class ImportUtils {

  public static Map<String, ImportStageData> getStageData() {

    Map<String, ImportStageData> dataMap = new HashMap<>();

    // Stages with their own select options
    dataMap.put(ImportQuestion.WHAT.key(), new ImportStageData(ImportQuestion.WHAT,
        ImportWhat.getSelectOptions(), (jm, opt) -> jm.performTransition(IMPORT_WHAT_SELECTED, ImportWhat.valueOf(opt))));
    dataMap.put(ImportQuestion.FOOD_WHAT.key(), new ImportStageData(ImportQuestion.FOOD_WHAT,
        ImportFoodWhat.getSelectOptions(), (jm, opt) -> jm.performTransition(IMPORT_FOOD_WHAT_SELECTED, ImportFoodWhat.valueOf(opt))));

    // Stages with yes/no select options - derived from ImportYesNo aliases
    BiFunction<JourneyManager, String, CompletionStage<Result>> yesNoBiFunction = (jm, opt) -> jm.performTransition(IMPORT_YES_NO_SELECTED, Boolean.valueOf(opt));
    dataMap.put(ImportQuestion.MILITARY.key(), new ImportStageData(ImportQuestion.MILITARY, yesNoBiFunction));
    dataMap.put(ImportQuestion.CHARCOAL.key(), new ImportStageData(ImportQuestion.CHARCOAL, yesNoBiFunction));
    dataMap.put(ImportQuestion.SHOT.key(), new ImportStageData(ImportQuestion.SHOT, yesNoBiFunction));
    dataMap.put(ImportQuestion.SUBSTANCES.key(), new ImportStageData(ImportQuestion.SUBSTANCES, yesNoBiFunction));
    dataMap.put(ImportQuestion.OZONE.key(), new ImportStageData(ImportQuestion.OZONE, yesNoBiFunction));
    dataMap.put(ImportQuestion.DRUGS.key(), new ImportStageData(ImportQuestion.DRUGS, yesNoBiFunction));
    dataMap.put(ImportQuestion.ENDANGERED.key(), new ImportStageData(ImportQuestion.ENDANGERED, yesNoBiFunction));
    dataMap.put(ImportQuestion.BELARUS_TEXTILES.key(), new ImportStageData(ImportQuestion.BELARUS_TEXTILES, yesNoBiFunction));

    return dataMap;
  }
}
