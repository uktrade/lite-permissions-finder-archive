package controllers.categories;

import static play.mvc.Results.badRequest;
import static play.mvc.Results.ok;

import com.google.inject.Inject;
import controllers.GoodsTypeController;
import play.data.Form;
import play.data.FormFactory;
import play.mvc.Result;
import views.html.categories.radioactive;

public class RadioactiveController {
  private final FormFactory formFactory;
  private final GoodsTypeController goodsTypeController;

  @Inject
  public RadioactiveController(FormFactory formFactory, GoodsTypeController goodsTypeController) {
    this.formFactory = formFactory;
    this.goodsTypeController = goodsTypeController;
  }

  public Result renderForm() {
    return ok(radioactive.render(formFactory.form(RadioactiveForm.class)));
  }

  public Result handleSubmit() {
    Form<RadioactiveForm> form = formFactory.form(RadioactiveForm.class).bindFromRequest();
    if ("true".equals(form.get().goToSearch)) {
      return goodsTypeController.renderForm();
    }
    return badRequest("Unknown value of goToSearch: \"" + form.get().goToSearch + "\"");
  }

  public static class RadioactiveForm {

    public String goToSearch;

  }
}
