package controllers.categories;

import static java.util.concurrent.CompletableFuture.completedFuture;

import com.google.inject.Inject;
import components.common.journey.JourneyManager;
import components.persistence.PermissionsFinderDao;
import journey.Events;
import play.data.Form;
import play.data.FormFactory;
import play.data.validation.Constraints.Required;
import play.mvc.Controller;
import play.mvc.Result;
import utils.common.SelectOption;
import views.html.categories.artsCultural;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.CompletionStage;

public class ArtsCulturalController extends Controller {

  public static final List<SelectOption> AGE_OPTIONS = Arrays.asList(
      new SelectOption("LT50", "Less than 50 years old"),
      new SelectOption("GT50LT100", "Between 50 and 100 years old"),
      new SelectOption("GT100", "More than 100 years old")
  );

  private final JourneyManager jm;
  private final FormFactory formFactory;
  private final PermissionsFinderDao dao;

  @Inject
  public ArtsCulturalController(JourneyManager jm, FormFactory formFactory, PermissionsFinderDao dao) {
    this.jm = jm;
    this.formFactory = formFactory;
    this.dao = dao;
  }

  public Result renderForm() {
    Optional<ArtsCulturalForm> templateFormOptional = dao.getArtsCulturalForm();
    ArtsCulturalForm templateForm = templateFormOptional.isPresent() ? templateFormOptional.get() : new ArtsCulturalForm();
    return ok(artsCultural.render(formFactory.form(ArtsCulturalForm.class).fill(templateForm)));
  }

  public CompletionStage<Result> handleSubmit() {

    Form<ArtsCulturalForm> form = formFactory.form(ArtsCulturalForm.class).bindFromRequest();

    if (form.hasErrors()) {
      return completedFuture(ok(artsCultural.render(form)));
    }

    dao.saveArtsCulturalForm(form.get());

    if (form.get().firearm && !"GT100".equals(form.get().itemAge)) {
      return jm.performTransition(Events.GOOD_CONTROLLED);
    }
    else {
      return jm.performTransition(Events.GOOD_NOT_CONTROLLED);
    }
  }

  public static class ArtsCulturalForm {

    @Required(message = "Please select the items age")
    public String itemAge;

    @Required(message = "Please specify if the item is a firearm")
    public Boolean firearm;
  }

}
