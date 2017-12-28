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
import views.html.prototype.prototype2;

import java.util.concurrent.CompletionStage;

public class PrototypeController2 {

  private final JourneyManager journeyManager;
  private final FormFactory formFactory;

  @Inject
  public PrototypeController2(JourneyManager journeyManager, FormFactory formFactory) {
    this.journeyManager = journeyManager;
    this.formFactory = formFactory;
  }

  public CompletionStage<Result> renderForm() {
    PrototypeController2Form templateForm = new PrototypeController2Form();

    return completedFuture(ok(prototype2.render(formFactory.form(PrototypeController2Form.class).fill(templateForm),
        new PrototypeDisplay())));
  }

  public CompletionStage<Result> handleSubmit() {

    Form<PrototypeController2Form> form = formFactory.form(PrototypeController2Form.class).bindFromRequest();

    Boolean isRatedBefore = form.get().ratedBefore;

    return journeyManager.performTransition(StandardEvents.NEXT);
  }

  public static class PrototypeController2Form {

    @Required(message = "Select one option")
    public Boolean ratedBefore;

  }

}
