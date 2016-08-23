package controllers.categories;

import static play.mvc.Results.badRequest;
import static play.mvc.Results.ok;

import com.google.inject.Inject;
import components.persistence.PermissionsFinderDao;
import controllers.GoodsTypeController;
import controllers.search.NoneDescribedController;
import model.ExportCategory;
import play.data.Form;
import play.data.FormFactory;
import play.data.validation.Constraints.Required;
import play.mvc.Result;
import views.html.categories.dualUse;

public class DualUseController {

  private final FormFactory formFactory;
  private final PermissionsFinderDao dao;
  private final GoodsTypeController goodsTypeController;
  private final NoneDescribedController noneDescribedController;

  @Inject
  public DualUseController(FormFactory formFactory,
                           PermissionsFinderDao dao,
                           GoodsTypeController goodsTypeController,
                           NoneDescribedController noneDescribedController) {
    this.formFactory = formFactory;
    this.dao = dao;
    this.goodsTypeController = goodsTypeController;
    this.noneDescribedController = noneDescribedController;
  }

  public Result renderForm() {
    return ok(dualUse.render(formFactory.form(DualUseForm.class)));
  }

  public Result handleSubmit() {
    Form<DualUseForm> form = formFactory.form(DualUseForm.class).bindFromRequest();
    if (form.hasErrors()) {
      return ok(dualUse.render(form));
    }
    String isDualUse = form.get().isDualUse;
    if ("true".equals(isDualUse)) {
      dao.saveExportCategory(ExportCategory.DUAL_USE);
      return goodsTypeController.renderForm();
    }
    else if ("false".equals(isDualUse)) {
      dao.saveExportCategory(ExportCategory.NONE);
      return noneDescribedController.render();
    }
    return badRequest("Unknown value for isDualUse: \"" + isDualUse + "\"");
  }

  public static class DualUseForm {

    @Required(message = "You must answer this question")
    public String isDualUse;

  }
}
