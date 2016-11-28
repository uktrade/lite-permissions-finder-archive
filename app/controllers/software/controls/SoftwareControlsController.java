package controllers.software.controls;

import static play.mvc.Results.ok;

import com.google.inject.Inject;
import components.common.journey.JourneyManager;
import components.persistence.PermissionsFinderDao;
import components.services.controlcode.controls.catchall.CatchallControlsServiceClient;
import components.services.controlcode.controls.category.CategoryControlsServiceClient;
import components.services.controlcode.controls.related.RelatedControlsServiceClient;
import exceptions.FormStateException;
import journey.Events;
import journey.helpers.ControlCodeJourneyHelper;
import journey.helpers.SoftwareJourneyHelper;
import models.GoodsType;
import models.controlcode.ControlCodeJourney;
import models.software.SoftwareCategory;
import models.software.controls.SoftwareControlsDisplay;
import models.software.controls.SoftwareControlsJourney;
import org.apache.commons.lang3.StringUtils;
import play.data.Form;
import play.data.FormFactory;
import play.libs.concurrent.HttpExecutionContext;
import play.mvc.Result;
import views.html.software.controls.softwareControls;

import java.util.concurrent.CompletionStage;

public class SoftwareControlsController {
  private final JourneyManager journeyManager;
  private final FormFactory formFactory;
  private final PermissionsFinderDao permissionsFinderDao;
  private final CategoryControlsServiceClient categoryControlsServiceClient;
  private final RelatedControlsServiceClient relatedControlsServiceClient;
  private final CatchallControlsServiceClient catchallControlsServiceClient;
  private final HttpExecutionContext httpExecutionContext;
  private final ControlCodeJourneyHelper controlCodeJourneyHelper;
  private final SoftwareJourneyHelper softwareJourneyHelper;

  @Inject
  public SoftwareControlsController(JourneyManager journeyManager,
                                    FormFactory formFactory,
                                    PermissionsFinderDao permissionsFinderDao,
                                    CategoryControlsServiceClient categoryControlsServiceClient,
                                    RelatedControlsServiceClient relatedControlsServiceClient,
                                    CatchallControlsServiceClient catchallControlsServiceClient,
                                    HttpExecutionContext httpExecutionContext,
                                    ControlCodeJourneyHelper controlCodeJourneyHelper,
                                    SoftwareJourneyHelper softwareJourneyHelper) {
    this.journeyManager = journeyManager;
    this.formFactory = formFactory;
    this.permissionsFinderDao = permissionsFinderDao;
    this.categoryControlsServiceClient = categoryControlsServiceClient;
    this.relatedControlsServiceClient = relatedControlsServiceClient;
    this.catchallControlsServiceClient = catchallControlsServiceClient;
    this.httpExecutionContext = httpExecutionContext;
    this.controlCodeJourneyHelper = controlCodeJourneyHelper;
    this.softwareJourneyHelper = softwareJourneyHelper;
  }

  private CompletionStage<Result> renderForm(SoftwareControlsJourney softwareControlsJourney) {
    return renderWithForm(softwareControlsJourney, formFactory.form(SoftwareControlsForm.class));
  }

  public CompletionStage<Result> renderSofwareCategoryForm() {
    return renderForm(SoftwareControlsJourney.SOFTWARE_CATEGORY);
  }

  public CompletionStage<Result> renderRelatedToPhysicalGoodForm() {
    return renderForm(SoftwareControlsJourney.SOFTWARE_RELATED_TO_A_PHYSICAL_GOOD);
  }

  public CompletionStage<Result> renderSoftwareCatchallForm() {
    return renderForm(SoftwareControlsJourney.SOFTWARE_CATCHALL);
  }

  private CompletionStage<Result> handleSubmit(SoftwareControlsJourney softwareControlsJourney) {
    Form<SoftwareControlsForm> form = formFactory.form(SoftwareControlsForm.class).bindFromRequest();
    if (form.hasErrors()) {
      renderWithForm(softwareControlsJourney, form);
    }
    String action = form.get().action;
    String controlCode = form.get().controlCode;
    if (StringUtils.isNotEmpty(action)) {
      if ("noMatchedControlCode".equals(action)) {
        if (softwareControlsJourney == SoftwareControlsJourney.SOFTWARE_CATEGORY) {
          return journeyManager.performTransition(Events.NONE_MATCHED);
        }
        else if (softwareControlsJourney == SoftwareControlsJourney.SOFTWARE_RELATED_TO_A_PHYSICAL_GOOD) {
          return softwareJourneyHelper.performCatchallSoftwareControlsTransition();
        }
        else if (softwareControlsJourney == SoftwareControlsJourney.SOFTWARE_CATCHALL) {
          return softwareJourneyHelper.performCatchallSoftwareControlRelationshipTransition();
        }
        else {
          throw new RuntimeException(String.format("Unexpected member of SoftwareControlsJourney enum: \"%s\""
              , softwareControlsJourney.toString()));
        }
      }
      else {
        throw new FormStateException(String.format("Unknown value for action: \"%s\"", action));
      }
    }
    else if (StringUtils.isNotEmpty(controlCode)) {
      // Setup DAO state based on view variant
      if (softwareControlsJourney == SoftwareControlsJourney.SOFTWARE_CATEGORY) {
        controlCodeJourneyHelper.clearControlCodeJourneyDaoFieldsIfChanged(ControlCodeJourney.SOFTWARE_CONTROLS, controlCode);
        permissionsFinderDao.saveSelectedControlCode(ControlCodeJourney.SOFTWARE_CONTROLS, controlCode);
      }
      else if (softwareControlsJourney == SoftwareControlsJourney.SOFTWARE_RELATED_TO_A_PHYSICAL_GOOD) {
        controlCodeJourneyHelper.clearControlCodeJourneyDaoFieldsIfChanged(ControlCodeJourney.SOFTWARE_CONTROLS_RELATED_TO_A_PHYSICAL_GOOD, controlCode);
        permissionsFinderDao.saveSelectedControlCode(ControlCodeJourney.SOFTWARE_CONTROLS_RELATED_TO_A_PHYSICAL_GOOD, controlCode);
      }
      else if (softwareControlsJourney == SoftwareControlsJourney.SOFTWARE_CATCHALL) {
        controlCodeJourneyHelper.clearControlCodeJourneyDaoFieldsIfChanged(ControlCodeJourney.SOFTWARE_CATCHALL_CONTROLS, controlCode);
        permissionsFinderDao.saveSelectedControlCode(ControlCodeJourney.SOFTWARE_CATCHALL_CONTROLS, controlCode);
      }
      else {
        throw new RuntimeException(String.format("Unexpected member of SoftwareControlsJourney enum: \"%s\""
            , softwareControlsJourney.toString()));
      }
      return journeyManager.performTransition(Events.CONTROL_CODE_SELECTED);
    }
    else {
      throw new FormStateException("Unhandled form state");
    }
  }

