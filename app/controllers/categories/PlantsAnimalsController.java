package controllers.categories;

import static java.util.concurrent.CompletableFuture.completedFuture;
import static play.mvc.Results.badRequest;
import static play.mvc.Results.ok;

import com.google.inject.Inject;
import components.common.journey.JourneyManager;
import components.persistence.PermissionsFinderDao;
import journey.Events;
import model.LifeType;
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

  private final JourneyManager jm;
  private final FormFactory formFactory;
  private final PermissionsFinderDao dao;

  public static final List<SelectOption> LIFE_TYPE_OPTIONS = Arrays.asList(
      new SelectOption(LifeType.ENDANGERED.value(), "Endangered animal"),
      new SelectOption(LifeType.NON_ENDANGERED.value(), "Non-endangered animal"),
      new SelectOption(LifeType.PLANT.value(), "Plant")
  );

  @Inject
  public PlantsAnimalsController(JourneyManager jm, FormFactory formFactory, PermissionsFinderDao dao) {
    this.jm = jm;
    this.formFactory = formFactory;
    this.dao = dao;
  }

  public Result renderForm() {
    PlantsAnimalsForm templateForm = new PlantsAnimalsForm();
    Optional<LifeType> lifeTypeOptional = dao.getPlantsAnimalsLifeType();
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
      dao.savePlantsAnimalsLifeType(lifeTypeOptional.get());
      return jm.performTransition(Events.LIFE_TYPE_SELECTED, lifeTypeOptional.get());
    }
    return completedFuture(badRequest("Unknown value for lifeType: \"" + form.get().lifeType + "\""));
  }

  public static class PlantsAnimalsForm {

    @Required(message = "You must answer this question")
    public String lifeType;

  }
}
