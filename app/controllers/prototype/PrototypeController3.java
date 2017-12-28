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
import views.html.prototype.prototype3;

import java.util.concurrent.CompletionStage;

public class PrototypeController3 {

  private final JourneyManager journeyManager;
  private final FormFactory formFactory;

  @Inject
  public PrototypeController3(JourneyManager journeyManager, FormFactory formFactory) {
    this.journeyManager = journeyManager;
    this.formFactory = formFactory;
  }

  public CompletionStage<Result> renderForm() {
    PrototypeController3Form templateForm = new PrototypeController3Form();

    return completedFuture(ok(prototype3.render(formFactory.form(PrototypeController3Form.class).fill(templateForm),
        new PrototypeDisplay())));
  }

  public CompletionStage<Result> handleSubmit() {

    Form<PrototypeController3Form> form = formFactory.form(PrototypeController3Form.class).bindFromRequest();

    Boolean isSameGoods = form.get().sameGoods;

    return journeyManager.performTransition(StandardEvents.NEXT);
  }

  public static class PrototypeController3Form {

    @Required(message = "Select one option")
    public Boolean sameGoods;

  }

}
