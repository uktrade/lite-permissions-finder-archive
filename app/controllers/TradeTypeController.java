package controllers;

import static java.util.concurrent.CompletableFuture.completedFuture;

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
  private final StaticContentController staticContentController;
  private final ExportCategoryController exportCategoryController;
  private final PermissionsFinderDao dao;
  private final HttpExecutionContext ec;

  private static final String UNITED_KINGDOM = "CTRY0";

  @Inject
  public TradeTypeController(FormFactory formFactory,
                             StaticContentController staticContentController,
                             ExportCategoryController exportCategoryController,
                             PermissionsFinderDao dao, HttpExecutionContext ec) {
    this.formFactory = formFactory;
    this.staticContentController = staticContentController;
    this.exportCategoryController = exportCategoryController;
    this.dao = dao;
    this.ec = ec;
  }

  public CompletionStage<Result> renderForm() {
    return completedFuture(ok(tradeType.render(formFactory.form(TradeTypeForm.class))));
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
        switch(tradeTypeOption.get()) {
          case IMPORT:
            return staticContentController.renderStaticHtml(StaticContentController.StaticHtml.IMPORT);
          case EXPORT:
            dao.saveSourceCountry(UNITED_KINGDOM);
            return exportCategoryController.renderForm();
          case BROKERING:
          case TRANSSHIPMENT:
            return staticContentController.renderStaticHtml(StaticContentController.StaticHtml.BROKERING_TRANSHIPMENT);
        }
      }
      return badRequest("Unknown trade type " + tradeTypeParam);
    }, ec.current());
  }

  public static class TradeTypeForm {

    @Required(message = "You must select a trade activity")
    public String tradeType;

  }

}
