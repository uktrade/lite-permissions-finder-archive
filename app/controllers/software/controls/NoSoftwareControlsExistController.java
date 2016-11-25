package controllers.software.controls;

import static play.mvc.Results.ok;

import com.google.inject.Inject;
import components.common.journey.JourneyManager;
import components.persistence.PermissionsFinderDao;
import exceptions.FormStateException;
import journey.helpers.SoftwareJourneyHelper;
import models.software.ApplicableSoftwareControls;
import models.software.SoftwareCategory;
import play.data.Form;
import play.data.FormFactory;
import play.data.validation.Constraints.Required;
import play.libs.concurrent.HttpExecutionContext;
import play.mvc.Result;
import views.html.software.controls.noSoftwareControlsExist;

import java.util.concurrent.CompletionStage;

public class NoSoftwareControlsExistController {
  private final FormFactory formFactory;
  private final PermissionsFinderDao permissionsFinderDao;
  private final SoftwareJourneyHelper softwareJourneyHelper;
  private final HttpExecutionContext httpExecutionContext;

  @Inject
  public NoSoftwareControlsExistController(FormFactory formFactory,
                                           PermissionsFinderDao permissionsFinderDao,
                                           SoftwareJourneyHelper softwareJourneyHelper,
                                           HttpExecutionContext httpExecutionContext) {
    this.formFactory = formFactory;
    this.permissionsFinderDao = permissionsFinderDao;
    this.softwareJourneyHelper = softwareJourneyHelper;
    this.httpExecutionContext = httpExecutionContext;
  }

  public CompletionStage<Result> renderForm() {
    return renderWithForm(formFactory.form(NoSoftwareControlsExistForm.class));
  }

  public CompletionStage<Result> handleSubmit() {
    Form<NoSoftwareControlsExistForm> form = formFactory.form(NoSoftwareControlsExistForm.class).bindFromRequest();
    if (form.hasErrors()) {
      return renderWithForm(form);
    }
    String action = form.get().action;
    if ("continue".equals(action)) {
      return softwareJourneyHelper.performCatchallSoftwareControlsTransition();
    }
    else {
      throw new FormStateException(String.format("Unknown value for action: \"%s\"", action));
    }
  }

  private CompletionStage<Result> renderWithForm(Form<NoSoftwareControlsExistForm> form) {
    SoftwareCategory softwareCategory = permissionsFinderDao.getSoftwareCategory().get();
    return softwareJourneyHelper.checkCatchtallSoftwareControls(softwareCategory, false)
        .thenApplyAsync(control -> {
          if (control == ApplicableSoftwareControls.ONE || control == ApplicableSoftwareControls.GREATER_THAN_ONE) {
            return ok(noSoftwareControlsExist.render(form));
          }
          else {
            throw new RuntimeException(String.format("Unexpected member of ApplicableSoftwareControls enum: \"%s\""
                , control.toString()));
          }
        }, httpExecutionContext.current());
  }

  public static class NoSoftwareControlsExistForm {
    @Required(message = "This is required")
    public String action;
  }

}