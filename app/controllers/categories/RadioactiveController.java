package controllers.categories;

import static play.mvc.Results.ok;

import com.google.inject.Inject;
import components.common.journey.JourneyManager;
import components.persistence.PermissionsFinderDao;
import exceptions.FormStateException;
import journey.Events;
import models.GoodsType;
import models.RadioactiveStage;
import models.controlcode.ControlCodeSubJourney;
import play.data.Form;
import play.data.FormFactory;
import play.mvc.Result;
import views.html.categories.radioactive;

import java.util.concurrent.CompletionStage;

public class RadioactiveController {

  private final JourneyManager journeyManager;
  private final FormFactory formFactory;
  private final PermissionsFinderDao permissionsFinderDao;

  public static final String CONTROLLED_RADIOACTIVE_SOURCES = "Controlled Radioactive Sources";

  @Inject
  public RadioactiveController(JourneyManager journeyManager,
                               FormFactory formFactory,
                               PermissionsFinderDao permissionsFinderDao) {
    this.journeyManager = journeyManager;
    this.formFactory = formFactory;
    this.permissionsFinderDao = permissionsFinderDao;
  }

  public Result renderForm() {
    return ok(radioactive.render(formFactory.form(RadioactiveForm.class)));
  }

  public CompletionStage<Result> handleSubmit() {
    Form<RadioactiveForm> form = formFactory.form(RadioactiveForm.class).bindFromRequest();

    if (RadioactiveStage.CRS_SELECTED.name().equals(form.get().action)) {
      // Setup DAO state for Destination Country journey stage.
      permissionsFinderDao.saveGoodsType(GoodsType.PHYSICAL);
      permissionsFinderDao.clearAndUpdateControlCodeSubJourneyDaoFieldsIfChanged(
          ControlCodeSubJourney.PHYSICAL_GOODS_SEARCH, CONTROLLED_RADIOACTIVE_SOURCES);
      permissionsFinderDao.saveControlCodeForRegistration(CONTROLLED_RADIOACTIVE_SOURCES);
      return journeyManager.performTransition(Events.RADIOACTIVE_NEXT, RadioactiveStage.CRS_SELECTED);
    } else if(RadioactiveStage.CONTINUE.name().equals(form.get().action)) {
      return journeyManager.performTransition(Events.RADIOACTIVE_NEXT, RadioactiveStage.CONTINUE);
    }

    throw new FormStateException("Unknown value of action: \"" + form.get().action + "\"");
  }

  public static class RadioactiveForm {

    public String action;

  }
}
