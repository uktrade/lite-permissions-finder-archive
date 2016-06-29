package controllers;

import com.google.inject.Inject;
import play.data.DynamicForm;
import play.data.FormFactory;
import play.mvc.Controller;
import play.mvc.Result;
import views.html.exportCategories;

public class ExportCategoryController extends Controller {

  public enum ExportCategory {
    MILITARY,
    DUAL_USE,
    NONE;
  }

  private final FormFactory formFactory;
  private final GoodsTypeController goodsTypeController;

  @Inject
  public ExportCategoryController(FormFactory formFactory, GoodsTypeController goodsTypeController) {
    this.formFactory = formFactory;
    this.goodsTypeController = goodsTypeController;
  }

  public Result renderForm() {
    return ok(exportCategories.render());
  }

  public Result handleSubmit() {

    DynamicForm form = formFactory.form().bindFromRequest();

    ExportCategory category = ExportCategory.valueOf(form.get("category"));

    switch (category) {
      case MILITARY:
        return goodsTypeController.renderForm();
      case DUAL_USE:
        return ok("DUAL USE");
      case NONE:
        return ok("NONE");
      default:
        throw new RuntimeException("Unknown category " + category);
    }
  }
}
