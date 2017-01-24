package controllers.softtech;

import static java.util.concurrent.CompletableFuture.completedFuture;
import static play.mvc.Results.ok;

import com.google.inject.Inject;
import components.common.journey.JourneyManager;
import components.common.journey.StandardEvents;
import components.persistence.PermissionsFinderDao;
import exceptions.FormStateException;
import journey.helpers.SoftTechJourneyHelper;
import models.GoodsType;
import models.softtech.RelatedEquipmentDisplay;
import play.data.Form;
import play.data.FormFactory;
import play.data.validation.Constraints.Required;
import play.mvc.Result;
import views.html.softtech.relatedEquipment;

import java.util.Optional;
import java.util.concurrent.CompletionStage;

public class RelatedEquipmentController {
  private final JourneyManager journeyManager;
  private final FormFactory formFactory;
  private final PermissionsFinderDao permissionsFinderDao;

  @Inject
  public RelatedEquipmentController(JourneyManager journeyManager,
                                    FormFactory formFactory,
                                    PermissionsFinderDao permissionsFinderDao) {
    this.journeyManager = journeyManager;
    this.formFactory = formFactory;
    this.permissionsFinderDao = permissionsFinderDao;
  }

  public CompletionStage<Result> renderForm(String goodsTypeText) {
    return SoftTechJourneyHelper.validateGoodsTypeAndGetResult(goodsTypeText, this::renderFormInternal);
  }

  private CompletionStage<Result> renderFormInternal(GoodsType goodsType) {
    RelatedEquipmentForm templateForm = new RelatedEquipmentForm();
    Optional<Boolean> relatedToEquipmentOrMaterialsOptional = permissionsFinderDao.getRelatedToEquipmentOrMaterials(goodsType);
    templateForm.relatedToEquipmentOrMaterials = relatedToEquipmentOrMaterialsOptional.orElse(null);
    return completedFuture(ok(relatedEquipment.render(formFactory.form(RelatedEquipmentForm.class).fill(templateForm),
        new RelatedEquipmentDisplay(goodsType))));
  }

  public CompletionStage<Result> handleSubmit(String goodsTypeText){
    return SoftTechJourneyHelper.validateGoodsTypeAndGetResult(goodsTypeText, this::handleSubmitInternal);
  }

  public CompletionStage<Result> handleSubmitInternal(GoodsType goodsType) {
    Form<RelatedEquipmentForm> form = formFactory.form(RelatedEquipmentForm.class).bindFromRequest();
    if (form.hasErrors()) {
      return completedFuture(ok(relatedEquipment.render(form, new RelatedEquipmentDisplay(goodsType))));
    }

    Boolean relatedToEquipmentOrMaterials = form.get().relatedToEquipmentOrMaterials;

    if (relatedToEquipmentOrMaterials) {
      permissionsFinderDao.saveRelatedToEquipmentOrMaterials(goodsType, true);
      return journeyManager.performTransition(StandardEvents.YES);
    }
    else {
      permissionsFinderDao.saveRelatedToEquipmentOrMaterials(goodsType, false);
      return journeyManager.performTransition(StandardEvents.NO);
    }
  }

  public static class RelatedEquipmentForm {

    @Required(message = "You must answer this question")
    public Boolean relatedToEquipmentOrMaterials;

  }
}
