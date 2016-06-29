package controllers;

import com.google.inject.Inject;
import play.data.Form;
import play.data.FormFactory;
import play.data.validation.Constraints.Required;
import play.mvc.Controller;
import play.mvc.Result;
import views.html.goodsType;

public class GoodsTypeController extends Controller {

  public enum GoodsTypeOption {
    PHYSICAL,
    SOFTWARE,
    TECHNOLOGY
  }

  private final FormFactory formFactory;

  @Inject
  public GoodsTypeController(FormFactory formFactory) {
    this.formFactory = formFactory;
  }

  public Result renderForm() {
    return ok(goodsType.render(formFactory.form(GoodsTypeForm.class)));
  }

  public Result handleSubmit() {

    Form<GoodsTypeForm> form = formFactory.form(GoodsTypeForm.class).bindFromRequest();

    if (form.hasErrors()) {
      return ok(goodsType.render(form));
    }

    GoodsTypeOption goodsTypeOption = GoodsTypeOption.valueOf(form.get().goodsType);
    switch (goodsTypeOption) {
      case TECHNOLOGY:
      case SOFTWARE:
        return ok("Not implemented");
      case PHYSICAL:
        return ok("physical");
      default:
        return badRequest("Unknown goods type " + goodsTypeOption);
    }
  }

  public static class GoodsTypeForm {

    public GoodsTypeForm() {
    }

    @Required(message = "You must select what you are exporting")
    public String goodsType;
  }

}
