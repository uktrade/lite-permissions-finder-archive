package controllers.categories;

import static play.mvc.Results.ok;

import com.google.inject.Inject;
import components.common.journey.JourneyManager;
import components.persistence.PermissionsFinderDao;
import exceptions.FormStateException;
import journey.Events;
import models.ExportCategory;
import models.GoodsType;
import play.data.Form;
import play.data.FormFactory;
import play.mvc.Result;
import views.html.categories.nonMilitary;

import java.util.concurrent.CompletionStage;

public class NonMilitaryController {
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
    return ok(nonMilitary.render(formFactory.form(NonMilitaryForm.class)));
  }

  public CompletionStage<Result> handleSubmit() {
    Form<NonMilitaryForm> form = formFactory.form(NonMilitaryForm.class).bindFromRequest();
    if ("true".equals(form.get().goToSearch)) {
      permissionsFinderDao.saveExportCategory(ExportCategory.NON_MILITARY);
      permissionsFinderDao.saveGoodsType(GoodsType.PHYSICAL);
      return journeyManager.performTransition(Events.SEARCH_PHYSICAL_GOODS);
    }
    throw new FormStateException("Unknown value of goToSearch: \"" + form.get().goToSearch + "\"");
  }

  public static class NonMilitaryForm {

    public String goToSearch;

  }
}
