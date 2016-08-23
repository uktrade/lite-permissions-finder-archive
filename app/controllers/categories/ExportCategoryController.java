package controllers.categories;

import com.google.inject.Inject;
import components.persistence.PermissionsFinderDao;
import controllers.GoodsTypeController;
import controllers.StaticContentController;
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
  private final StaticContentController staticContentController;
  private final GoodsTypeController goodsTypeController;
  private final ArtsCulturalController artsCulturalController;
  private final DualUseController dualUseController;
  private final FinancialTechnicalAssistanceController financialTechnicalAssistanceController;
  private final TortureRestraintController tortureRestraintController;
  private final RadioactiveController radioactiveController;
  private final ChemicalsCosmeticsController chemicalsCosmeticsController;
  private final PlantsAnimalsController plantsAnimalsController;
  private final MedicinesDrugsController medicinesDrugsController;

  @Inject
  public ExportCategoryController(FormFactory formFactory,
                                  PermissionsFinderDao dao,
                                  StaticContentController staticContentController,
                                  GoodsTypeController goodsTypeController,
                                  ArtsCulturalController artsCulturalController,
                                  DualUseController dualUseController,
                                  FinancialTechnicalAssistanceController financialTechnicalAssistanceController,
                                  TortureRestraintController tortureRestraintController,
                                  RadioactiveController radioactiveController,
                                  ChemicalsCosmeticsController chemicalsCosmeticsController,
                                  PlantsAnimalsController plantsAnimalsController,
                                  MedicinesDrugsController medicinesDrugsController) {
    this.formFactory = formFactory;
    this.dao = dao;
    this.staticContentController = staticContentController;
    this.goodsTypeController = goodsTypeController;
    this.artsCulturalController = artsCulturalController;
    this.dualUseController = dualUseController;
    this.financialTechnicalAssistanceController = financialTechnicalAssistanceController;
    this.tortureRestraintController = tortureRestraintController;
    this.radioactiveController = radioactiveController;
    this.chemicalsCosmeticsController = chemicalsCosmeticsController;
    this.plantsAnimalsController = plantsAnimalsController;
    this.medicinesDrugsController = medicinesDrugsController;
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
          return chemicalsCosmeticsController.renderForm();
        case ARTS_CULTURAL:
          return artsCulturalController.renderForm();
        case PLANTS_ANIMALS:
          return plantsAnimalsController.renderForm();
        case FOOD:
          return staticContentController.renderStaticHtml(StaticContentController.StaticHtml.CATEGORY_FOOD);
        case MEDICINES_DRUGS:
          return medicinesDrugsController.renderForm();
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
