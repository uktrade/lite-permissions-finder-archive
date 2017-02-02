package controllers.categories;

import static play.mvc.Results.ok;

import com.google.inject.Inject;
import components.common.journey.JourneyManager;
import components.common.journey.StandardEvents;
import components.persistence.PermissionsFinderDao;
import exceptions.FormStateException;
import models.ExportCategory;
import models.GoodsType;
import models.softtech.SoftTechCategory;
import play.data.Form;
import play.data.FormFactory;
import play.mvc.Result;
import views.html.categories.financialTechnicalAssistance;

import java.util.concurrent.CompletionStage;

public class FinancialTechnicalAssistanceController {

  private final JourneyManager journeyManager;
  private final FormFactory formFactory;
  private final PermissionsFinderDao permissionsFinderDao;

  @Inject
  public FinancialTechnicalAssistanceController(FormFactory formFactory, JourneyManager journeyManager, PermissionsFinderDao permissionsFinderDao) {
    this.formFactory = formFactory;
    this.journeyManager = journeyManager;
    this.permissionsFinderDao = permissionsFinderDao;
  }

  public Result renderForm() {
    return ok(financialTechnicalAssistance.render(formFactory.form(FinancialTechnicalAssistanceForm.class)));
  }

  public CompletionStage<Result> handleSubmit() {
    Form<FinancialTechnicalAssistanceForm> form = formFactory.form(FinancialTechnicalAssistanceForm.class).bindFromRequest();
    String action = form.get().action;
    if ("goToMilitary".equals(action)) {
      permissionsFinderDao.saveExportCategory(ExportCategory.MILITARY);
      permissionsFinderDao.saveGoodsType(GoodsType.TECHNOLOGY);
      permissionsFinderDao.saveSoftTechCategory(GoodsType.TECHNOLOGY, SoftTechCategory.MILITARY);
      return journeyManager.performTransition(StandardEvents.NEXT);
    }
    else if ("goToDualUse".equals(action)) {
      permissionsFinderDao.saveExportCategory(ExportCategory.DUAL_USE);
      permissionsFinderDao.saveGoodsType(GoodsType.TECHNOLOGY);
      return journeyManager.performTransition(StandardEvents.NEXT);
    }
    else {
      throw new FormStateException("Unknown value of action: \"" + form.get().action + "\"");
    }
  }

  public static class FinancialTechnicalAssistanceForm {

    public String action;

  }

}
