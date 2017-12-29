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
import views.html.prototype.prototype6;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletionStage;

public class PrototypeController6 {

  private final JourneyManager journeyManager;
  private final FormFactory formFactory;

  @Inject
  public PrototypeController6(JourneyManager journeyManager, FormFactory formFactory) {
    this.journeyManager = journeyManager;
    this.formFactory = formFactory;
  }

  public CompletionStage<Result> renderForm() {
    PrototypeController6Form templateForm = new PrototypeController6Form();

    return completedFuture(ok(prototype6.render(formFactory.form(PrototypeController6Form.class).fill(templateForm),
        new PrototypeDisplay(), getSelectOptions())));
  }

  public CompletionStage<Result> handleSubmit() {

    Form<PrototypeController6Form> form = formFactory.form(PrototypeController6Form.class).bindFromRequest();
    String exportItem = form.get().exportItem;

    return journeyManager.performTransition(StandardEvents.NEXT);
  }

  public static List<SelectOption> getSelectOptions() {
    SelectOption option1 = new SelectOption("option1", "equipment");
    SelectOption option2 = new SelectOption("option2", "materials and substances");
    SelectOption option3 = new SelectOption("option3", "software");
    SelectOption option4 = new SelectOption("option4", "technology");

    List<SelectOption> list = new ArrayList<>();
    list.add(option1);
    list.add(option2);
    list.add(option3);
    list.add(option4);
    return list;
  }

  public static class PrototypeController6Form {

    @Required(message = "Select one option")
    public String exportItem;

  }

}
