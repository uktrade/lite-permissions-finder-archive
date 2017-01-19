package controllers.softtech.controls;

import static play.mvc.Results.ok;

import com.google.inject.Inject;
import components.common.journey.JourneyManager;
import components.persistence.PermissionsFinderDao;
import components.services.controlcode.controls.catchall.CatchallControlsServiceClient;
import components.services.controlcode.controls.category.CategoryControlsServiceClient;
import components.services.controlcode.controls.related.RelatedControlsServiceClient;
import exceptions.FormStateException;
import journey.Events;
import journey.helpers.ControlCodeSubJourneyHelper;
import models.GoodsType;
import models.controlcode.ControlCodeSubJourney;
import models.softtech.SoftTechCategory;
import models.softtech.controls.SoftTechControlsDisplay;
import models.softtech.controls.SoftTechControlsJourney;
import org.apache.commons.lang3.StringUtils;
import play.data.Form;
import play.data.FormFactory;
import play.libs.concurrent.HttpExecutionContext;
import play.mvc.Result;
import views.html.softtech.controls.softTechControls;

import java.util.concurrent.CompletionStage;

public class SoftTechControlsController {
  private final JourneyManager journeyManager;
  private final FormFactory formFactory;
  private final PermissionsFinderDao permissionsFinderDao;
  private final CategoryControlsServiceClient categoryControlsServiceClient;
  private final RelatedControlsServiceClient relatedControlsServiceClient;
  private final CatchallControlsServiceClient catchallControlsServiceClient;
  private final HttpExecutionContext httpExecutionContext;

  @Inject
  public SoftTechControlsController(JourneyManager journeyManager,
                                    FormFactory formFactory,
                                    PermissionsFinderDao permissionsFinderDao,
                                    CategoryControlsServiceClient categoryControlsServiceClient,
                                    RelatedControlsServiceClient relatedControlsServiceClient,
                                    CatchallControlsServiceClient catchallControlsServiceClient,
                                    HttpExecutionContext httpExecutionContext) {
    this.journeyManager = journeyManager;
    this.formFactory = formFactory;
    this.permissionsFinderDao = permissionsFinderDao;
    this.categoryControlsServiceClient = categoryControlsServiceClient;
    this.relatedControlsServiceClient = relatedControlsServiceClient;
    this.catchallControlsServiceClient = catchallControlsServiceClient;
    this.httpExecutionContext = httpExecutionContext;
  }

  private CompletionStage<Result> renderForm(SoftTechControlsJourney softTechControlsJourney) {
    // Set SubJourney context
    ControlCodeSubJourneyHelper.updateSubJourneyContext(softTechControlsJourney.getMappedControlCodeSubJourney());
    return renderWithForm(softTechControlsJourney, formFactory.form(SoftTechControlsForm.class));
  }

  public CompletionStage<Result> renderCategoryForm(String goodsTypeText) {
    GoodsType goodsType = GoodsType.getMatchedByUrlString(goodsTypeText).get();
    SoftTechControlsJourney softTechControlsJourney = SoftTechControlsJourney.getCategoryVariant(goodsType);
    return renderForm(softTechControlsJourney);
  }

  public CompletionStage<Result> renderRelatedToPhysicalGoodForm(String goodsTypeText) {
    GoodsType goodsType = GoodsType.getMatchedByUrlString(goodsTypeText).get();
    SoftTechControlsJourney softTechControlsJourney = SoftTechControlsJourney.getRelatedToPhysicalGoodsVariant(goodsType);
    return renderForm(softTechControlsJourney);
  }

  public CompletionStage<Result> renderCatchallControlsForm(String goodsTypeText) {
    GoodsType goodsType = GoodsType.getMatchedByUrlString(goodsTypeText).get();
    SoftTechControlsJourney softTechControlsJourney = SoftTechControlsJourney.getCatchallVariant(goodsType);
    return renderForm(softTechControlsJourney);
  }

  private CompletionStage<Result> handleSubmit(SoftTechControlsJourney softTechControlsJourney) {
    Form<SoftTechControlsForm> form = formFactory.form(SoftTechControlsForm.class).bindFromRequest();
    if (form.hasErrors()) {
      renderWithForm(softTechControlsJourney, form);
    }
    String action = form.get().action;
    String controlCode = form.get().controlCode;
    if (StringUtils.isNotEmpty(action)) {
      if ("noMatchedControlCode".equals(action)) {
        if (softTechControlsJourney.isCategoryVariant() ||
            softTechControlsJourney.isRelatedToPhysicalGoodsVariant() ||
            softTechControlsJourney.isCatchallVariant()) {
          return journeyManager.performTransition(Events.NONE_MATCHED);
        }
        else {
          throw new RuntimeException(String.format("Unexpected member of SoftTechControlsJourney enum: \"%s\""
              , softTechControlsJourney.toString()));
        }
      }
      else {
        throw new FormStateException(String.format("Unknown value for action: \"%s\"", action));
      }
    }
    else if (StringUtils.isNotEmpty(controlCode)) {
      // Setup DAO state based on view variant
      permissionsFinderDao.clearAndUpdateControlCodeSubJourneyDaoFieldsIfChanged(softTechControlsJourney.getMappedControlCodeSubJourney(), controlCode);
      return journeyManager.performTransition(Events.CONTROL_CODE_SELECTED);
    }
    else {
      throw new FormStateException("Unhandled form state");
    }
  }

