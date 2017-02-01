package controllers.controlcode;

import static play.mvc.Results.ok;

import com.google.inject.Inject;
import components.common.journey.JourneyManager;
import components.persistence.PermissionsFinderDao;
import controllers.controlcode.notapplicable.NotApplicableControllerHelper;
import journey.Events;
import journey.helpers.ControlCodeSubJourneyHelper;
import models.ExportCategory;
import models.GoodsType;
import models.controlcode.BackType;
import models.controlcode.ControlCodeSubJourney;
import models.controlcode.NotApplicableDisplayCommon;
import models.controlcode.notapplicable.DecontrolsApplyDisplay;
import models.softtech.SoftTechCategory;
import play.data.Form;
import play.data.FormFactory;
import play.mvc.Result;
import views.html.controlcode.decontrolsApply;

import java.util.Optional;
import java.util.concurrent.CompletionStage;

public class DecontrolsApplyController {

  private final JourneyManager journeyManager;
  private final FormFactory formFactory;
  private final PermissionsFinderDao permissionsFinderDao;
  private final NotApplicableControllerHelper notApplicableControllerHelper;

  @Inject
  public DecontrolsApplyController(JourneyManager journeyManager,
                                   FormFactory formFactory,
                                   PermissionsFinderDao permissionsFinderDao,
                                   NotApplicableControllerHelper notApplicableControllerHelper) {
    this.journeyManager = journeyManager;
    this.formFactory = formFactory;
    this.permissionsFinderDao = permissionsFinderDao;
    this.notApplicableControllerHelper = notApplicableControllerHelper;
  }



  public CompletionStage<Result> renderForm(String controlCodeVariantText, String goodsTypeText) {
    ControlCodeSubJourney controlCodeSubJourney = ControlCodeSubJourneyHelper.resolveUrlToSubJourneyAndUpdateContext(controlCodeVariantText, goodsTypeText);
    return renderFormInternal(formFactory.form(DecontrolsApplyForm.class), controlCodeSubJourney);
  }

  private CompletionStage<Result> renderFormInternal(Form<?> form, ControlCodeSubJourney controlCodeSubJourney) {
    if (controlCodeSubJourney.isPhysicalGoodsSearchVariant()) {
      return notApplicableControllerHelper.physicalGoodsSearchVariant(controlCodeSubJourney, form, this::renderDecontrolsApply);
    }
    else if (controlCodeSubJourney.isSoftTechControlsVariant()) {
      return notApplicableControllerHelper.softTechControlsVariantHandleSubmit(controlCodeSubJourney, form, this::renderDecontrolsApply);
    }
    else if (controlCodeSubJourney.isSoftTechControlsRelatedToPhysicalGoodVariant()) {
      return notApplicableControllerHelper.softTechControlsRelatedToPhysicalGoodVariant(controlCodeSubJourney, form, this::renderDecontrolsApply);
    }
    else if (controlCodeSubJourney.isSoftTechCatchallControlsVariant()) {
      return notApplicableControllerHelper.softTechCatchallControlsVariant(controlCodeSubJourney, form, this::renderDecontrolsApply);
    }
    else if (controlCodeSubJourney.isNonExemptControlsVariant()) {
      return notApplicableControllerHelper.nonExemptTechnologyHandleSubmit(controlCodeSubJourney, form, this::renderDecontrolsApply);
    }
    else {
      throw new RuntimeException(String.format("Unexpected member of ControlCodeSubJourney enum: \"%s\""
          , controlCodeSubJourney.toString()));
    }
  }

  private Result renderDecontrolsApply(Form<?> form, NotApplicableDisplayCommon displayCommon) {
    @SuppressWarnings("OptionalGetWithoutIsPresent")
    ExportCategory exportCategory = permissionsFinderDao.getExportCategory().get();
    GoodsType goodsType = displayCommon.controlCodeSubJourney.getGoodsType();
    Optional<SoftTechCategory> softTechCategoryOptional = permissionsFinderDao.getSoftTechCategory(goodsType);
    return ok(decontrolsApply.render(form, new DecontrolsApplyDisplay(displayCommon, exportCategory, softTechCategoryOptional)));
  }

  public CompletionStage<Result> handleSubmit() {
    ControlCodeSubJourney controlCodeSubJourney = ControlCodeSubJourneyHelper.resolveContextToSubJourney();
    return handleSubmitInternal(controlCodeSubJourney);
  }

  private CompletionStage<Result> handleSubmitInternal(ControlCodeSubJourney controlCodeSubJourney){
    Form<DecontrolsApplyForm> form = formFactory.form(DecontrolsApplyForm.class).bindFromRequest();
    if (form.hasErrors()) {
      // TODO this should render with the form.
      return renderFormInternal(form, controlCodeSubJourney);
    }
    String action = form.get().action;
    BackType backType = BackType.valueOf(action);
    return journeyManager.performTransition(Events.BACK, backType);
  }

  public static class DecontrolsApplyForm {

    public String action;

  }
}
