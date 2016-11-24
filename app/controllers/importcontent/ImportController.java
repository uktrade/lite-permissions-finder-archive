package controllers.importcontent;

import static java.util.concurrent.CompletableFuture.completedFuture;

import com.google.inject.Inject;
import components.common.journey.JourneyManager;
import components.persistence.PermissionsFinderDao;
import exceptions.FormStateException;
import importcontent.ImportJourneyDefinitionBuilder;
import importcontent.models.ImportFoodWhat;
import importcontent.models.ImportWhat;
import importcontent.models.ImportWhere;
import importcontent.models.ImportYesNo;
import models.ImportFormData;
import play.Logger;
import play.data.Form;
import play.data.FormFactory;
import play.data.validation.Constraints;
import play.mvc.Controller;
import play.mvc.Result;
import views.html.importcontent.importQuestion;

import java.util.Arrays;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.CompletionStage;

public class ImportController extends Controller {

  private final JourneyManager journeyManager;
  private final FormFactory formFactory;
  private final PermissionsFinderDao permissionsFinderDao;

  @Inject
  public ImportController(JourneyManager journeyManager, FormFactory formFactory, PermissionsFinderDao permissionsFinderDao) {
    this.journeyManager = journeyManager;
    this.formFactory = formFactory;
    this.permissionsFinderDao = permissionsFinderDao;
  }

  public Result renderForm(String stageKey) {
    Logger.info("ImportController.renderForm: " + stageKey);
    return ok(importQuestion.render(formFactory.form(), getImportFormData(stageKey)));
  }

  public CompletionStage<Result> handleSubmit() {
    Form<ImportController.ImportQuestionForm> form = formFactory.form(ImportController.ImportQuestionForm.class).bindFromRequest();
    showFormValues();
    if (form.hasErrors()) {
      return completedFuture(ok(importQuestion.render(form, getImportFormData(form.get().stageKey))));
    }
    return completeTransition(form.get().stageKey, form.get().selectedOption);
  }

  public static class ImportQuestionForm {
    @Constraints.Required(message = "Please select one option")
    public String selectedOption;
    public String stageKey;

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

  private CompletionStage<Result> completeTransition(String stageKey, String option) {
    Logger.info("ImportController.completeTransition: " + stageKey + "/" + option);
    switch (stageKey) {
      case ImportJourneyDefinitionBuilder.KEY_WHERE: {
        Optional<ImportWhere> optional = ImportWhere.getMatched(option);
        if (optional.isPresent()) {
          return journeyManager.performTransition(ImportEvents.IMPORT_WHERE_SELECTED, optional.get());
        }
        break;
      }
      case ImportJourneyDefinitionBuilder.KEY_WHAT: {
        Optional<ImportWhat> optional = ImportWhat.getMatched(option);
        if (optional.isPresent()) {
          return journeyManager.performTransition(ImportEvents.IMPORT_WHAT_SELECTED, optional.get());
        }
        break;
      }
      case ImportJourneyDefinitionBuilder.KEY_CHARCOAL: {
        Optional<ImportYesNo> optional = ImportYesNo.getMatched(option);
        if (optional.isPresent()) {
          return journeyManager.performTransition(ImportEvents.IMPORT_CHARCOAL_SELECTED, optional.get());
        }
        break;
      }
      case ImportJourneyDefinitionBuilder.KEY_MILITARY: {
        Optional<ImportYesNo> optional = ImportYesNo.getMatched(option);
        if (optional.isPresent()) {
          return journeyManager.performTransition(ImportEvents.IMPORT_MILITARY_SELECTED, optional.get());
        }
        break;
      }
      case ImportJourneyDefinitionBuilder.KEY_SHOT: {
        Optional<ImportYesNo> optional = ImportYesNo.getMatched(option);
        if (optional.isPresent()) {
          return journeyManager.performTransition(ImportEvents.IMPORT_SHOT_SELECTED, optional.get());
        }
        break;
      }
      case ImportJourneyDefinitionBuilder.KEY_SUBSTANCES: {
        Optional<ImportYesNo> optional = ImportYesNo.getMatched(option);
        if (optional.isPresent()) {
          return journeyManager.performTransition(ImportEvents.IMPORT_SUBSTANCES_SELECTED, optional.get());
        }
        break;
      }
      case ImportJourneyDefinitionBuilder.KEY_OZONE: {
        Optional<ImportYesNo> optional = ImportYesNo.getMatched(option);
        if (optional.isPresent()) {
          return journeyManager.performTransition(ImportEvents.IMPORT_OZONE_SELECTED, optional.get());
        }
        break;
      }
      case ImportJourneyDefinitionBuilder.KEY_DRUGS: {
        Optional<ImportYesNo> optional = ImportYesNo.getMatched(option);
        if (optional.isPresent()) {
          return journeyManager.performTransition(ImportEvents.IMPORT_DRUGS_SELECTED, optional.get());
        }
        break;
      }
      case ImportJourneyDefinitionBuilder.KEY_FOOD_WHAT: {
        Optional<ImportFoodWhat> optional = ImportFoodWhat.getMatched(option);
        if (optional.isPresent()) {
          return journeyManager.performTransition(ImportEvents.IMPORT_FOOD_WHAT_SELECTED, optional.get());
        }
        break;
      }
      case ImportJourneyDefinitionBuilder.KEY_ENDANGERED: {
        Optional<ImportYesNo> optional = ImportYesNo.getMatched(option);
        if (optional.isPresent()) {
          return journeyManager.performTransition(ImportEvents.IMPORT_ENDANGERED_SELECTED, optional.get());
        }
        break;
      }
    }
    throw new FormStateException("Unknown selection option: " + option);
  }

