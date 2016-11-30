package utils;

import static importcontent.ImportEvents.IMPORT_FOOD_WHAT_SELECTED;
import static importcontent.ImportEvents.IMPORT_WHAT_SELECTED;
import static importcontent.ImportEvents.IMPORT_WHAT_WHERE_IRON_SELECTED;
import static importcontent.ImportEvents.IMPORT_WHAT_WHERE_TEXTILES_SELECTED;
import static importcontent.ImportEvents.IMPORT_WHERE_SELECTED;
import static importcontent.ImportEvents.IMPORT_YES_NO_SELECTED;

import components.common.journey.JourneyManager;
import importcontent.ImportQuestion;
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
    dataMap.put(ImportQuestion.WHERE.key(), new ImportStageData(ImportQuestion.WHERE.question(),
        ImportWhere.getSelectOptions(), (jm, opt) -> jm.performTransition(IMPORT_WHERE_SELECTED, ImportWhere.valueOf(opt))));
    dataMap.put(ImportQuestion.WHAT.key(), new ImportStageData(ImportQuestion.WHAT.question(),
        ImportWhat.getSelectOptions(), (jm, opt) -> jm.performTransition(IMPORT_WHAT_SELECTED, ImportWhat.valueOf(opt))));
    dataMap.put(ImportQuestion.FOOD_WHAT.key(), new ImportStageData(ImportQuestion.FOOD_WHAT.question(),
        ImportFoodWhat.getSelectOptions(), (jm, opt) -> jm.performTransition(IMPORT_FOOD_WHAT_SELECTED, ImportFoodWhat.valueOf(opt))));
    dataMap.put(ImportQuestion.WHAT_WHERE_IRON.key(), new ImportStageData(ImportQuestion.WHAT_WHERE_IRON.question(),
        ImportWhatWhereIron.getSelectOptions(), (jm, opt) -> jm.performTransition(IMPORT_WHAT_WHERE_IRON_SELECTED, ImportWhatWhereIron.valueOf(opt))));
    dataMap.put(ImportQuestion.WHAT_WHERE_TEXTILES.key(), new ImportStageData(ImportQuestion.WHAT_WHERE_TEXTILES.question(),
        ImportWhatWhereTextiles.getSelectOptions(), (jm, opt) -> jm.performTransition(IMPORT_WHAT_WHERE_TEXTILES_SELECTED, ImportWhatWhereTextiles.valueOf(opt))));

    // Stages with yes/no select options
    BiFunction<JourneyManager, String, CompletionStage<Result>> yesNoBiFunction = (jm, opt) -> jm.performTransition(IMPORT_YES_NO_SELECTED, ImportYesNo.valueOf(opt));
    List<SelectOption> options = ImportYesNo.getSelectOptions();
    dataMap.put(ImportQuestion.CHARCOAL.key(), new ImportStageData(ImportQuestion.CHARCOAL.question(), options, yesNoBiFunction));
    dataMap.put(ImportQuestion.MILITARY_IRAN.key(), new ImportStageData(ImportQuestion.MILITARY_IRAN.question(), options, yesNoBiFunction));
    dataMap.put(ImportQuestion.MILITARY_RUSSIA.key(), new ImportStageData(ImportQuestion.MILITARY_RUSSIA.question(), options, yesNoBiFunction));
    dataMap.put(ImportQuestion.MILITARY_MYANMAR.key(), new ImportStageData(ImportQuestion.MILITARY_MYANMAR.question(), options, yesNoBiFunction));
    dataMap.put(ImportQuestion.SHOT.key(), new ImportStageData(ImportQuestion.SHOT.question(), options, yesNoBiFunction));
    dataMap.put(ImportQuestion.SUBSTANCES.key(), new ImportStageData(ImportQuestion.SUBSTANCES.question(), options, yesNoBiFunction));
    dataMap.put(ImportQuestion.OZONE.key(), new ImportStageData(ImportQuestion.OZONE.question(), options, yesNoBiFunction));
    dataMap.put(ImportQuestion.DRUGS.key(), new ImportStageData(ImportQuestion.DRUGS.question(), options, yesNoBiFunction));
    dataMap.put(ImportQuestion.ENDANGERED.key(), new ImportStageData(ImportQuestion.ENDANGERED.question(), options, yesNoBiFunction));
    dataMap.put(ImportQuestion.BELARUS_TEXTILES.key(), new ImportStageData(ImportQuestion.BELARUS_TEXTILES.question(), options, yesNoBiFunction));

    return dataMap;
  }
}
