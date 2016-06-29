package controllers.categories;

import com.google.inject.Inject;
import controllers.GoodsTypeController;
import play.data.Form;
import play.data.FormFactory;
import play.data.validation.Constraints.Required;
import play.mvc.Controller;
import play.mvc.Result;
import utils.common.SelectOption;
import views.html.categories.artsCultural;

import java.util.Arrays;
import java.util.List;

public class ArtsCulturalController extends Controller {

  public static final List<SelectOption> AGE_OPTIONS = Arrays.asList(
      new SelectOption("LT50", "Less than 50 years old"),
      new SelectOption("GT50LT100", "Between 50 and 100 years old"),
      new SelectOption("GT100", "More than 100 years old")
  );

  private final FormFactory formFactory;
  private final GoodsTypeController goodsTypeController;

  @Inject
  public ArtsCulturalController(FormFactory formFactory, GoodsTypeController goodsTypeController) {
    this.formFactory = formFactory;
    this.goodsTypeController = goodsTypeController;
  }

  public Result renderForm() {
    return ok(artsCultural.render(formFactory.form()));
  }

  public Result handleSubmit() {

    Form<ArtsForm> form = formFactory.form(ArtsForm.class).bindFromRequest();

    if (form.hasErrors()) {
      return ok(artsCultural.render(form));
    }
    else {
      if (form.get().firearm && !"GT100".equals(form.get().itemAge)) {
        return goodsTypeController.renderForm();
      }
    }

    return ok("TODO static response NLR");
  }

  public static class ArtsForm {
    @Required
    public String itemAge;

    @Required
    public Boolean firearm;
  }

}