  private ImportFormData getImportFormData(String stageKey) {
    ImportFormData data = new ImportFormData(stageKey);
    switch (stageKey) {
      case ImportJourneyDefinitionBuilder.KEY_WHERE:
        data.setQuestion(ImportJourneyDefinitionBuilder.KEY_WHERE_QUESTION);
        data.setOptions(ImportWhere.getSelectOptions());
        break;
      case ImportJourneyDefinitionBuilder.KEY_WHAT:
        data.setQuestion(ImportJourneyDefinitionBuilder.KEY_WHAT_QUESTION);
        data.setOptions(ImportWhat.getSelectOptions());
        break;
      case ImportJourneyDefinitionBuilder.KEY_CHARCOAL:
        data.setQuestion(ImportJourneyDefinitionBuilder.KEY_CHARCOAL_QUESTION);
        data.setOptions(ImportYesNo.getSelectOptions());
        break;
      case ImportJourneyDefinitionBuilder.KEY_MILITARY:
        data.setQuestion(ImportJourneyDefinitionBuilder.KEY_MILITARY_QUESTION);
        data.setOptions(ImportYesNo.getSelectOptions());
        break;
      case ImportJourneyDefinitionBuilder.KEY_SHOT:
        data.setQuestion(ImportJourneyDefinitionBuilder.KEY_SHOT_QUESTION);
        data.setOptions(ImportYesNo.getSelectOptions());
        break;
      case ImportJourneyDefinitionBuilder.KEY_SUBSTANCES:
        data.setQuestion(ImportJourneyDefinitionBuilder.KEY_SUBSTANCES_QUESTION);
        data.setOptions(ImportYesNo.getSelectOptions());
        break;
      case ImportJourneyDefinitionBuilder.KEY_OZONE:
        data.setQuestion(ImportJourneyDefinitionBuilder.KEY_OZONE_QUESTION);
        data.setOptions(ImportYesNo.getSelectOptions());
        break;
      case ImportJourneyDefinitionBuilder.KEY_DRUGS:
        data.setQuestion(ImportJourneyDefinitionBuilder.KEY_DRUGS_QUESTION);
        data.setOptions(ImportYesNo.getSelectOptions());
        break;
      case ImportJourneyDefinitionBuilder.KEY_FOOD_WHAT:
        data.setQuestion(ImportJourneyDefinitionBuilder.KEY_FOOD_WHAT_QUESTION);
        data.setOptions(ImportFoodWhat.getSelectOptions());
        break;
      case ImportJourneyDefinitionBuilder.KEY_ENDANGERED:
        data.setQuestion(ImportJourneyDefinitionBuilder.KEY_ENDANGERED_QUESTION);
        data.setOptions(ImportYesNo.getSelectOptions());
        break;
      default:
        Logger.warn("Unrecognised stageKey: " + stageKey);
        break;
    }
    return data;
  }

  private void showFormValues() {
    final Map<String, String[]> formValues = request().body().asFormUrlEncoded();
    formValues.forEach((key, value) -> {
      System.out.println("Key : " + key + " Value : " + Arrays.toString(value));
    });
  }
}

