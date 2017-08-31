package controllers.categories;

import static java.util.concurrent.CompletableFuture.completedFuture;

import com.google.inject.Inject;
import components.common.journey.JourneyManager;
import components.persistence.PermissionsFinderDao;
import exceptions.FormStateException;
import journey.Events;
import models.ArtsCulturalGoodsAgeRange;
import models.ArtsCulturalGoodsType;
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
      new SelectOption(ArtsCulturalGoodsAgeRange.LESS_THAN_50.value(), "Less than 50 years old"),
      new SelectOption(ArtsCulturalGoodsAgeRange.BETWEEN_50_AND_100.value(), "Between 50 and 100 years old"),
      new SelectOption(ArtsCulturalGoodsAgeRange.GREATER_THAN_100.value(), "More than 100 years old")
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

    if (firearm && ArtsCulturalGoodsAgeRange.LESS_THAN_50.equals(itemAge)) {
      return journeyManager.performTransition(Events.ARTS_CULTURAL_CATEGORY_SELECTED, ArtsCulturalGoodsType.FIREARM_NON_HISTORIC);
    }
    else if (firearm && ArtsCulturalGoodsAgeRange.BETWEEN_50_AND_100.equals(itemAge)) {
      return journeyManager.performTransition(Events.ARTS_CULTURAL_CATEGORY_SELECTED, ArtsCulturalGoodsType.FIREARM_HISTORIC);
    }
    else if (!firearm && ArtsCulturalGoodsAgeRange.LESS_THAN_50.equals(itemAge)) {
      return journeyManager.performTransition(Events.ARTS_CULTURAL_CATEGORY_SELECTED, ArtsCulturalGoodsType.NON_HISTORIC);
    }
    else if ((firearm && ArtsCulturalGoodsAgeRange.GREATER_THAN_100.equals(itemAge)) ||
        (!firearm && (ArtsCulturalGoodsAgeRange.BETWEEN_50_AND_100.equals(itemAge)
            || ArtsCulturalGoodsAgeRange.GREATER_THAN_100.equals(itemAge)))) {
      return journeyManager.performTransition(Events.ARTS_CULTURAL_CATEGORY_SELECTED, ArtsCulturalGoodsType.HISTORIC);
    }
    else {
      throw new FormStateException("Unhandled tuple of itemAge: \"" + itemAge + "\" and firearm: \"" + Boolean.toString(firearm) + "\"");
    }
  }

  public static class ArtsCulturalForm {

    @Required(message = "Select the item's age")
    public String itemAge;

    @Required(message = "Select whether this item is a gun or firearm")
    public Boolean firearm;
  }

}
