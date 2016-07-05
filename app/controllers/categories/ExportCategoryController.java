package controllers.categories;

import com.google.inject.Inject;
import controllers.GoodsTypeController;
import play.data.DynamicForm;
import play.data.FormFactory;
import play.mvc.Controller;
import play.mvc.Result;
import views.html.categories.selectExportCategories;

public class ExportCategoryController extends Controller {

  public enum ExportCategory {
    MILITARY,
    DUAL_USE,
    TORTURE_RESTRAINT,
    RADIOACTIVE,
    CHEMICALS_COSMETICS,
    ARTS_CULTURAL,
    PLANTS_ANIMALS,
    FOOD,
    MEDICINES_DRUGS,
    TECHNICAL_ASSISTANCE,
    FINANCIAL_ASSISTANCE,
    NONE
  }

  private final FormFactory formFactory;
  private final GoodsTypeController goodsTypeController;
  private final ArtsCulturalController artsCulturalController;

  @Inject
  public ExportCategoryController(FormFactory formFactory, GoodsTypeController goodsTypeController,
                                  ArtsCulturalController artsCulturalController) {
    this.formFactory = formFactory;
    this.goodsTypeController = goodsTypeController;
    this.artsCulturalController = artsCulturalController;
  }

  public Result renderForm() {
    return ok(selectExportCategories.render());
  }

  public Result handleSubmit() {

    DynamicForm form = formFactory.form().bindFromRequest();

    ExportCategory category = ExportCategory.valueOf(form.get("category"));

    switch (category) {
      case MILITARY:
        return goodsTypeController.renderForm();
      case ARTS_CULTURAL:
        return artsCulturalController.renderForm();
      case DUAL_USE:
        return ok("DUAL USE");
      case NONE:
        return ok("NONE");
      default:
        throw new RuntimeException("Unknown category " + category);
    }
  }
}
