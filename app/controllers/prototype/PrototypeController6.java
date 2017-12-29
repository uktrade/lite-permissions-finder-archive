package controllers.prototype;

import static java.util.concurrent.CompletableFuture.completedFuture;
import static play.mvc.Results.ok;

import com.google.inject.Inject;
import components.common.journey.JourneyManager;
import controllers.prototype.enums.PrototypeMilitaryItems;
import journey.Events;
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

  public static List<SelectOption> getSelectOptions() {
    SelectOption option1 = new SelectOption("equipment", "equipment");
    SelectOption option2 = new SelectOption("materials and substances", "materials and substances");
    SelectOption option3 = new SelectOption("software", "software");
    SelectOption option4 = new SelectOption("technology", "technology");

    List<SelectOption> list = new ArrayList<>();
    list.add(option1);
    list.add(option2);
    list.add(option3);
    list.add(option4);
    return list;
  }

  public CompletionStage<Result> renderForm() {
    PrototypeController6Form templateForm = new PrototypeController6Form();

    return completedFuture(ok(prototype6.render(formFactory.form(PrototypeController6Form.class).fill(templateForm),
        new PrototypeDisplay(), getSelectOptions())));
  }

  public CompletionStage<Result> handleSubmit() {

    Form<PrototypeController6Form> form = formFactory.form(PrototypeController6Form.class).bindFromRequest();
    String exportItem = form.get().exportItem;

    return journeyManager.performTransition(Events.PROTOTYPE_MILTARY_SELECTED, PrototypeMilitaryItems.get(exportItem));
  }

  public static class PrototypeController6Form {

    @Required(message = "Select one option")
    public String exportItem;

  }

}
