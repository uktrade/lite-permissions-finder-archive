package controllers;

import static java.util.concurrent.CompletableFuture.completedFuture;

import com.google.inject.Inject;
import components.common.journey.JourneyManager;
import components.persistence.PermissionsFinderDao;
import journey.Events;
import model.GoodsType;
import play.data.Form;
import play.data.FormFactory;
import play.data.validation.Constraints.Required;
import play.mvc.Controller;
import play.mvc.Result;
import views.html.goodsType;

import java.util.Optional;
import java.util.concurrent.CompletionStage;

public class GoodsTypeController extends Controller {

  private final JourneyManager jm;
  private final FormFactory formFactory;
  private final PermissionsFinderDao dao;

  @Inject
  public GoodsTypeController(JourneyManager jm, FormFactory formFactory, PermissionsFinderDao dao) {
    this.jm = jm;
    this.formFactory = formFactory;
    this.dao = dao;
  }

  public Result renderForm() {
    GoodsTypeForm templateForm = new GoodsTypeForm();
    Optional<GoodsType> goodsTypeOptional = dao.getGoodsType();
    templateForm.goodsType = goodsTypeOptional.isPresent() ? goodsTypeOptional.get().value() : "";
    return ok(goodsType.render(formFactory.form(GoodsTypeForm.class).fill(templateForm)));
  }

  public CompletionStage<Result> handleSubmit() {

    Form<GoodsTypeForm> form = formFactory.form(GoodsTypeForm.class).bindFromRequest();

    if (form.hasErrors()) {
      return completedFuture(ok(goodsType.render(form)));
    }

    String goodsTypeParam = form.get().goodsType;
    Optional<GoodsType> goodsTypeOptional = GoodsType.getMatched(goodsTypeParam);

    if(goodsTypeOptional.isPresent()) {
      dao.saveGoodsType(goodsTypeOptional.get());
      return jm.performTransition(Events.GOODS_TYPE_SELECTED, goodsTypeOptional.get());
    }

    return completedFuture(badRequest("Unknown goods type " + goodsTypeParam));
  }

  public static class GoodsTypeForm {

    @Required(message = "You must select what you are exporting")
    public String goodsType;

  }

}
