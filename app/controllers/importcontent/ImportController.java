package controllers.importcontent;

import static java.util.concurrent.CompletableFuture.completedFuture;

import com.google.inject.Inject;
import components.common.journey.JourneyManager;
import exceptions.FormStateException;
import models.importcontent.ImportStageData;
import play.data.Form;
import play.data.FormFactory;
import play.data.validation.Constraints.Required;
import play.mvc.Controller;
import play.mvc.Result;
import utils.ImportUtils;
import views.html.importcontent.importQuestion;

import java.util.Map;
import java.util.concurrent.CompletionStage;

public class ImportController extends Controller {

  private final JourneyManager journeyManager;
  private final FormFactory formFactory;
  private Map<String, ImportStageData> stageDataMap;

  @Inject
  public ImportController(JourneyManager journeyManager, FormFactory formFactory) {
    this.journeyManager = journeyManager;
    this.formFactory = formFactory;
    this.stageDataMap = ImportUtils.getStageData();
  }

  public Result renderForm() {
    String stageKey = journeyManager.getCurrentInternalStageName();
    return ok(importQuestion.render(formFactory.form(), stageDataMap.get(stageKey)));
  }

  public CompletionStage<Result> handleSubmit() {
    String stageKey = journeyManager.getCurrentInternalStageName();
    ImportStageData importStageData = stageDataMap.get(stageKey);

    Form<ImportStageForm> form = formFactory.form(ImportStageForm.class).bindFromRequest();
    if (form.hasErrors()) {
      return completedFuture(ok(importQuestion.render(form, importStageData)));
    }

    String selectedOption = form.get().selectedOption;
    if(importStageData.isValidStageOption(selectedOption)) {
      return importStageData.completeTransition(journeyManager, selectedOption);
    } else {
      throw new FormStateException("Unknown selected option: " + selectedOption);
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

