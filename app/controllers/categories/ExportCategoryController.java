package controllers.categories;

import com.google.inject.Inject;
import controllers.ErrorController;
import controllers.GoodsTypeController;
import play.data.DynamicForm;
import play.data.FormFactory;
import play.mvc.Controller;
import play.mvc.Result;
import views.html.categories.selectExportCategories;

import java.util.EnumSet;
import java.util.Optional;

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
  public ExportCategoryController(FormFactory formFactory,
                                  ErrorController errorController,
                                  GoodsTypeController goodsTypeController,
                                  ArtsCulturalController artsCulturalController) {
    this.formFactory = formFactory;
    this.goodsTypeController = goodsTypeController;
    this.artsCulturalController = artsCulturalController;
  }

  public Result renderForm() {
    return ok(selectExportCategories.render(formFactory.form()));
  }

  public Result handleSubmit() {
    DynamicForm form = formFactory.form().bindFromRequest();

    Optional<ExportCategory> exportCategoryOptional = EnumSet.allOf(ExportCategory.class).stream()
        .filter(e -> e.name().equals(form.get("category"))).findFirst();

    if (exportCategoryOptional.isPresent()) {
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
        default:
          form.reject("Please select a category");
          return ok(selectExportCategories.render(form));
      }
    }
    else {
      form.reject("Please select a category");
      return ok(selectExportCategories.render(form));
    }
  }
}
