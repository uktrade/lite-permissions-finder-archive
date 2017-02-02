package controllers.softtech;

import static java.util.concurrent.CompletableFuture.completedFuture;
import static play.mvc.Results.ok;

import com.google.inject.Inject;
import components.common.journey.JourneyManager;
import components.common.journey.StandardEvents;
import exceptions.FormStateException;
import play.data.Form;
import play.data.FormFactory;
import play.mvc.Result;
import views.html.softtech.technologyPublicDomainExemptionNLR;

import java.util.concurrent.CompletionStage;

public class TechnologyPublicDomainExemptionNLRController {
  private final JourneyManager journeyManager;
  private final FormFactory formFactory;

  @Inject
  public TechnologyPublicDomainExemptionNLRController(JourneyManager journeyManager, FormFactory formFactory) {
    this.journeyManager = journeyManager;
    this.formFactory = formFactory;
  }

  public Result renderForm() {
    return ok(technologyPublicDomainExemptionNLR.render(formFactory.form(TechnologyPublicDomainExemptionNLRForm.class)));
  }

  public CompletionStage<Result> handleSubmit() {
    Form<TechnologyPublicDomainExemptionNLRForm> form = formFactory.form(TechnologyPublicDomainExemptionNLRForm.class).bindFromRequest();
    if (form.hasErrors()) {
      completedFuture(ok(technologyPublicDomainExemptionNLR.render(form)));
    }
    String action = form.get().action;
    if ("continue".equals(action)) {
      return journeyManager.performTransition(StandardEvents.NEXT);
    }
    else {
      throw new FormStateException(String.format("Unknown value for action: \"%s\"", action));
    }
  }

  public static class TechnologyPublicDomainExemptionNLRForm {
    public String action;
  }

}