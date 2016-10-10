package controllers.categories;

import static java.util.concurrent.CompletableFuture.completedFuture;
import static play.mvc.Results.ok;

import com.google.inject.Inject;
import components.common.journey.JourneyManager;
import components.persistence.PermissionsFinderDao;
import exceptions.FormStateException;
import journey.Events;
import models.LifeType;
import play.data.Form;
import play.data.FormFactory;
import play.data.validation.Constraints.Required;
import play.mvc.Result;
import utils.common.SelectOption;
import views.html.categories.plantsAnimals;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.CompletionStage;


public class PlantsAnimalsController {

  private final JourneyManager journeyManager;
  private final FormFactory formFactory;
  private final PermissionsFinderDao permissionsFinderDao;

  public static final List<SelectOption> LIFE_TYPE_OPTIONS = Arrays.asList(
      new SelectOption(LifeType.ENDANGERED.value(), "Endangered animal"),
      new SelectOption(LifeType.NON_ENDANGERED.value(), "Non-endangered animal"),
      new SelectOption(LifeType.PLANT.value(), "Plant")
  );

  @Inject
  public PlantsAnimalsController(JourneyManager journeyManager, FormFactory formFactory, PermissionsFinderDao permissionsFinderDao) {
    this.journeyManager = journeyManager;
    this.formFactory = formFactory;
    this.permissionsFinderDao = permissionsFinderDao;
  }

  public Result renderForm() {
    PlantsAnimalsForm templateForm = new PlantsAnimalsForm();
    Optional<LifeType> lifeTypeOptional = permissionsFinderDao.getPlantsAnimalsLifeType();
    templateForm.lifeType = lifeTypeOptional.isPresent() ? lifeTypeOptional.get().value() : "";
    return ok(plantsAnimals.render(formFactory.form(PlantsAnimalsForm.class).fill(templateForm)));
  }

  public CompletionStage<Result> handleSubmit() {
    Form<PlantsAnimalsForm> form = formFactory.form(PlantsAnimalsForm.class).bindFromRequest();
    if (form.hasErrors()) {
      return completedFuture(ok(plantsAnimals.render(form)));
    }
    Optional<LifeType> lifeTypeOptional = LifeType.getMatched(form.get().lifeType);
    if(lifeTypeOptional.isPresent()) {
      permissionsFinderDao.savePlantsAnimalsLifeType(lifeTypeOptional.get());
      return journeyManager.performTransition(Events.LIFE_TYPE_SELECTED, lifeTypeOptional.get());
    }
    throw new FormStateException("Unknown value for lifeType: \"" + form.get().lifeType + "\"");
  }

  public static class PlantsAnimalsForm {

    @Required(message = "You must answer this question")
    public String lifeType;

  }
}
