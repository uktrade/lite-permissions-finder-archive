package controllers.categories;

import static java.util.concurrent.CompletableFuture.completedFuture;

import com.google.inject.Inject;
import components.common.journey.JourneyManager;
import components.persistence.PermissionsFinderDao;
import exceptions.FormStateException;
import journey.Events;
import models.ArtsCulturalGoodsType;
import models.GoodsType;
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

  private final JourneyManager journeyManager;
  private final FormFactory formFactory;
  private final PermissionsFinderDao permissionsFinderDao;

  @Inject
  public ArtsCulturalController(JourneyManager journeyManager, FormFactory formFactory,
                                PermissionsFinderDao permissionsFinderDao) {
    this.journeyManager = journeyManager;
    this.formFactory = formFactory;
    this.permissionsFinderDao = permissionsFinderDao;
  }

  public Result renderForm() {
    Optional<ArtsCulturalForm> templateFormOptional = permissionsFinderDao.getArtsCulturalForm();
    ArtsCulturalForm templateForm = templateFormOptional.isPresent() ? templateFormOptional.get() : new ArtsCulturalForm();
    return ok(artsCultural.render(formFactory.form(ArtsCulturalForm.class).fill(templateForm)));
  }

  public CompletionStage<Result> handleSubmit() {

    Form<ArtsCulturalForm> form = formFactory.form(ArtsCulturalForm.class).bindFromRequest();

    if (form.hasErrors()) {
      return completedFuture(ok(artsCultural.render(form)));
    }

    permissionsFinderDao.saveArtsCulturalForm(form.get());
    boolean firearm = form.get().firearm;
    String itemAge = form.get().itemAge;

    if (firearm && ("LT50".equals(itemAge) || "GT50LT100".equals(itemAge))) {
      permissionsFinderDao.saveGoodsType(GoodsType.PHYSICAL);
      return journeyManager.performTransition(Events.ARTS_CULTURAL_CATEGORY_SELECTED, ArtsCulturalGoodsType.CONTROLLED);
    }
    else if ((!firearm && ("GT50LT100".equals(itemAge) || "GT100".equals(itemAge))) || (firearm && "GT100".equals(itemAge))) {
      return journeyManager.performTransition(Events.ARTS_CULTURAL_CATEGORY_SELECTED, ArtsCulturalGoodsType.HISTORIC);
    }
    else if (!firearm && "LT50".equals(itemAge)) {
      return journeyManager.performTransition(Events.ARTS_CULTURAL_CATEGORY_SELECTED, ArtsCulturalGoodsType.NON_HISTORIC);
    }
    else {
      throw new FormStateException("Unhandled tuple of itemAge: \"" + itemAge + "\" and firearm: \"" + Boolean.toString(firearm) + "\"");
    }
  }

  public static class ArtsCulturalForm {

    @Required(message = "Please select the items age")
    public String itemAge;

    @Required(message = "Please specify if the item is a firearm")
    public Boolean firearm;
  }

}
