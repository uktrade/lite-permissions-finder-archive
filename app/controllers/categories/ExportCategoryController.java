package controllers.categories;

import com.google.inject.Inject;
import components.persistence.PermissionsFinderDao;
import controllers.GoodsTypeController;
import model.ExportCategory;
import play.data.Form;
import play.data.FormFactory;
import play.mvc.Controller;
import play.mvc.Result;
import views.html.categories.selectExportCategories;

import java.util.Optional;

public class ExportCategoryController extends Controller {

  private final FormFactory formFactory;
  private final PermissionsFinderDao dao;
  private final GoodsTypeController goodsTypeController;
  private final ArtsCulturalController artsCulturalController;
  private final DualUseController dualUseController;
  private final FinancialTechnicalAssistanceController financialTechnicalAssistanceController;
  private final TortureRestraintController tortureRestraintController;
  private final RadioactiveController radioactiveController;

  @Inject
  public ExportCategoryController(FormFactory formFactory,
                                  PermissionsFinderDao dao,
                                  GoodsTypeController goodsTypeController,
                                  ArtsCulturalController artsCulturalController,
                                  DualUseController dualUseController,
                                  FinancialTechnicalAssistanceController financialTechnicalAssistanceController,
                                  TortureRestraintController tortureRestraintController,
                                  RadioactiveController radioactiveController) {
    this.formFactory = formFactory;
    this.dao = dao;
    this.goodsTypeController = goodsTypeController;
    this.artsCulturalController = artsCulturalController;
    this.dualUseController = dualUseController;
    this.financialTechnicalAssistanceController = financialTechnicalAssistanceController;
    this.tortureRestraintController = tortureRestraintController;
    this.radioactiveController = radioactiveController;
  }

  public Result renderForm() {
    return ok(selectExportCategories.render(formFactory.form()));
  }

  public Result handleSubmit() {
    Form<ExportCategoryForm> form = formFactory.form(ExportCategoryForm.class).bindFromRequest();

    Optional<ExportCategory> exportCategoryOptional = ExportCategory.getMatched(form.get().category);

    if (exportCategoryOptional.isPresent()) {
      dao.saveExportCategory(exportCategoryOptional.get());
      switch (exportCategoryOptional.get()) {
        case MILITARY:
        return goodsTypeController.renderForm();
        case DUAL_USE:
          return goodsTypeController.renderForm();
        case TORTURE_RESTRAINT:
          return tortureRestraintController.renderForm();
        case RADIOACTIVE:
          return radioactiveController.renderForm();
        case CHEMICALS_COSMETICS:
          // TODO CHEMICALS_COSMETICS
          return ok("CHEMICALS_COSMETICS");
        case ARTS_CULTURAL:
          return artsCulturalController.renderForm();
        case PLANTS_ANIMALS:
          // TODO PLANTS_ANIMALS
          return ok("PLANTS_ANIMALS");
        case FOOD:
          // TODO FOOD
          return ok("FOOD");
        case MEDICINES_DRUGS:
          // TODO MEDICINES_DRUGS
          return ok("MEDICINES_DRUGS");
        case TECHNICAL_ASSISTANCE:
          return financialTechnicalAssistanceController.renderForm();
        case FINANCIAL_ASSISTANCE:
          return financialTechnicalAssistanceController.renderForm();
        case NONE:
          return dualUseController.renderForm();
      }
    } // "Dual-use items" button link
    else if ("true".equals(form.get().couldBeDualUse)) {
      dao.saveExportCategory(ExportCategory.DUAL_USE);
      return dualUseController.renderForm();
    }

    return badRequest("Unknown export category: \"" + form.get().category + "\"");
  }

  public static class ExportCategoryForm {

    public String category;

    public String couldBeDualUse;

  }
}