  public CompletionStage<Result> handleSoftwareCategorySubmit() {
    return handleSubmit(SoftwareControlsJourney.SOFTWARE_CATEGORY);
  }

  public CompletionStage<Result> handleRelatedToPhysicalGoodSubmit() {
    return handleSubmit(SoftwareControlsJourney.SOFTWARE_RELATED_TO_A_PHYSICAL_GOOD);
  }

  public CompletionStage<Result> handleSoftwareCatchallSubmit() {
    return handleSubmit(SoftwareControlsJourney.SOFTWARE_CATCHALL);
  }

  private CompletionStage<Result> renderWithForm(SoftwareControlsJourney softwareControlsJourney, Form<SoftwareControlsForm> form) {
    // Software category is expected at this stage of the journey
    SoftwareCategory softwareCategory = permissionsFinderDao.getSoftwareCategory().get();

    // Count is specific to stubbed CategoryControlsServiceClient
    int count =
        softwareCategory == SoftwareCategory.MILITARY ? 0
            : softwareCategory == SoftwareCategory.AEROSPACE ? 1
            : softwareCategory == SoftwareCategory.COMPUTERS ? 2
            : 0;

    // Setup DAO state based on view variant
    if (softwareControlsJourney == SoftwareControlsJourney.SOFTWARE_CATEGORY) {
      return categoryControlsServiceClient.get(GoodsType.SOFTWARE, softwareCategory) // TODO TECHNOLOGY
          .thenApplyAsync(result -> {
            SoftwareControlsDisplay display = new SoftwareControlsDisplay(softwareControlsJourney, result.controlCodes);
            return ok(softwareControls.render(form, display));
          }, httpExecutionContext.current());
    }
    else if (softwareControlsJourney == SoftwareControlsJourney.SOFTWARE_RELATED_TO_A_PHYSICAL_GOOD) {

      // Note, this is looking at the selected physical good control code which is related to their physical good
      String controlCode = permissionsFinderDao.getSelectedControlCode(ControlCodeJourney.PHYSICAL_GOODS_SEARCH_RELATED_TO_SOFTWARE);

      return relatedControlsServiceClient.get(GoodsType.SOFTWARE, controlCode)  // TODO TECHNOLOGY
          .thenApplyAsync(result -> {
            int size = result.controlCodes.size();
            if (size > 1) {
              /**
               * Expecting more than one control code here. 1 or 0 control codes should not reach this point (and should
               * have prompted a different transition)
               */
              SoftwareControlsDisplay display = new SoftwareControlsDisplay(softwareControlsJourney, result.controlCodes);
              return ok(softwareControls.render(form, display));
            }
            else {
              throw new RuntimeException(String.format("Invalid value for size: \"%d\"", size));
            }
          }, httpExecutionContext.current());
    }
    else if (softwareControlsJourney == SoftwareControlsJourney.SOFTWARE_CATCHALL) {
      return catchallControlsServiceClient.get(softwareCategory, count)
          .thenApplyAsync(result -> {
            int size = result.controlCodes.size();
            if (size > 1) {
              /**
               * Expecting more than one control code here. 1 or 0 control codes should not reach this point (and should
               * have prompted a different transition)
               */
              SoftwareControlsDisplay display = new SoftwareControlsDisplay(softwareControlsJourney, result.controlCodes);
              return ok(softwareControls.render(form, display));
            }
            else {
              throw new RuntimeException(String.format("Invalid value for size: \"%d\"", size));
            }
          }, httpExecutionContext.current());
    }
    else {
      throw new RuntimeException(String.format("Unexpected member of SoftwareControlsJourney enum: \"%s\""
          , softwareControlsJourney.toString()));
    }
  }

  public static class SoftwareControlsForm {

    public String controlCode;

    public String action;

  }

}