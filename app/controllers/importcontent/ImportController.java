package controllers.importcontent;

import static importcontent.ImportEvents.IMPORT_BELARUS_TEXTILES_SELECTED;
import static importcontent.ImportEvents.IMPORT_CHARCOAL_SELECTED;
import static importcontent.ImportEvents.IMPORT_DRUGS_SELECTED;
import static importcontent.ImportEvents.IMPORT_ENDANGERED_SELECTED;
import static importcontent.ImportEvents.IMPORT_FOOD_WHAT_SELECTED;
import static importcontent.ImportEvents.IMPORT_MILITARY_IRAN_SELECTED;
import static importcontent.ImportEvents.IMPORT_MILITARY_MYANMAR_SELECTED;
import static importcontent.ImportEvents.IMPORT_MILITARY_RUSSIA_SELECTED;
import static importcontent.ImportEvents.IMPORT_OZONE_SELECTED;
import static importcontent.ImportEvents.IMPORT_SHOT_SELECTED;
import static importcontent.ImportEvents.IMPORT_SUBSTANCES_SELECTED;
import static importcontent.ImportEvents.IMPORT_WHAT_SELECTED;
import static importcontent.ImportEvents.IMPORT_WHAT_WHERE_IRON_SELECTED;
import static importcontent.ImportEvents.IMPORT_WHAT_WHERE_TEXTILES_SELECTED;
import static importcontent.ImportEvents.IMPORT_WHERE_SELECTED;
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
import static java.util.concurrent.CompletableFuture.completedFuture;

import com.google.inject.Inject;
import components.common.journey.JourneyManager;
import exceptions.FormStateException;
import importcontent.models.ImportFoodWhat;
import importcontent.models.ImportWhat;
import importcontent.models.ImportWhatWhereIron;
import importcontent.models.ImportWhatWhereTextiles;
import importcontent.models.ImportWhere;
import importcontent.models.ImportYesNo;
import models.importcontent.ImportStageData;
import play.Logger;
import play.data.Form;
import play.data.FormFactory;
import play.data.validation.Constraints.Required;
import play.mvc.Controller;
import play.mvc.Result;
import views.html.importcontent.importQuestion;

import java.util.Map;
import java.util.TreeMap;
import java.util.concurrent.CompletionStage;

public class ImportController extends Controller {

  private final JourneyManager journeyManager;
  private final FormFactory formFactory;
  private Map<String, ImportStageData> stageDataMap;

  @Inject
  public ImportController(JourneyManager journeyManager, FormFactory formFactory) {
    this.journeyManager = journeyManager;
    this.formFactory = formFactory;
    initPageData();
  }

  public Result renderForm(String stageKey) {
    return ok(importQuestion.render(formFactory.form(), getImportStageData(stageKey)));
  }

  public CompletionStage<Result> handleSubmit() {
    Form<ImportStageForm> form = formFactory.form(ImportStageForm.class).bindFromRequest();
    if (form.hasErrors()) {
      return completedFuture(ok(importQuestion.render(form, getImportStageData(getStageKeyFromRequest()))));
    }
    return completeTransition(form.get().stageKey, form.get().selectedOption);
  }

  /**
   * ImportStageForm
   */
  public static class ImportStageForm {

    @Required(message = "Please select one option")
    private String selectedOption;
    private String stageKey;

    public String getSelectedOption() {
      return selectedOption;
    }

    public void setSelectedOption(String selectedOption) {
      this.selectedOption = selectedOption;
    }

    public String getStageKey() {
      return stageKey;
    }

    public void setStageKey(String stageKey) {
      this.stageKey = stageKey;
    }
  }

  private ImportStageData getImportStageData(String stageKey) {
    return stageDataMap.get(stageKey);
  }

  private void initPageData() {
    stageDataMap = new TreeMap<>();
    stageDataMap.put(KEY_WHERE, new ImportStageData(KEY_WHERE, KEY_WHERE_QUESTION, ImportWhere.getSelectOptions()));
    stageDataMap.put(KEY_WHAT, new ImportStageData(KEY_WHAT, KEY_WHAT_QUESTION, ImportWhat.getSelectOptions()));
    stageDataMap.put(KEY_CHARCOAL, new ImportStageData(KEY_CHARCOAL, KEY_CHARCOAL_QUESTION, ImportYesNo.getSelectOptions()));
    stageDataMap.put(KEY_MILITARY_IRAN, new ImportStageData(KEY_MILITARY_IRAN, KEY_MILITARY_QUESTION, ImportYesNo.getSelectOptions()));
    stageDataMap.put(KEY_MILITARY_RUSSIA, new ImportStageData(KEY_MILITARY_RUSSIA, KEY_MILITARY_QUESTION, ImportYesNo.getSelectOptions()));
    stageDataMap.put(KEY_MILITARY_MYANMAR, new ImportStageData(KEY_MILITARY_MYANMAR, KEY_MILITARY_QUESTION, ImportYesNo.getSelectOptions()));
    stageDataMap.put(KEY_SHOT, new ImportStageData(KEY_SHOT, KEY_SHOT_QUESTION, ImportYesNo.getSelectOptions()));
    stageDataMap.put(KEY_SUBSTANCES, new ImportStageData(KEY_SUBSTANCES, KEY_SUBSTANCES_QUESTION, ImportYesNo.getSelectOptions()));
    stageDataMap.put(KEY_OZONE, new ImportStageData(KEY_OZONE, KEY_OZONE_QUESTION, ImportYesNo.getSelectOptions()));
    stageDataMap.put(KEY_DRUGS, new ImportStageData(KEY_DRUGS, KEY_DRUGS_QUESTION, ImportYesNo.getSelectOptions()));
    stageDataMap.put(KEY_FOOD_WHAT, new ImportStageData(KEY_FOOD_WHAT, KEY_FOOD_WHAT_QUESTION, ImportFoodWhat.getSelectOptions()));
    stageDataMap.put(KEY_ENDANGERED, new ImportStageData(KEY_ENDANGERED, KEY_ENDANGERED_QUESTION, ImportYesNo.getSelectOptions()));
    stageDataMap.put(KEY_WHAT_WHERE_IRON, new ImportStageData(KEY_WHAT_WHERE_IRON, KEY_WHAT_WHERE_IRON_QUESTION, ImportWhatWhereIron.getSelectOptions()));
    stageDataMap.put(KEY_WHAT_WHERE_TEXTILES, new ImportStageData(KEY_WHAT_WHERE_TEXTILES, KEY_WHAT_WHERE_TEXTILES_QUESTION, ImportWhatWhereTextiles.getSelectOptions()));
    stageDataMap.put(KEY_BELARUS_TEXTILES, new ImportStageData(KEY_BELARUS_TEXTILES, KEY_BELARUS_TEXTILES_QUESTION, ImportYesNo.getSelectOptions()));
  }

