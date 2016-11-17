package controllers.categories;

import static play.mvc.Results.ok;

import com.google.inject.Inject;
import components.common.journey.JourneyManager;
import components.common.journey.StandardEvents;
import components.persistence.PermissionsFinderDao;
import exceptions.FormStateException;
import journey.helpers.ControlCodeJourneyHelper;
import models.GoodsType;
import models.controlcode.ControlCodeJourney;
import play.data.Form;
import play.data.FormFactory;
import play.mvc.Result;
import views.html.categories.radioactive;

import java.util.concurrent.CompletionStage;

public class RadioactiveController {

  private final JourneyManager journeyManager;
  private final FormFactory formFactory;
  private final PermissionsFinderDao permissionsFinderDao;
  private final ControlCodeJourneyHelper controlCodeJourneyHelper;

  public static final String CONTROLLED_RADIOACTIVE_SOURCES = "Controlled Radioactive Sources";

  @Inject
  public RadioactiveController(JourneyManager journeyManager,
                               FormFactory formFactory,
                               PermissionsFinderDao permissionsFinderDao,
                               ControlCodeJourneyHelper controlCodeJourneyHelper) {
    this.journeyManager = journeyManager;
    this.formFactory = formFactory;
    this.permissionsFinderDao = permissionsFinderDao;
    this.controlCodeJourneyHelper = controlCodeJourneyHelper;
  }

  public Result renderForm() {
    return ok(radioactive.render(formFactory.form(RadioactiveForm.class)));
  }

  public CompletionStage<Result> handleSubmit() {
    Form<RadioactiveForm> form = formFactory.form(RadioactiveForm.class).bindFromRequest();
    if ("continue".equals(form.get().action)) {
      // Setup DAO state for Destination Country journey stage.
      permissionsFinderDao.saveGoodsType(GoodsType.PHYSICAL);
      controlCodeJourneyHelper.clearControlCodeJourneyDaoFieldsIfChanged(ControlCodeJourney.PHYSICAL_GOODS_SEARCH, CONTROLLED_RADIOACTIVE_SOURCES);
      permissionsFinderDao.saveSelectedControlCode(ControlCodeJourney.PHYSICAL_GOODS_SEARCH, CONTROLLED_RADIOACTIVE_SOURCES);
      return journeyManager.performTransition(StandardEvents.NEXT);
    }
    throw new FormStateException("Unknown value of action: \"" + form.get().action + "\"");
  }

  public static class RadioactiveForm {

    public String action;

  }
}
