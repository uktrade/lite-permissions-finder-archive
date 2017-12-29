package controllers.prototype;

import static controllers.prototype.enums.PrototypeChemicalType.ML7a;
import static controllers.prototype.enums.PrototypeChemicalType.ML7b;
import static controllers.prototype.enums.PrototypeChemicalType.ML7c;
import static controllers.prototype.enums.PrototypeChemicalType.ML7d;
import static controllers.prototype.enums.PrototypeChemicalType.ML7e;
import static controllers.prototype.enums.PrototypeChemicalType.ML7f;
import static controllers.prototype.enums.PrototypeChemicalType.ML7g;
import static controllers.prototype.enums.PrototypeChemicalType.ML7h;
import static controllers.prototype.enums.PrototypeChemicalType.ML7i;
import static java.util.concurrent.CompletableFuture.completedFuture;
import static play.mvc.Results.ok;

import com.google.inject.Inject;
import components.common.journey.JourneyManager;
import controllers.prototype.enums.PrototypeChemicalType;
import journey.Events;
import play.data.Form;
import play.data.FormFactory;
import play.data.validation.Constraints.Required;
import play.mvc.Result;
import utils.common.SelectOption;
import views.html.prototype.prototype8_2;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletionStage;

public class PrototypeController8_2 {

  private final JourneyManager journeyManager;
  private final FormFactory formFactory;

  @Inject
  public PrototypeController8_2(JourneyManager journeyManager, FormFactory formFactory) {
    this.journeyManager = journeyManager;
    this.formFactory = formFactory;
  }

  public static List<SelectOption> getSelectOptions() {

    SelectOption option1 = new SelectOption(ML7a.value(), ML7a.value());
    SelectOption option2 = new SelectOption(ML7b.value(), ML7b.value());
    SelectOption option3 = new SelectOption(ML7c.value(), ML7c.value());
    SelectOption option4 = new SelectOption(ML7d.value(), ML7d.value());
    SelectOption option5 = new SelectOption(ML7e.value(), ML7e.value());
    SelectOption option6 = new SelectOption(ML7f.value(), ML7f.value());
    SelectOption option7 = new SelectOption(ML7g.value(), ML7g.value());
    SelectOption option8 = new SelectOption(ML7h.value(), ML7h.value());
    SelectOption option9 = new SelectOption(ML7i.value(), ML7i.value());

    List<SelectOption> list = new ArrayList<>();
    list.add(option1);
    list.add(option2);
    list.add(option3);
    list.add(option4);
    list.add(option5);
    list.add(option6);
    list.add(option7);
    list.add(option8);
    list.add(option9);
    return list;
  }

  public CompletionStage<Result> renderForm() {
    PrototypeController8_2Form templateForm = new PrototypeController8_2Form();

    return completedFuture(ok(prototype8_2.render(formFactory.form(PrototypeController8_2Form.class).fill(templateForm),
        new PrototypeDisplay(), getSelectOptions())));
  }

  public CompletionStage<Result> handleSubmit() {
    Form<PrototypeController8_2Form> form = formFactory.form(PrototypeController8_2Form.class).bindFromRequest();
    String chemicalType = form.get().chemicalType;

    return journeyManager.performTransition(Events.PROTOTYPE_CHEMICAL_SELECTED, PrototypeChemicalType.get(chemicalType));
  }

  public static class PrototypeController8_2Form {

    @Required(message = "Select one option")
    public String chemicalType;

  }

}
