package controllers.softtech.controls;

import static play.mvc.Results.ok;

import com.google.inject.Inject;
import components.common.journey.JourneyManager;
import components.persistence.PermissionsFinderDao;
import components.services.controlcode.controls.ControlCode;
import components.services.controlcode.controls.catchall.CatchallControlsServiceClient;
import components.services.controlcode.controls.category.CategoryControlsServiceClient;
import components.services.controlcode.controls.nonexempt.NonExemptControlServiceClient;
import components.services.controlcode.controls.nonexempt.NonExemptControlsServiceResult;
import components.services.controlcode.controls.related.RelatedControlsServiceClient;
import exceptions.FormStateException;
import journey.Events;
import journey.helpers.ControlCodeSubJourneyHelper;
import models.GoodsType;
import models.controlcode.ControlCodeSubJourney;
import models.softtech.SoftTechCategory;
import models.softtech.controls.SoftTechControlsDisplay;
import org.apache.commons.lang3.StringUtils;
import play.data.Form;
import play.data.FormFactory;
import play.libs.concurrent.HttpExecutionContext;
import play.mvc.Result;
import views.html.softtech.controls.softTechControls;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletionStage;

public class SoftTechControlsController {
  private final JourneyManager journeyManager;
  private final FormFactory formFactory;
  private final PermissionsFinderDao permissionsFinderDao;
  private final CategoryControlsServiceClient categoryControlsServiceClient;
  private final RelatedControlsServiceClient relatedControlsServiceClient;
  private final CatchallControlsServiceClient catchallControlsServiceClient;
  private final NonExemptControlServiceClient nonExemptControlServiceClient;
  private final HttpExecutionContext httpExecutionContext;

  @Inject
  public SoftTechControlsController(JourneyManager journeyManager,
                                    FormFactory formFactory,
                                    PermissionsFinderDao permissionsFinderDao,
                                    CategoryControlsServiceClient categoryControlsServiceClient,
                                    RelatedControlsServiceClient relatedControlsServiceClient,
                                    CatchallControlsServiceClient catchallControlsServiceClient,
                                    NonExemptControlServiceClient nonExemptControlServiceClient,
                                    HttpExecutionContext httpExecutionContext) {
    this.journeyManager = journeyManager;
    this.formFactory = formFactory;
    this.permissionsFinderDao = permissionsFinderDao;
    this.categoryControlsServiceClient = categoryControlsServiceClient;
    this.relatedControlsServiceClient = relatedControlsServiceClient;
    this.catchallControlsServiceClient = catchallControlsServiceClient;
    this.nonExemptControlServiceClient = nonExemptControlServiceClient;
    this.httpExecutionContext = httpExecutionContext;
  }

  public CompletionStage<Result> renderForm(String controlCodeVariantText, String goodsTypeText) {
    ControlCodeSubJourney controlCodeSubJourney = ControlCodeSubJourneyHelper.resolveUrlToSubJourneyAndUpdateContext(controlCodeVariantText, goodsTypeText);
    return renderWithForm(controlCodeSubJourney, formFactory.form(SoftTechControlsForm.class));
  }

  public CompletionStage<Result> handleSubmit() {
    ControlCodeSubJourney controlCodeSubJourney = ControlCodeSubJourneyHelper.resolveContextToSubJourney();
    return handleSubmitInternal(controlCodeSubJourney);
  }

  private CompletionStage<Result> handleSubmitInternal(ControlCodeSubJourney controlCodeSubJourney) {
    Form<SoftTechControlsForm> form = formFactory.form(SoftTechControlsForm.class).bindFromRequest();
    if (form.hasErrors()) {
      renderWithForm(controlCodeSubJourney, form);
    }
    String action = form.get().action;
    String controlCode = form.get().controlCode;
    if (StringUtils.isNotEmpty(action)) {
      if ("noMatchedControlCode".equals(action)) {
        if (controlCodeSubJourney.isSoftTechControlsVariant() ||
            controlCodeSubJourney.isSoftTechControlsRelatedToPhysicalGoodVariant() ||
            controlCodeSubJourney.isSoftTechCatchallControlsVariant() ||
            controlCodeSubJourney.isNonExemptControlsVariant()) {
          return journeyManager.performTransition(Events.NONE_MATCHED);
        }
        else {
          throw new RuntimeException(String.format("Unexpected member of ControlCodeSubJourney enum: \"%s\""
              , controlCodeSubJourney.toString()));
        }
      }
      else {
        throw new FormStateException(String.format("Unknown value for action: \"%s\"", action));
      }
    }
    else if (StringUtils.isNotEmpty(controlCode)) {
      // Setup DAO state based on view variant
      permissionsFinderDao.clearAndUpdateControlCodeSubJourneyDaoFieldsIfChanged(controlCodeSubJourney, controlCode);
      return journeyManager.performTransition(Events.CONTROL_CODE_SELECTED);
    }
    else {
      throw new FormStateException("Unhandled form state");
    }
  }

