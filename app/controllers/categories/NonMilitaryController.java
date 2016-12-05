package controllers.categories;

import static java.util.concurrent.CompletableFuture.completedFuture;
import static play.mvc.Results.ok;

import com.google.inject.Inject;
import components.common.journey.JourneyManager;
import components.persistence.PermissionsFinderDao;
import exceptions.FormStateException;
import journey.Events;
import models.NonMilitaryFirearmExportBySelfType;
import play.data.Form;
import play.data.FormFactory;
import play.data.validation.Constraints.Required;
import play.mvc.Result;
import utils.common.SelectOption;
import views.html.categories.nonMilitary;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.CompletionStage;

public class NonMilitaryController {

  public static final List<SelectOption> FIREARMS_EXPORTED_BY_SELF_OPTIONS = Arrays.asList(
      new SelectOption(NonMilitaryFirearmExportBySelfType.YES.value(), "Yes"),
      new SelectOption(NonMilitaryFirearmExportBySelfType.NO_INCLUDED_IN_PERSONAL_EFFECTS.value(),
          "No, but they will be sent as part of my personal effects"),
      new SelectOption(NonMilitaryFirearmExportBySelfType.NO_TRANSFER_TO_THIRD_PARTY.value(),
          "No, they are for sale, gift, loan or another form of transfer to someone else")
  );

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
    templateForm.nonMilitaryFirearmsExportedBySelf = permissionsFinderDao.readNonMilitaryFirearmsExportedBySelf();
    return ok(nonMilitary.render(formFactory.form(NonMilitaryForm.class).fill(templateForm)));
  }

  public CompletionStage<Result> handleSubmit() {
    Form<NonMilitaryForm> form = formFactory.form(NonMilitaryForm.class).bindFromRequest();
    if (form.hasErrors()) {
      return completedFuture(ok(nonMilitary.render(form)));
    }
    String nonMilitaryFirearmsExportedBySelf = form.get().nonMilitaryFirearmsExportedBySelf;
    Optional<NonMilitaryFirearmExportBySelfType> typeOptional = NonMilitaryFirearmExportBySelfType
        .getMatched(nonMilitaryFirearmsExportedBySelf);
    if (typeOptional.isPresent()) {
      permissionsFinderDao.saveNonMilitaryFirearmsExportedBySelf(nonMilitaryFirearmsExportedBySelf);
      return journeyManager.performTransition(Events.NON_MILITARY_FIREARMS_QUESTION_ANSWERED, typeOptional.get());
    }
    else {
      throw new FormStateException("Unknown value of nonMilitaryFirearmsExportedBySelf: \"" + nonMilitaryFirearmsExportedBySelf + "\"");
    }

  }

  public static class NonMilitaryForm {

    @Required(message = "You must answer this question")
    public String nonMilitaryFirearmsExportedBySelf;

  }
}
