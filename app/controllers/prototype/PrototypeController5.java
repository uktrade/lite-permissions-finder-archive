package controllers.prototype;

import static java.util.concurrent.CompletableFuture.completedFuture;
import static play.mvc.Results.ok;

import com.google.inject.Inject;
import components.common.journey.JourneyManager;
import components.common.journey.StandardEvents;
import play.data.Form;
import play.data.FormFactory;
import play.data.validation.Constraints.Required;
import play.mvc.Result;
import views.html.prototype.prototype5;

import java.util.concurrent.CompletionStage;

public class PrototypeController5 {

  private final JourneyManager journeyManager;
  private final FormFactory formFactory;

  @Inject
  public PrototypeController5(JourneyManager journeyManager, FormFactory formFactory) {
    this.journeyManager = journeyManager;
    this.formFactory = formFactory;
  }

  public CompletionStage<Result> renderForm() {
    PrototypeController5Form templateForm = new PrototypeController5Form();

    return completedFuture(ok(prototype5.render(formFactory.form(PrototypeController5Form.class).fill(templateForm),
        new PrototypeDisplay())));
  }

  public CompletionStage<Result> handleSubmit() {
    Form<PrototypeController5Form> form = formFactory.form(PrototypeController5Form.class).bindFromRequest();
    Boolean isSpecialMilitary = form.get().specialMilitary;

    return journeyManager.performTransition(StandardEvents.YES);
  }

  public static class PrototypeController5Form {

    @Required(message = "Select one option")
    public Boolean specialMilitary;

  }

}
