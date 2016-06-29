package controllers;

import com.google.inject.Inject;
import play.data.Form;
import play.data.FormFactory;
import play.data.validation.Constraints.Required;
import play.mvc.Controller;
import play.mvc.Result;
import utils.common.SelectOption;
import views.html.tradeType;

import java.util.Arrays;
import java.util.List;

public class TradeTypeController extends Controller {

  public static final List<SelectOption> TRADE_TYPE_OPTIONS = Arrays.asList(
    new SelectOption("IMPORT", "I am importing"),
    new SelectOption("EXPORT", "I am exporting"),
    new SelectOption("BROKERING", "I am brokering trade")
  );

  private final FormFactory formFactory;
  private final ExportCategoryController exportCategoryController;

  @Inject
  public TradeTypeController(FormFactory formFactory, ExportCategoryController exportCategoryController) {
    this.formFactory = formFactory;
    this.exportCategoryController = exportCategoryController;
  }

  public Result renderForm() {
    return ok(tradeType.render(formFactory.form(TradeTypeForm.class)));
  }

  public Result handleSubmit() {

    Form<TradeTypeForm> form = formFactory.form(TradeTypeForm.class).bindFromRequest();

    if (form.hasErrors()) {
      return ok(tradeType.render(form));
    }

    String tradeTypeParam = form.get().tradeType;
    switch (tradeTypeParam) {
      case "IMPORT":
      case "BROKERING":
        return ok("Not implemented");
      case "EXPORT":
        return exportCategoryController.renderForm(); //ok(exportCategories.render());
      default:
        return badRequest("Unknown trade type " + tradeTypeParam);
    }
  }

  public static class TradeTypeForm {

    public TradeTypeForm() {
    }

    @Required(message = "You must select a trade activity")
    public String tradeType;
  }

}
