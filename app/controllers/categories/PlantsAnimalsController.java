package controllers.categories;

import static play.mvc.Results.badRequest;
import static play.mvc.Results.ok;

import com.google.inject.Inject;
import controllers.StaticContentController;
import play.data.Form;
import play.data.FormFactory;
import play.data.validation.Constraints.Required;
import play.mvc.Result;
import utils.common.SelectOption;
import views.html.categories.plantsAnimals;

import java.util.Arrays;
import java.util.EnumSet;
import java.util.List;
import java.util.Optional;


public class PlantsAnimalsController {

  private final FormFactory formFactory;
  private final StaticContentController staticContentController;

  public enum LifeType {
    ENDANGERED("ENDANGERED"),
    NON_ENDANGERED("NON_ENDANGERED"),
    PLANT("PLANT");

    private String value;

    LifeType(String value) {
      this.value = value;
    }

    public String value() {
      return this.value;
    }

    public static Optional<LifeType> getMatched(String lifeType) {
      return EnumSet.allOf(LifeType.class).stream().filter(e -> e.value().equals(lifeType)).findFirst();
    }
  }

  public static final List<SelectOption> LIFE_TYPE_OPTIONS = Arrays.asList(
      new SelectOption(LifeType.ENDANGERED.value(), "Endangered animal"),
      new SelectOption(LifeType.NON_ENDANGERED.value(), "Non-endangered animal"),
      new SelectOption(LifeType.PLANT.value(), "Plant")
  );

  @Inject
  public PlantsAnimalsController(FormFactory formFactory, StaticContentController staticContentController) {
    this.formFactory = formFactory;
    this.staticContentController = staticContentController;
  }

  public Result renderForm() {
    return ok(plantsAnimals.render(formFactory.form(PlantsAnimalsForm.class)));
  }

  public Result handleSubmit() {
    Form<PlantsAnimalsForm> form = formFactory.form(PlantsAnimalsForm.class).bindFromRequest();
    if (form.hasErrors()) {
      return ok(plantsAnimals.render(form));
    }
    Optional<LifeType> lifeTypeOptional = LifeType.getMatched(form.get().lifeType);
    if(lifeTypeOptional.isPresent()) {
      switch (lifeTypeOptional.get()) {
        case ENDANGERED:
          return staticContentController.renderStaticHtml(StaticContentController.StaticHtml.CATEGORY_ENDANGERED_ANIMAL);
        case NON_ENDANGERED:
          return staticContentController.renderStaticHtml(StaticContentController.StaticHtml.CATEGORY_NON_ENDANGERED_ANIMAL);
        case PLANT:
          return staticContentController.renderStaticHtml(StaticContentController.StaticHtml.CATEGORY_PLANT);
      }
    }
    return badRequest("Unknown value for lifeType: \"" + form.get().lifeType + "\"");
  }

  public static class PlantsAnimalsForm {

    @Required(message = "You must answer this question")
    public String lifeType;

  }
}
