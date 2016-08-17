package controllers;

import com.google.inject.Inject;
import components.persistence.PermissionsFinderDao;
import controllers.categories.ExportCategoryController;
import play.data.Form;
import play.data.FormFactory;
import play.data.validation.Constraints.Required;
import play.libs.concurrent.HttpExecutionContext;
import play.mvc.Controller;
import play.mvc.Result;
import views.html.tradeType;

import java.util.EnumSet;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;

public class TradeTypeController extends Controller {

  public enum TradeTypeOption {
    IMPORT,
    EXPORT,
    BROKERING,
    TRANSSHIPMENT
  }

  private final FormFactory formFactory;
  private final ExportCategoryController exportCategoryController;
  private final PermissionsFinderDao dao;
  private final HttpExecutionContext ec;

  private static final String UNITED_KINGDOM = "CTRY0";

  @Inject
  public TradeTypeController(FormFactory formFactory,
                             ExportCategoryController exportCategoryController,
                             PermissionsFinderDao dao, HttpExecutionContext ec) {
    this.formFactory = formFactory;
    this.exportCategoryController = exportCategoryController;
    this.dao = dao;
    this.ec = ec;
  }

  public Result renderForm() {
    return ok(tradeType.render(formFactory.form(TradeTypeForm.class)));
  }

  public CompletionStage<Result> handleSubmit() {
    return CompletableFuture.supplyAsync(() -> {
      Form<TradeTypeForm> form = formFactory.form(TradeTypeForm.class).bindFromRequest();

      if (form.hasErrors()) {
        return ok(tradeType.render(form));
      }

      String tradeTypeParam = form.get().tradeType;

      Optional<TradeTypeOption> tradeTypeOption = EnumSet.allOf(TradeTypeOption.class).stream()
          .filter(e -> e.name().equals(tradeTypeParam)).findFirst();

      if(tradeTypeOption.isPresent()) {
        if(tradeTypeOption.get() == TradeTypeOption.EXPORT) {
          dao.saveSourceCountry(UNITED_KINGDOM);
          return exportCategoryController.renderForm();
        }
        else {
          return ok("Not implemented");
        }
      }
      else {
        return badRequest("Unknown trade type " + tradeTypeParam);
      }
    }, ec.current());
  }

  public static class TradeTypeForm {

    @Required(message = "You must select a trade activity")
    public String tradeType;

  }

}
