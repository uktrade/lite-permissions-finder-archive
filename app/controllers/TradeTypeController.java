package controllers;

import com.google.inject.Inject;
import controllers.categories.ExportCategoryController;
import play.data.Form;
import play.data.FormFactory;
import play.data.validation.Constraints.Required;
import play.mvc.Controller;
import play.mvc.Result;
import views.html.tradeType;

import java.util.EnumSet;
import java.util.Optional;

public class TradeTypeController extends Controller {

  public enum TradeTypeOption {
    IMPORT,
    EXPORT,
    BROKERING,
    TRANSSHIPMENT
  }

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

    Optional<TradeTypeOption> tradeTypeOption = EnumSet.allOf(TradeTypeOption.class).stream()
        .filter(e -> e.name().equals(tradeTypeParam)).findFirst();

    if(tradeTypeOption.isPresent()) {
      if(tradeTypeOption.get() == TradeTypeOption.EXPORT) {
        return exportCategoryController.renderForm();
      }
      else {
        return ok("Not implemented");
      }
    }
    else {
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
