package controllers.importcontent;

import static java.util.concurrent.CompletableFuture.completedFuture;

import com.google.inject.Inject;
import components.common.journey.JourneyManager;
import components.persistence.ImportJourneyDao;
import controllers.importcontent.forms.ImportFoodWhatForm;
import controllers.importcontent.forms.ImportForm;
import controllers.importcontent.forms.ImportWhatForm;
import exceptions.FormStateException;
import importcontent.ImportEvents;
import importcontent.ImportQuestion;
import importcontent.ImportUtils;
import importcontent.models.ImportWhat;
import models.importcontent.ImportStageData;
import play.data.Form;
import play.data.FormFactory;
import play.mvc.Controller;
import play.mvc.Result;
import views.html.importcontent.importQuestion;

import java.util.Map;
import java.util.concurrent.CompletionStage;

public class ImportController extends Controller {

  private final JourneyManager journeyManager;
  private final FormFactory formFactory;
  private Map<String, ImportStageData> stageDataMap;
  private final ImportJourneyDao importJourneyDao;

  // Country Spire Codes
  private static final String KAZAKHSTAN_SPIRE_CODE = "CTRY706";
  private static final String BELARUS_SPIRE_CODE = "CTRY31";
  private static final String NORTH_KOREA_SPIRE_CODE = "CTRY383";
  public static final String SYRIA_SPIRE_CODE = "CTRY617";
  public static final String SOMALIA_SPIRE_CODE = "CTRY2004";
  public static final String UKRAINE_SPIRE_CODE = "CTRY1646";

  @Inject
  public ImportController(JourneyManager journeyManager, FormFactory formFactory, ImportJourneyDao importJourneyDao) {
    this.journeyManager = journeyManager;
    this.formFactory = formFactory;
    this.importJourneyDao = importJourneyDao;
    this.stageDataMap = ImportUtils.getStageData();
  }

  public Result renderForm(String stageKey) {
    ImportStageData importStageData = stageDataMap.get(journeyManager.getCurrentInternalStageName());
    return ok(importQuestion.render(formFactory.form(importStageData.getFormClass()), importStageData));
  }

  public CompletionStage<Result> handleSubmit() {
    String stageKey = journeyManager.getCurrentInternalStageName();
    ImportStageData importStageData = stageDataMap.get(stageKey);

    Form<?> form = formFactory.form(importStageData.getFormClass()).bindFromRequest();

    if (form.hasErrors()) {
      return completedFuture(ok(importQuestion.render(form, importStageData)));
    }

    String option = getSelectedOption(form);

    if (importStageData.isValidStageOption(option)) {

      // Get selected country for custom transitions
      String country = importJourneyDao.getImportCountrySelected();

      // Custom transitions for ImportQuestion.WHAT
      if (stageKey.equals(ImportQuestion.WHAT.key())) {
        if (option.equals(ImportWhat.IRON.name())) {
          if (country.equals(KAZAKHSTAN_SPIRE_CODE)) {
            return journeyManager.performTransition(ImportEvents.IMPORT_WHAT_SELECTED, ImportWhat.IRON_KAZAKHSTAN);
          }
        } else if (option.equals(ImportWhat.TEXTILES.name())) {
          if (country.equals(BELARUS_SPIRE_CODE)) {
            return journeyManager.performTransition(ImportEvents.IMPORT_WHAT_SELECTED, ImportWhat.TEXTILES_BELARUS);
          } else if (country.equals(NORTH_KOREA_SPIRE_CODE)) {
            return journeyManager.performTransition(ImportEvents.IMPORT_WHAT_SELECTED, ImportWhat.TEXTILES_NORTH_KOREA);
          }
        }
      }

      return importStageData.completeTransition(journeyManager, option);
    } else {
      throw new FormStateException("Unknown selected option: " + option);
    }
  }

  private String getSelectedOption (Form<?> form) {
    // Converts wildcard parametrised type to a concrete instance using the forms backed type class
    if (form.getBackedType() == ImportForm.class) {
      return ((Form<ImportForm>) form).get().selectedOption;
    }
    else if (form.getBackedType() == ImportWhatForm.class) {
      return ((Form<ImportWhatForm>) form).get().selectedOption;
    }
    else if (form.getBackedType() == ImportFoodWhatForm.class) {
      return ((Form<ImportFoodWhatForm>) form).get().selectedOption;
    }
    else {
      throw new FormStateException("Unknown backed type for form: " + form.getBackedType().toString());
    }
  }

}

