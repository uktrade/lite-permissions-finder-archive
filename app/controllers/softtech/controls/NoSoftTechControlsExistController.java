package controllers.softtech.controls;

import static play.mvc.Results.ok;

import com.google.inject.Inject;
import components.persistence.PermissionsFinderDao;
import exceptions.FormStateException;
import journey.helpers.SoftTechJourneyHelper;
import models.GoodsType;
import models.softtech.ApplicableSoftTechControls;
import models.softtech.SoftTechCategory;
import play.data.Form;
import play.data.FormFactory;
import play.data.validation.Constraints.Required;
import play.libs.concurrent.HttpExecutionContext;
import play.mvc.Result;
import views.html.softtech.controls.noSoftTechControlsExist;

import java.util.concurrent.CompletionStage;

public class NoSoftTechControlsExistController {
  private final FormFactory formFactory;
  private final PermissionsFinderDao permissionsFinderDao;
  private final SoftTechJourneyHelper softTechJourneyHelper;
  private final HttpExecutionContext httpExecutionContext;

  @Inject
  public NoSoftTechControlsExistController(FormFactory formFactory,
                                           PermissionsFinderDao permissionsFinderDao,
                                           SoftTechJourneyHelper softTechJourneyHelper,
                                           HttpExecutionContext httpExecutionContext) {
    this.formFactory = formFactory;
    this.permissionsFinderDao = permissionsFinderDao;
    this.softTechJourneyHelper = softTechJourneyHelper;
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
      return softTechJourneyHelper.performCatchallSoftwareControlsTransition();
    }
    else {
      throw new FormStateException(String.format("Unknown value for action: \"%s\"", action));
    }
  }

  private CompletionStage<Result> renderWithForm(Form<NoSoftwareControlsExistForm> form) {
    SoftTechCategory softTechCategory = permissionsFinderDao.getSoftTechCategory(GoodsType.SOFTWARE).get();
    return softTechJourneyHelper.checkCatchtallSoftwareControls(softTechCategory, false)
        .thenApplyAsync(control -> {
          if (control == ApplicableSoftTechControls.ONE || control == ApplicableSoftTechControls.GREATER_THAN_ONE) {
            return ok(noSoftTechControlsExist.render(form));
          }
          else {
            throw new RuntimeException(String.format("Unexpected member of ApplicableSoftTechControls enum: \"%s\""
                , control.toString()));
          }
        }, httpExecutionContext.current());
  }

  public static class NoSoftwareControlsExistForm {
    @Required(message = "This is required")
    public String action;
  }

}