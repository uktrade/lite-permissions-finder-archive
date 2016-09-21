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
  private final PermissionsFinderDao permissionsFinderDao;

  @Inject
  public GoodsTypeController(JourneyManager jm, FormFactory formFactory, PermissionsFinderDao permissionsFinderDao) {
    this.jm = jm;
    this.formFactory = formFactory;
    this.permissionsFinderDao = permissionsFinderDao;
  }

  public Result renderForm() {
    GoodsTypeForm templateForm = new GoodsTypeForm();
    Optional<GoodsType> goodsTypeOptional = permissionsFinderDao.getGoodsType();
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
      permissionsFinderDao.saveGoodsType(goodsTypeOptional.get());
      return jm.performTransition(Events.GOODS_TYPE_SELECTED, goodsTypeOptional.get());
    }

    return completedFuture(badRequest("Unknown goods type " + goodsTypeParam));
  }

  public static class GoodsTypeForm {

    @Required(message = "You must select what you are exporting")
    public String goodsType;

  }

}
