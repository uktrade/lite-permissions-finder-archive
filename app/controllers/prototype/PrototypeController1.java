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
import views.html.prototype.prototype1;

import java.util.concurrent.CompletionStage;

public class PrototypeController1 {

  private final JourneyManager journeyManager;
  private final FormFactory formFactory;

  @Inject
  public PrototypeController1(JourneyManager journeyManager, FormFactory formFactory) {
    this.journeyManager = journeyManager;
    this.formFactory = formFactory;
  }

  public CompletionStage<Result> renderForm() {
    PrototypeController1Form templateForm = new PrototypeController1Form();

    return completedFuture(ok(prototype1.render(formFactory.form(PrototypeController1Form.class).fill(templateForm),
        new PrototypeDisplay())));
  }

  public CompletionStage<Result> handleSubmit() {
    Form<PrototypeController1Form> form = formFactory.form(PrototypeController1Form.class).bindFromRequest();
    Boolean isExportedBefore = form.get().exportedBefore;

    if(isExportedBefore) {
      return journeyManager.performTransition(StandardEvents.YES);
    } else {
      return journeyManager.performTransition(StandardEvents.NO);
    }
  }

  public static class PrototypeController1Form {

    @Required(message = "Select one option")
    public Boolean exportedBefore;

  }

}