  private CompletionStage<Result> completeTransition(String stageKey, String option) {
    Logger.info("ImportController.completeTransition: " + stageKey + "/" + option);
    if (stageKey.equals(KEY_WHERE) && ImportWhere.getMatched(option).isPresent()) {
      return journeyManager.performTransition(IMPORT_WHERE_SELECTED, ImportWhere.valueOf(option));
    } else if (stageKey.equals(KEY_WHAT) && ImportWhat.getMatched(option).isPresent()) {
      return journeyManager.performTransition(IMPORT_WHAT_SELECTED, ImportWhat.valueOf(option));
    } else if (stageKey.equals(KEY_CHARCOAL) && ImportYesNo.getMatched(option).isPresent()) {
      return journeyManager.performTransition(IMPORT_CHARCOAL_SELECTED, ImportYesNo.valueOf(option));
    } else if (stageKey.equals(KEY_MILITARY_IRAN) && ImportYesNo.getMatched(option).isPresent()) {
      return journeyManager.performTransition(IMPORT_MILITARY_IRAN_SELECTED, ImportYesNo.valueOf(option));
    } else if (stageKey.equals(KEY_MILITARY_RUSSIA) && ImportYesNo.getMatched(option).isPresent()) {
      return journeyManager.performTransition(IMPORT_MILITARY_RUSSIA_SELECTED, ImportYesNo.valueOf(option));
    } else if (stageKey.equals(KEY_MILITARY_MYANMAR) && ImportYesNo.getMatched(option).isPresent()) {
      return journeyManager.performTransition(IMPORT_MILITARY_MYANMAR_SELECTED, ImportYesNo.valueOf(option));
    } else if (stageKey.equals(KEY_SHOT) && ImportYesNo.getMatched(option).isPresent()) {
      return journeyManager.performTransition(IMPORT_SHOT_SELECTED, ImportYesNo.valueOf(option));
    } else if (stageKey.equals(KEY_SUBSTANCES) && ImportYesNo.getMatched(option).isPresent()) {
      return journeyManager.performTransition(IMPORT_SUBSTANCES_SELECTED, ImportYesNo.valueOf(option));
    } else if (stageKey.equals(KEY_OZONE) && ImportYesNo.getMatched(option).isPresent()) {
      return journeyManager.performTransition(IMPORT_OZONE_SELECTED, ImportYesNo.valueOf(option));
    } else if (stageKey.equals(KEY_DRUGS) && ImportYesNo.getMatched(option).isPresent()) {
      return journeyManager.performTransition(IMPORT_DRUGS_SELECTED, ImportYesNo.valueOf(option));
    } else if (stageKey.equals(KEY_FOOD_WHAT) && ImportFoodWhat.getMatched(option).isPresent()) {
      return journeyManager.performTransition(IMPORT_FOOD_WHAT_SELECTED, ImportFoodWhat.valueOf(option));
    } else if (stageKey.equals(KEY_ENDANGERED) && ImportYesNo.getMatched(option).isPresent()) {
      return journeyManager.performTransition(IMPORT_ENDANGERED_SELECTED, ImportYesNo.valueOf(option));
    } else if (stageKey.equals(KEY_WHAT_WHERE_IRON) && ImportWhatWhereIron.getMatched(option).isPresent()) {
      return journeyManager.performTransition(IMPORT_WHAT_WHERE_IRON_SELECTED, ImportWhatWhereIron.valueOf(option));
    } else if (stageKey.equals(KEY_WHAT_WHERE_TEXTILES) && ImportWhatWhereTextiles.getMatched(option).isPresent()) {
      return journeyManager.performTransition(IMPORT_WHAT_WHERE_TEXTILES_SELECTED, ImportWhatWhereTextiles.valueOf(option));
    } else if (stageKey.equals(KEY_BELARUS_TEXTILES) && ImportYesNo.getMatched(option).isPresent()) {
      return journeyManager.performTransition(IMPORT_BELARUS_TEXTILES_SELECTED, ImportYesNo.valueOf(option));
    }
    throw new FormStateException("Unknown selectedOption: " + option);
  }

  private String getStageKeyFromRequest() {
    return request().body().asFormUrlEncoded().get("stageKey")[0];
  }
}

