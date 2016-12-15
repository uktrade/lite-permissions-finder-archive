package controllers.importcontent;

import static java.util.concurrent.CompletableFuture.completedFuture;

import com.google.inject.Inject;
import components.common.journey.JourneyManager;
import components.persistence.ImportJourneyDao;
import exceptions.FormStateException;
import importcontent.ImportEvents;
import importcontent.ImportQuestion;
import importcontent.ImportUtils;
import importcontent.models.ImportMilitaryYesNo;
import importcontent.models.ImportWhat;
import models.importcontent.ImportStageData;
import org.apache.commons.lang3.StringUtils;
import play.Logger;
import play.data.Form;
import play.data.FormFactory;
import play.data.validation.Constraints.Required;
import play.mvc.Controller;
import play.mvc.Result;
import views.html.importcontent.importQuestion;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
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
  public static final String RUSSIA_SPIRE_CODE = "CTRY220";
  public static final String IRAN_SPIRE_CODE = "CTRY1952";
  public static final String MYANMAR_SPIRE_CODE = "CTRY1989";

  // 'Military' spire codes: Russia, Iran and Myanmar
  public static final List<String> MILITARY_COUNTRY_SPIRE_CODES
      = new ArrayList<>(Arrays.asList(RUSSIA_SPIRE_CODE, IRAN_SPIRE_CODE, MYANMAR_SPIRE_CODE));

  @Inject
  public ImportController(JourneyManager journeyManager, FormFactory formFactory, ImportJourneyDao importJourneyDao) {
    this.journeyManager = journeyManager;
    this.formFactory = formFactory;
    this.importJourneyDao = importJourneyDao;
    this.stageDataMap = ImportUtils.getStageData();
  }

  public Result renderForm(String stageKey) {
    return ok(importQuestion.render(formFactory.form(), stageDataMap.get(journeyManager.getCurrentInternalStageName())));
  }

  public CompletionStage<Result> handleSubmit() {
    String stageKey = journeyManager.getCurrentInternalStageName();
    Logger.info("stageKey: " + stageKey);
    ImportStageData importStageData = stageDataMap.get(stageKey);

    Form<ImportStageForm> form = formFactory.form(ImportStageForm.class).bindFromRequest();
    if (form.hasErrors()) {
      return completedFuture(ok(importQuestion.render(form, importStageData)));
    }

    String option = form.get().selectedOption;
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

      // Custom transitions for ImportQuestion.MILITARY
      if (stageKey.equals(ImportQuestion.MILITARY.key())) {
        if (option.equals(ImportMilitaryYesNo.YES.name())) {
          if (country.equals(RUSSIA_SPIRE_CODE)) {
            return journeyManager.performTransition(ImportEvents.IMPORT_MILITARY_YES_NO_SELECTED, ImportMilitaryYesNo.YES_RUSSIA);
          } else if (country.equals(IRAN_SPIRE_CODE)) {
            return journeyManager.performTransition(ImportEvents.IMPORT_MILITARY_YES_NO_SELECTED, ImportMilitaryYesNo.YES_IRAN);
          } else if (country.equals(MYANMAR_SPIRE_CODE)) {
            return journeyManager.performTransition(ImportEvents.IMPORT_MILITARY_YES_NO_SELECTED, ImportMilitaryYesNo.YES_MYANMAR);
          }
        }
      }

      return importStageData.completeTransition(journeyManager, option);
    } else {
      throw new FormStateException("Unknown selected option: " + option);
    }
  }

  /**
   * ImportStageForm
   */
  public static class ImportStageForm {
    @Required(message = "Please select one option")
    public String selectedOption;
  }
}

