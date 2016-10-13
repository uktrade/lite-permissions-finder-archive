package controllers.categories;

import static play.mvc.Results.ok;

import com.google.inject.Inject;
import components.common.journey.JourneyManager;
import components.persistence.PermissionsFinderDao;
import exceptions.FormStateException;
import journey.Events;
import models.GoodsType;
import play.data.Form;
import play.data.FormFactory;
import play.mvc.Result;
import views.html.categories.chemicalsCosmetics;

import java.util.concurrent.CompletionStage;

public class ChemicalsCosmeticsController {

  private final JourneyManager journeyManager;
  private final FormFactory formFactory;
  private final PermissionsFinderDao permissionsFinderDao;

  @Inject
  public ChemicalsCosmeticsController(JourneyManager journeyManager, FormFactory formFactory, PermissionsFinderDao permissionsFinderDao) {
    this.journeyManager = journeyManager;
    this.formFactory = formFactory;
    this.permissionsFinderDao = permissionsFinderDao;
  }

  public Result renderForm() {
    return ok(chemicalsCosmetics.render(formFactory.form(ChemicalsCosmeticsForm.class)));
  }

  public CompletionStage<Result> handleSubmit() {
    Form<ChemicalsCosmeticsForm> form = formFactory.form(ChemicalsCosmeticsForm.class).bindFromRequest();
    if ("true".equals(form.get().goToSearch)) {
      permissionsFinderDao.saveGoodsType(GoodsType.PHYSICAL);
      return journeyManager.performTransition(Events.SEARCH_PHYSICAL_GOODS);
    }
    throw new FormStateException("Unknown value of goToSearch: \"" + form.get().goToSearch + "\"");
  }

  public static class ChemicalsCosmeticsForm {

    public String goToSearch;

  }
}
