package controllers.categories;

import static play.mvc.Results.badRequest;
import static play.mvc.Results.ok;

import com.google.inject.Inject;
import controllers.GoodsTypeController;
import play.data.Form;
import play.data.FormFactory;
import play.mvc.Result;
import views.html.categories.financialTechnicalAssistance;

public class FinancialTechnicalAssistanceController {

  private final FormFactory formFactory;
  private final GoodsTypeController goodsTypeController;

  @Inject
  public FinancialTechnicalAssistanceController(FormFactory formFactory, GoodsTypeController goodsTypeController) {
    this.formFactory = formFactory;
    this.goodsTypeController = goodsTypeController;
  }

  public Result renderForm() {
    return ok(financialTechnicalAssistance.render(formFactory.form(FinancialTechnicalAssistanceForm.class)));
  }

  public Result handleSubmit() {
    Form<FinancialTechnicalAssistanceForm> form = formFactory.form(FinancialTechnicalAssistanceForm.class).bindFromRequest();
    if ("true".equals(form.get().goToSearch)) {
      return goodsTypeController.renderForm();
    }
    return badRequest("Unknown value of goToSearch: \"" + form.get().goToSearch + "\"");
  }

  public static class FinancialTechnicalAssistanceForm {

    public String goToSearch;

  }

}
