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
import utils.common.SelectOption;
import views.html.prototype.prototype7_1;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletionStage;

public class PrototypeController7_1 {

  private final JourneyManager journeyManager;
  private final FormFactory formFactory;

  @Inject
  public PrototypeController7_1(JourneyManager journeyManager, FormFactory formFactory) {
    this.journeyManager = journeyManager;
    this.formFactory = formFactory;
  }

  public CompletionStage<Result> renderForm() {
    PrototypeController7_1Form templateForm = new PrototypeController7_1Form();

    return completedFuture(ok(prototype7_1.render(formFactory.form(PrototypeController7_1Form.class).fill(templateForm),
        new PrototypeDisplay(), getSelectOptions())));
  }

  public CompletionStage<Result> handleSubmit() {

    Form<PrototypeController7_1Form> form = formFactory.form(PrototypeController7_1Form.class).bindFromRequest();
    String equipmentType = form.get().equipmentType;

    return journeyManager.performTransition(StandardEvents.NEXT);
  }

  public static List<SelectOption> getSelectOptions() {

    SelectOption option1 = new SelectOption("physical equipment", "physical equipment");
    SelectOption option2 = new SelectOption("production equipment", "production equipment");
    SelectOption option3 = new SelectOption("training equipment", "training equipment");

    List<SelectOption> list = new ArrayList<>();
    list.add(option1);
    list.add(option2);
    list.add(option3);
    return list;
  }

  public static class PrototypeController7_1Form {

    @Required(message = "Select one option")
    public String equipmentType;

  }

}
