package controllers.categories;

import com.google.inject.Inject;
import components.persistence.PermissionsFinderDao;
import controllers.GoodsTypeController;
import model.ExportCategory;
import play.data.DynamicForm;
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

  @Inject
  public ExportCategoryController(FormFactory formFactory,
                                  PermissionsFinderDao dao,
                                  GoodsTypeController goodsTypeController,
                                  ArtsCulturalController artsCulturalController) {
    this.formFactory = formFactory;
    this.dao = dao;
    this.goodsTypeController = goodsTypeController;
    this.artsCulturalController = artsCulturalController;
  }

  public Result renderForm() {
    return ok(selectExportCategories.render(formFactory.form()));
  }

  public Result handleSubmit() {
    DynamicForm form = formFactory.form().bindFromRequest();

    Optional<ExportCategory> exportCategoryOptional = ExportCategory.getMatched(form.get("category"));

    if (exportCategoryOptional.isPresent()) {
      dao.saveExportCategory(exportCategoryOptional.get());
      switch (exportCategoryOptional.get()) {
        case MILITARY:
        return goodsTypeController.renderForm();
        case DUAL_USE:
          return ok("DUAL USE");
        case TORTURE_RESTRAINT:
          return ok("TORTURE_RESTRAINT");
        case RADIOACTIVE:
          return ok("RADIOACTIVE");
        case CHEMICALS_COSMETICS:
          return ok("CHEMICALS_COSMETICS");
        case ARTS_CULTURAL:
          return artsCulturalController.renderForm();
        case PLANTS_ANIMALS:
          return ok("PLANTS_ANIMALS");
        case FOOD:
          return ok("FOOD");
        case MEDICINES_DRUGS:
          return ok("MEDICINES_DRUGS");
        case TECHNICAL_ASSISTANCE:
          return ok("TECHNICAL_ASSISTANCE");
        case FINANCIAL_ASSISTANCE:
          return ok("FINANCIAL_ASSISTANCE");
        case NONE:
          return ok("NONE");
      }
    }

    return badRequest("Unknown export category: \"" + form.get("category") + "\"");
  }
}