  private CompletionStage<Result> renderWithForm(ControlCodeSubJourney controlCodeSubJourney, Form<SoftTechControlsForm> form) {
    // Setup DAO state based on view variant
    GoodsType goodsType = controlCodeSubJourney.getSoftTechGoodsType();
    if (controlCodeSubJourney.isSoftTechControlsVariant()) {
      // Software category is expected at this stage of the journey
      SoftTechCategory softTechCategory = permissionsFinderDao.getSoftTechCategory(goodsType).get();
      return categoryControlsServiceClient.get(goodsType, softTechCategory)
          .thenApplyAsync(result ->
              ok(softTechControls.render(form, checkResultsSize(new SoftTechControlsDisplay(controlCodeSubJourney, result.controlCodes))))
              , httpExecutionContext.current());
    }
    else if (controlCodeSubJourney.isSoftTechControlsRelatedToPhysicalGoodVariant()) {
      // Find the selected code of the prior sub journey (physical goods search variant)
      ControlCodeSubJourney priorSubJourney = ControlCodeSubJourney.getPhysicalGoodsSearchVariant(goodsType);
      String controlCode = permissionsFinderDao.getSelectedControlCode(priorSubJourney);
      return relatedControlsServiceClient.get(goodsType, controlCode)
          .thenApplyAsync(result ->
              ok(softTechControls.render(form, checkResultsSize(new SoftTechControlsDisplay(controlCodeSubJourney, result.controlCodes))))
              , httpExecutionContext.current());
    }
    else if (controlCodeSubJourney.isSoftTechCatchallControlsVariant()) {
      SoftTechCategory softTechCategory = permissionsFinderDao.getSoftTechCategory(goodsType).get();
      return catchallControlsServiceClient.get(goodsType, softTechCategory)
          .thenApplyAsync(result ->
              ok(softTechControls.render(form, checkResultsSize(new SoftTechControlsDisplay(controlCodeSubJourney, result.controlCodes))))
              , httpExecutionContext.current());
    }
    else if (controlCodeSubJourney.isNonExemptControlsVariant()) {
      CompletionStage<NonExemptControlsServiceResult> specialMaterialsStage = nonExemptControlServiceClient.get(goodsType, SoftTechCategory.SPECIAL_MATERIALS);
      CompletionStage<NonExemptControlsServiceResult> marineStage = nonExemptControlServiceClient.get(goodsType, SoftTechCategory.MARINE);
      return specialMaterialsStage.thenCombineAsync(marineStage, (specialMaterialsResult, marineResult) -> {
        List<ControlCode> controlCodes = new ArrayList<>(specialMaterialsResult.controlCodes);
        controlCodes.addAll(marineResult.controlCodes);
        return ok(softTechControls.render(form, checkResultsSize(new SoftTechControlsDisplay(controlCodeSubJourney, controlCodes))));
      }, httpExecutionContext.current());
    }
    else {
      throw new RuntimeException(String.format("Unexpected member of ControlCodeSubJourney enum: \"%s\""
          , controlCodeSubJourney.toString()));
    }
  }

  /**
   * Takes a SoftTechControlsDisplay object and performs a belt and braces check for the size of the control code list
   * An empty list shouldn't be possible.
   * @param display
   * @return the display object
   */
  private SoftTechControlsDisplay checkResultsSize(SoftTechControlsDisplay display) {
    if (display.controlCodes.size() > 1) {
      return display;
    }
    else {
      throw new RuntimeException(String.format("Invalid value for size: \"%d\"", display.controlCodes.size()));
    }
  }

  public static class SoftTechControlsForm {

    public String controlCode;

    public String action;

  }

}