  public CompletionStage<Result> handleCategorySubmit(String goodsTypeText) {
    GoodsType goodsType = GoodsType.getMatchedByUrlString(goodsTypeText).get();
    SoftTechControlsJourney softTechControlsJourney = SoftTechControlsJourney.getCategoryVariant(goodsType);
    return handleSubmit(softTechControlsJourney);
  }

  public CompletionStage<Result> handleRelatedToPhysicalGoodSubmit(String goodsTypeText) {
    GoodsType goodsType = GoodsType.getMatchedByUrlString(goodsTypeText).get();
    SoftTechControlsJourney softTechControlsJourney = SoftTechControlsJourney.getRelatedToPhysicalGoodsVariant(goodsType);
    return handleSubmit(softTechControlsJourney);
  }

  public CompletionStage<Result> handleCatchallControlsSubmit(String goodsTypeText) {
    GoodsType goodsType = GoodsType.getMatchedByUrlString(goodsTypeText).get();
    SoftTechControlsJourney softTechControlsJourney = SoftTechControlsJourney.getCatchallVariant(goodsType);
    return handleSubmit(softTechControlsJourney);
  }

  private CompletionStage<Result> renderWithForm(SoftTechControlsJourney softTechControlsJourney, Form<SoftTechControlsForm> form) {
    // Setup DAO state based on view variant
    GoodsType goodsType = softTechControlsJourney.getSoftTechGoodsType();
    ControlCodeSubJourney subJourney = softTechControlsJourney.getMappedControlCodeSubJourney();
    if (softTechControlsJourney.isCategoryVariant()) {
      // Software category is expected at this stage of the journey
      SoftTechCategory softTechCategory = permissionsFinderDao.getSoftTechCategory(goodsType).get();
      return categoryControlsServiceClient.get(goodsType, softTechCategory)
          .thenApplyAsync(result ->
              validateResultsSizeAndRender(new SoftTechControlsDisplay(form, softTechControlsJourney, result.controlCodes))
          , httpExecutionContext.current());
    }
    else if (softTechControlsJourney.isRelatedToPhysicalGoodsVariant()) {
      // Find the selected code of the prior sub journey (physical goods search variant)
      ControlCodeSubJourney priorSubJourney = ControlCodeSubJourney.getPhysicalGoodsSearchVariant(goodsType);
      String controlCode = permissionsFinderDao.getSelectedControlCode(priorSubJourney);
      return relatedControlsServiceClient.get(goodsType, controlCode)
          .thenApplyAsync(result ->
              validateResultsSizeAndRender(new SoftTechControlsDisplay(form, softTechControlsJourney, result.controlCodes))
              , httpExecutionContext.current());
    }
    else if (softTechControlsJourney.isCatchallVariant()) {
      SoftTechCategory softTechCategory = permissionsFinderDao.getSoftTechCategory(goodsType).get();
      return catchallControlsServiceClient.get(goodsType, softTechCategory)
          .thenApplyAsync(result ->
              validateResultsSizeAndRender(new SoftTechControlsDisplay(form, softTechControlsJourney, result.controlCodes))
              , httpExecutionContext.current());
    }
    else {
      throw new RuntimeException(String.format("Unexpected member of SoftTechControlsJourney enum: \"%s\""
          , softTechControlsJourney.toString()));
    }
  }

  /**
   * Takes a SoftTechControlsDisplay object and performs a belt and braces check for the size of the control code list
   * An empty list shouldn't be possible.
   * It's a little hacky, validating the display object instead of the source service result.
   * @param display
   * @return rendered softTechControls view or a RuntimeException for an invalid controlCode.size()
   */
  private Result validateResultsSizeAndRender(SoftTechControlsDisplay display) {
    if (display.controlCodes.size() > 1) {
      /**
       * Expecting more than one control code here. 1 or 0 control codes should not reach this point (and should
       * have prompted a different transition)
       */
      return ok(softTechControls.render(display));
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