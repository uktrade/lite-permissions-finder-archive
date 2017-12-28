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
import views.html.prototype.prototype1;
import views.html.prototype.prototype7;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletionStage;

public class PrototypeController7 {

  private final JourneyManager journeyManager;
  private final FormFactory formFactory;

  @Inject
  public PrototypeController7(JourneyManager journeyManager, FormFactory formFactory) {
    this.journeyManager = journeyManager;
    this.formFactory = formFactory;
  }

  public CompletionStage<Result> renderForm() {
    PrototypeController7Form templateForm = new PrototypeController7Form();

    return completedFuture(ok(prototype7.render(formFactory.form(PrototypeController7Form.class).fill(templateForm),
        new PrototypeDisplay(), getSelectOptions())));
  }

  public CompletionStage<Result> handleSubmit() {

    Form<PrototypeController7Form> form = formFactory.form(PrototypeController7Form.class).bindFromRequest();

    //todo: save to permissionDao

    return journeyManager.performTransition(StandardEvents.NEXT);
  }

  public static List<SelectOption> getSelectOptions() {

    SelectOption s_option1 = new SelectOption("option1", "physical equipment");
    SelectOption s_option2 = new SelectOption("option2", "production equipment");
    SelectOption s_option3 = new SelectOption("option3", "training equipment");

    List<SelectOption> list = new ArrayList<>();
    list.add(s_option1);
    list.add(s_option2);
    list.add(s_option3);
    return list;
  }

  public static class PrototypeController7Form {

    @Required(message = "Select one option")
    public Boolean equipmentType;

  }

}
