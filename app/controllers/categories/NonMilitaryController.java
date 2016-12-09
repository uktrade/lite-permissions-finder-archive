package controllers.categories;

import static java.util.concurrent.CompletableFuture.completedFuture;
import static play.mvc.Results.ok;

import com.google.inject.Inject;
import components.common.journey.JourneyManager;
import exceptions.FormStateException;
import journey.Events;
import models.ExportYesNo;
import play.data.Form;
import play.data.FormFactory;
import play.data.validation.Constraints.Required;
import play.mvc.Result;
import utils.common.SelectOption;
import views.html.categories.nonMilitary;

import java.util.List;
import java.util.Optional;
import java.util.concurrent.CompletionStage;

public class NonMilitaryController {

  public static final List<SelectOption> YES_NO_OPTIONS = ExportYesNo.getSelectOptions();

  private final JourneyManager journeyManager;
  private final FormFactory formFactory;

  public static final String TAKE_YOURSELF_KEY = "categoryNonMilitaryTakeYourself";
  public static final String TAKE_YOURSELF_QUESTION = "Will you be taking the firearms or ammunition out of the UK yourself?";
  public static final String PERSONAL_EFFECTS_KEY = "categoryNonMilitaryPersonalEffects";
  public static final String PERSONAL_EFFECTS_QUESTION = "Will the firearms or ammunition be sent out of the UK as part of your personal effects?";

  @Inject
  public NonMilitaryController(JourneyManager journeyManager, FormFactory formFactory) {
    this.journeyManager = journeyManager;
    this.formFactory = formFactory;
  }

  public Result renderForm() {
    NonMilitaryForm templateForm = new NonMilitaryForm();
    return ok(nonMilitary.render(formFactory.form(NonMilitaryForm.class).fill(templateForm), getQuestion(journeyManager.getCurrentInternalStageName())));
  }

  public CompletionStage<Result> handleSubmit() {
    Form<NonMilitaryForm> form = formFactory.form(NonMilitaryForm.class).bindFromRequest();
    if (form.hasErrors()) {
      return completedFuture(ok(nonMilitary.render(form, getQuestion(journeyManager.getCurrentInternalStageName()))));
    }

    Optional<ExportYesNo> optYesNo = ExportYesNo.getMatched(form.get().selectedOption);
    if (optYesNo.isPresent()) {
      return journeyManager.performTransition(Events.NON_MILITARY_FIREARMS_OPTION_SELECTED, optYesNo.get());
    }
    throw new FormStateException("Unknown selectedOption");
  }

  private String getQuestion(String stageName) {
    String question = "";
    if (stageName.equals(TAKE_YOURSELF_KEY)) {
      question = TAKE_YOURSELF_QUESTION;
    } else if (stageName.equals(PERSONAL_EFFECTS_KEY)) {
      question = PERSONAL_EFFECTS_QUESTION;
    }
    return question;
  }

  public static class NonMilitaryForm {

    @Required(message = "Please select one option")
    public String selectedOption;

  }
}
