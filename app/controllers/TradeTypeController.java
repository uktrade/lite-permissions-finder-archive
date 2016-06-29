package controllers;

import com.google.inject.Inject;
import play.data.Form;
import play.data.FormFactory;
import play.data.validation.Constraints.Required;
import play.mvc.Controller;
import play.mvc.Result;
import utils.common.CheckboxOption;
import views.html.tradeType;
import views.html.exportCategories;

import java.util.Arrays;
import java.util.List;

public class TradeTypeController extends Controller {

  public static final List<CheckboxOption> TRADE_TYPE_OPTIONS = Arrays.asList(
    new CheckboxOption("IMPORT", "I am importing"),
    new CheckboxOption("EXPORT", "I am exporting"),
    new CheckboxOption("BROKERING", "I am brokering trade")
  );

  private final FormFactory formFactory;

  @Inject
  public TradeTypeController(FormFactory formFactory) {
    this.formFactory = formFactory;
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
        return ok(exportCategories.render());
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
