package controllers;

import com.google.inject.Inject;
import play.data.DynamicForm;
import play.data.FormFactory;
import play.mvc.Controller;
import play.mvc.Result;

public class ExportCategoryController extends Controller {

  public enum ExportCategory {
    MILITARY,
    DUAL_USE,
    NONE;
  }

  private final FormFactory formFactory;

  @Inject
  public ExportCategoryController(FormFactory formFactory) {
    this.formFactory = formFactory;
  }

  public Result handleSubmit() {

    DynamicForm form = formFactory.form().bindFromRequest();

    ExportCategory category = ExportCategory.valueOf(form.get("category"));

    switch (category) {
      case MILITARY:
        return ok("MIL");
      case DUAL_USE:
        return ok("DUAL USE");
      case NONE:
        return ok("NONE");
      default:
        throw new RuntimeException("Unknown category " + category);
    }

  }

}
