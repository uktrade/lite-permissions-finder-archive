package controllers.categories;

import static java.util.concurrent.CompletableFuture.completedFuture;
import static play.mvc.Results.ok;

import com.google.inject.Inject;
import components.common.journey.JourneyManager;
import exceptions.FormStateException;
import journey.Events;
import models.ExportCategory;
import play.data.Form;
import play.data.FormFactory;
import play.mvc.Result;
import views.html.categories.artsCulturalFirearmHistoric;

import java.util.concurrent.CompletionStage;

public class ArtsCulturalFirearmHistoricController {

  public static final String GO_TO_NON_MILITARY_FIREARMS = "goToNonMilitaryFirearms";

  private final JourneyManager journeyManager;
  private final FormFactory formFactory;

  @Inject
  public ArtsCulturalFirearmHistoricController(JourneyManager journeyManager, FormFactory formFactory) {
    this.journeyManager = journeyManager;
    this.formFactory = formFactory;
  }

  public Result renderForm() {
    return ok(artsCulturalFirearmHistoric.render(formFactory.form(ArtsCulturalFirearmHistoric.class)));
  }

  public CompletionStage<Result> handleSubmit() {
    Form<ArtsCulturalFirearmHistoric> form = formFactory.form(ArtsCulturalFirearmHistoric.class).bindFromRequest();
    if (form.hasErrors()) {
      return completedFuture(ok(artsCulturalFirearmHistoric.render(form)));
    }

    String action = form.get().action;
    if (GO_TO_NON_MILITARY_FIREARMS.equals(action)) {
      return journeyManager.performTransition(Events.EXPORT_CATEGORY_SELECTED, ExportCategory.NON_MILITARY);
    }
    else {
      throw new FormStateException(String.format("Unknown value for action %s", action));
    }
  }

  public static class ArtsCulturalFirearmHistoric {

    public String action;

  }

}
