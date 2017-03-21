package controllers;

import static java.util.concurrent.CompletableFuture.completedFuture;

import com.google.inject.Inject;
import components.common.journey.JourneyManager;
import components.persistence.PermissionsFinderDao;
import exceptions.FormStateException;
import journey.Events;
import models.ExportCategory;
import models.GoodsType;
import models.softtech.SoftTechCategory;
import play.data.Form;
import play.data.FormFactory;
import play.data.validation.Constraints.Required;
import play.mvc.Controller;
import play.mvc.Result;
import views.html.goodsType;

import java.util.Optional;
import java.util.concurrent.CompletionStage;

public class GoodsTypeController extends Controller {

  private final JourneyManager journeyManager;
  private final FormFactory formFactory;
  private final PermissionsFinderDao permissionsFinderDao;

  @Inject
  public GoodsTypeController(JourneyManager journeyManager, FormFactory formFactory,
                             PermissionsFinderDao permissionsFinderDao) {
    this.journeyManager = journeyManager;
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
    Optional<GoodsType> goodsTypeOptional = GoodsType.getMatchedByValue(goodsTypeParam);

    if(goodsTypeOptional.isPresent()) {
      GoodsType goodsType = goodsTypeOptional.get();
      // Convert and save ExportCategory to SoftTechCategory in the dao, if exporting military
      // TODO, this isn't great. Could refactor as part of the CategoryControlsDecider
      if (goodsType == GoodsType.SOFTWARE || goodsType == GoodsType.TECHNOLOGY) {
        ExportCategory exportCategory = permissionsFinderDao.getExportCategory().get();
        if (exportCategory == ExportCategory.MILITARY) {
          permissionsFinderDao.saveSoftTechCategory(goodsType, SoftTechCategory.MILITARY);
        }
      }
      permissionsFinderDao.saveGoodsType(goodsType);
      return journeyManager.performTransition(Events.GOODS_TYPE_SELECTED, goodsTypeOptional.get());
    }

    throw new FormStateException("Unknown goods type " + goodsTypeParam);
  }

  public static class GoodsTypeForm {

    @Required(message = "Choose an item type")
    public String goodsType;

  }

}
