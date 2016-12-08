package controllers.categories;

import static java.util.concurrent.CompletableFuture.completedFuture;
import static play.mvc.Results.ok;

import com.google.inject.Inject;
import components.common.journey.JourneyManager;
import components.persistence.PermissionsFinderDao;
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
  private final PermissionsFinderDao permissionsFinderDao;

  @Inject
  public NonMilitaryController(JourneyManager journeyManager, FormFactory formFactory, PermissionsFinderDao permissionsFinderDao) {
    this.journeyManager = journeyManager;
    this.formFactory = formFactory;
    this.permissionsFinderDao = permissionsFinderDao;
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
    if(optYesNo.isPresent()) {
      return journeyManager.performTransition(Events.NON_MILITARY_FIREARMS_ANSWER_SELECTED, optYesNo.get());
    }
    throw new FormStateException("Unknown selectedOption");

  }

  private String getQuestion(String stageKey) {
    String question = "";
    if(stageKey.equals("categoryNonMilitary1")) {
      question = "Will you be taking the firearms or ammunition out of the UK yourself?";
    } else if(stageKey.equals("categoryNonMilitary2")) {
      question = "Will the firearms or ammunition be sent out of the UK as part of your personal effects?";
    }
    return question;
  }

  public static class NonMilitaryForm {

    @Required(message = "You must answer this question")
    public String selectedOption;

  }
}
