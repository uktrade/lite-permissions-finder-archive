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
import journey.helpers.SoftTechJourneyHelper;
import models.GoodsType;
import models.controlcode.ControlCodeJourney;
import models.softtech.SoftTechCategory;
import models.softtech.controls.SoftTechControlsJourney;
import models.softtech.controls.SoftTechControlsDisplay;
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
  private final SoftTechJourneyHelper softTechJourneyHelper;

  @Inject
  public SoftTechControlsController(JourneyManager journeyManager,
                                    FormFactory formFactory,
                                    PermissionsFinderDao permissionsFinderDao,
                                    CategoryControlsServiceClient categoryControlsServiceClient,
                                    RelatedControlsServiceClient relatedControlsServiceClient,
                                    CatchallControlsServiceClient catchallControlsServiceClient,
                                    HttpExecutionContext httpExecutionContext,
                                    SoftTechJourneyHelper softTechJourneyHelper) {
    this.journeyManager = journeyManager;
    this.formFactory = formFactory;
    this.permissionsFinderDao = permissionsFinderDao;
    this.categoryControlsServiceClient = categoryControlsServiceClient;
    this.relatedControlsServiceClient = relatedControlsServiceClient;
    this.catchallControlsServiceClient = catchallControlsServiceClient;
    this.httpExecutionContext = httpExecutionContext;
    this.softTechJourneyHelper = softTechJourneyHelper;
  }

  private CompletionStage<Result> renderForm(SoftTechControlsJourney softTechControlsJourney) {
    return renderWithForm(softTechControlsJourney, formFactory.form(SoftTechControlsForm.class));
  }

  public CompletionStage<Result> renderSofwareCategoryForm() {
    return renderForm(SoftTechControlsJourney.SOFTWARE_CATEGORY);
  }

  public CompletionStage<Result> renderRelatedToPhysicalGoodForm() {
    return renderForm(SoftTechControlsJourney.SOFTWARE_RELATED_TO_A_PHYSICAL_GOOD);
  }

  public CompletionStage<Result> renderCatchallControlsForm(String goodsTypeText) {
    return SoftTechJourneyHelper.getCatchallControlsResult(goodsTypeText, this::renderForm);
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
        if (softTechControlsJourney == SoftTechControlsJourney.SOFTWARE_CATEGORY) {
          return journeyManager.performTransition(Events.NONE_MATCHED);
        }
        else if (softTechControlsJourney == SoftTechControlsJourney.SOFTWARE_RELATED_TO_A_PHYSICAL_GOOD) {
          return softTechJourneyHelper.performCatchallSoftwareControlsTransition();
        }
        else if (softTechControlsJourney == SoftTechControlsJourney.SOFTWARE_CATCHALL) {
          return softTechJourneyHelper.performCatchallSoftTechControlRelationshipTransition(GoodsType.SOFTWARE);
        }
        else if (softTechControlsJourney == SoftTechControlsJourney.TECHNOLOGY_CATCHALL) {
          return softTechJourneyHelper.performCatchallSoftTechControlRelationshipTransition(GoodsType.TECHNOLOGY);
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
      if (softTechControlsJourney == SoftTechControlsJourney.SOFTWARE_CATEGORY) {
        permissionsFinderDao.clearAndUpdateControlCodeJourneyDaoFieldsIfChanged(ControlCodeJourney.SOFTWARE_CONTROLS, controlCode);
      }
      else if (softTechControlsJourney == SoftTechControlsJourney.SOFTWARE_RELATED_TO_A_PHYSICAL_GOOD) {
        permissionsFinderDao.clearAndUpdateControlCodeJourneyDaoFieldsIfChanged(ControlCodeJourney.SOFTWARE_CONTROLS_RELATED_TO_A_PHYSICAL_GOOD, controlCode);
      }
      else if (softTechControlsJourney == SoftTechControlsJourney.SOFTWARE_CATCHALL) {
        permissionsFinderDao.clearAndUpdateControlCodeJourneyDaoFieldsIfChanged(ControlCodeJourney.SOFTWARE_CATCHALL_CONTROLS, controlCode);
      }
      else if (softTechControlsJourney == SoftTechControlsJourney.TECHNOLOGY_CATCHALL) {
        permissionsFinderDao.clearAndUpdateControlCodeJourneyDaoFieldsIfChanged(ControlCodeJourney.TECHNOLOGY_CATCHALL_CONTROLS, controlCode);
      }
      else {
        throw new RuntimeException(String.format("Unexpected member of SoftTechControlsJourney enum: \"%s\""
            , softTechControlsJourney.toString()));
      }
      return journeyManager.performTransition(Events.CONTROL_CODE_SELECTED);
    }
    else {
      throw new FormStateException("Unhandled form state");
    }
  }

  public CompletionStage<Result> handleSoftwareCategorySubmit() {
    return handleSubmit(SoftTechControlsJourney.SOFTWARE_CATEGORY);
  }

  public CompletionStage<Result> handleRelatedToPhysicalGoodSubmit() {
    return handleSubmit(SoftTechControlsJourney.SOFTWARE_RELATED_TO_A_PHYSICAL_GOOD);
  }

  public CompletionStage<Result> handleCatchallControlsSubmit(String goodsTypeText) {
    return SoftTechJourneyHelper.getCatchallControlsResult(goodsTypeText, this::handleSubmit);
  }

  private CompletionStage<Result> renderWithForm(SoftTechControlsJourney softTechControlsJourney, Form<SoftTechControlsForm> form) {
    // Setup DAO state based on view variant
    if (softTechControlsJourney == SoftTechControlsJourney.SOFTWARE_CATEGORY) {
      // Software category is expected at this stage of the journey
      SoftTechCategory softTechCategory = permissionsFinderDao.getSoftTechCategory(GoodsType.SOFTWARE).get();
      return categoryControlsServiceClient.get(GoodsType.SOFTWARE, softTechCategory) // TODO TECHNOLOGY
          .thenApplyAsync(result -> {
            SoftTechControlsDisplay display = new SoftTechControlsDisplay(softTechControlsJourney, result.controlCodes);
            return ok(softTechControls.render(form, display));
          }, httpExecutionContext.current());
    }
    else if (softTechControlsJourney == SoftTechControlsJourney.SOFTWARE_RELATED_TO_A_PHYSICAL_GOOD) {

      // Note, this is looking at the selected physical good control code which is related to their physical good
      String controlCode = permissionsFinderDao.getSelectedControlCode(ControlCodeJourney.PHYSICAL_GOODS_SEARCH_RELATED_TO_SOFTWARE);

      return relatedControlsServiceClient.get(GoodsType.SOFTWARE, controlCode) // TODO TECHNOLOGY
          .thenApplyAsync(result -> {
            int size = result.controlCodes.size();
            if (size > 1) {
              /**
               * Expecting more than one control code here. 1 or 0 control codes should not reach this point (and should
               * have prompted a different transition)
               */
              SoftTechControlsDisplay display = new SoftTechControlsDisplay(softTechControlsJourney, result.controlCodes);
              return ok(softTechControls.render(form, display));
            }
            else {
              throw new RuntimeException(String.format("Invalid value for size: \"%d\"", size));
            }
          }, httpExecutionContext.current());
    }
    else if (softTechControlsJourney == SoftTechControlsJourney.SOFTWARE_CATCHALL) {
      return catchallControls(softTechControlsJourney, form, GoodsType.SOFTWARE);
    }
    else if (softTechControlsJourney == SoftTechControlsJourney.TECHNOLOGY_CATCHALL) {
      return catchallControls(softTechControlsJourney, form, GoodsType.TECHNOLOGY);
    }
    else {
      throw new RuntimeException(String.format("Unexpected member of SoftTechControlsJourney enum: \"%s\""
          , softTechControlsJourney.toString()));
    }
  }

  private CompletionStage<Result> catchallControls(SoftTechControlsJourney softTechControlsJourney, Form<SoftTechControlsForm> form, GoodsType goodsType) {
    // SoftTech category is expected at this stage of the journey
    SoftTechCategory softTechCategory = permissionsFinderDao.getSoftTechCategory(GoodsType.SOFTWARE).get();
    return catchallControlsServiceClient.get(goodsType, softTechCategory)
        .thenApplyAsync(result -> {
          int size = result.controlCodes.size();
          if (size > 1) {
            /**
             * Expecting more than one control code here. 1 or 0 control codes should not reach this point (and should
             * have prompted a different transition)
             */
            SoftTechControlsDisplay display = new SoftTechControlsDisplay(softTechControlsJourney, result.controlCodes);
            return ok(softTechControls.render(form, display));
          }
          else {
            throw new RuntimeException(String.format("Invalid value for size: \"%d\"", size));
          }
        }, httpExecutionContext.current());
  }

  public static class SoftTechControlsForm {

    public String controlCode;

    public String action;

  }

}