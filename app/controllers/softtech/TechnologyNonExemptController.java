package controllers.softtech;

import static java.util.concurrent.CompletableFuture.completedFuture;
import static play.mvc.Results.ok;

import com.google.inject.Inject;
import components.common.journey.JourneyManager;
import components.common.journey.StandardEvents;
import components.persistence.PermissionsFinderDao;
import models.softtech.TechnologyNonExemptDisplay;
import play.data.Form;
import play.data.FormFactory;
import play.data.validation.Constraints.Required;
import play.mvc.Result;
import views.html.softtech.technologyNonExempt;

import java.util.concurrent.CompletionStage;

public class TechnologyNonExemptController {
  private final JourneyManager journeyManager;
  private final FormFactory formFactory;
  private final PermissionsFinderDao permissionsFinderDao;

  @Inject
  public TechnologyNonExemptController(JourneyManager journeyManager, FormFactory formFactory,
                                       PermissionsFinderDao permissionsFinderDao) {
    this.journeyManager = journeyManager;
    this.formFactory = formFactory;
    this.permissionsFinderDao = permissionsFinderDao;
  }

  public Result renderForm() {
    TechnologyNonExemptForm templateForm = new TechnologyNonExemptForm();
    templateForm.isNonExempt = permissionsFinderDao.getTechnologyIsNonExempt().orElse(null);
    return ok(technologyNonExempt.render(formFactory.form(TechnologyNonExemptForm.class).fill(templateForm), new TechnologyNonExemptDisplay()));
  }

  public CompletionStage<Result> handleSubmit() {
    Form<TechnologyNonExemptForm> form = formFactory.form(TechnologyNonExemptForm.class).bindFromRequest();
    if (form.hasErrors()) {
      completedFuture(ok(technologyNonExempt.render(form, new TechnologyNonExemptDisplay())));
    }
    Boolean isNonExempt = form.get().isNonExempt;
    permissionsFinderDao.saveTechnologyIsNonExempt(isNonExempt);
    if(isNonExempt) {
      return journeyManager.performTransition(StandardEvents.YES);
    }
    else {
      return journeyManager.performTransition(StandardEvents.NO);
    }
  }

  public static class TechnologyNonExemptForm {
    @Required(message = "You must answer this question")
    public Boolean isNonExempt;
  }

}