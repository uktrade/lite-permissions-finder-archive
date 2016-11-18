package controllers.software.controls;

import static play.mvc.Results.ok;

import com.google.inject.Inject;
import components.common.journey.JourneyManager;
import components.persistence.PermissionsFinderDao;
import components.services.controlcode.controls.category.CategoryControlsServiceClient;
import exceptions.FormStateException;
import journey.Events;
import journey.helpers.ControlCodeJourneyHelper;
import models.controlcode.ControlCodeJourney;
import models.software.SoftwareCategory;
import models.software.controls.ControlsBaseDisplay;
import org.apache.commons.lang3.StringUtils;
import play.data.Form;
import play.data.FormFactory;
import play.libs.concurrent.HttpExecutionContext;
import play.mvc.Result;
import views.html.software.controls.categoryControls;

import java.util.concurrent.CompletionStage;

public class CategoryControlsController {
  private final JourneyManager journeyManager;
  private final FormFactory formFactory;
  private final PermissionsFinderDao permissionsFinderDao;
  private final CategoryControlsServiceClient categoryControlsServiceClient;
  private final HttpExecutionContext httpExecutionContext;
  private final ControlCodeJourneyHelper controlCodeJourneyHelper;

  @Inject
  public CategoryControlsController(JourneyManager journeyManager,
                                    FormFactory formFactory,
                                    PermissionsFinderDao permissionsFinderDao,
                                    CategoryControlsServiceClient categoryControlsServiceClient,
                                    HttpExecutionContext httpExecutionContext,
                                    ControlCodeJourneyHelper controlCodeJourneyHelper) {
    this.journeyManager = journeyManager;
    this.formFactory = formFactory;
    this.permissionsFinderDao = permissionsFinderDao;
    this.categoryControlsServiceClient = categoryControlsServiceClient;
    this.httpExecutionContext = httpExecutionContext;
    this.controlCodeJourneyHelper = controlCodeJourneyHelper;
  }

  public CompletionStage<Result> renderForm() {
    return renderWithForm(formFactory.form(ControlsBaseForm.class));
  }

  public CompletionStage<Result> handleSubmit() {
    Form<ControlsBaseForm> form = formFactory.form(ControlsBaseForm.class).bindFromRequest();
    if (form.hasErrors()) {
      renderWithForm(form);
    }
    String action = form.get().action;
    String controlCode = form.get().controlCode;
    if (StringUtils.isNotEmpty(action)) {
      if ("noMatchedControlCode".equals(action)) {
        return journeyManager.performTransition(Events.NONE_MATCHED);
      }
      else {
        throw new FormStateException(String.format("Unknown value for action: \"%s\"", action));
      }
    }
    else if (StringUtils.isNotEmpty(controlCode)) {
      controlCodeJourneyHelper.clearControlCodeJourneyDaoFieldsIfChanged(ControlCodeJourney.SOFTWARE_CONTROLS, controlCode);
      permissionsFinderDao.saveSelectedControlCode(ControlCodeJourney.SOFTWARE_CONTROLS, controlCode);
      return journeyManager.performTransition(Events.CONTROL_CODE_SELECTED);
    }
    else {
      throw new FormStateException("Unhandled form state");
    }
  }

  public CompletionStage<Result> renderWithForm(Form<ControlsBaseForm> form) {
    // Software category is expected at this stage of the journey
    SoftwareCategory softwareCategory = permissionsFinderDao.getSoftwareCategory().get();

    // Count is specific to stubbed CategoryControlsServiceClient
    int count =
        softwareCategory == SoftwareCategory.MILITARY ? 0
            : softwareCategory == SoftwareCategory.DUMMY ? 1
            : softwareCategory == SoftwareCategory.RADIOACTIVE ? 2
            : 0;

    return categoryControlsServiceClient.get(softwareCategory, count)
        .thenApplyAsync(result -> {
          ControlsBaseDisplay display = new ControlsBaseDisplay(result.controlCodes);
          return ok(categoryControls.render(form, display));
        }, httpExecutionContext.current());
  }

}