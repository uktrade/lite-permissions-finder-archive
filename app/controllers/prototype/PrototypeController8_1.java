package controllers.prototype;

import static controllers.prototype.enums.PrototypeEquipment.ML1;
import static controllers.prototype.enums.PrototypeEquipment.ML10;
import static controllers.prototype.enums.PrototypeEquipment.ML12;
import static controllers.prototype.enums.PrototypeEquipment.ML13;
import static controllers.prototype.enums.PrototypeEquipment.ML15;
import static controllers.prototype.enums.PrototypeEquipment.ML19;
import static controllers.prototype.enums.PrototypeEquipment.ML2;
import static controllers.prototype.enums.PrototypeEquipment.ML20;
import static controllers.prototype.enums.PrototypeEquipment.ML3;
import static controllers.prototype.enums.PrototypeEquipment.ML4;
import static controllers.prototype.enums.PrototypeEquipment.ML5;
import static controllers.prototype.enums.PrototypeEquipment.ML6;
import static controllers.prototype.enums.PrototypeEquipment.ML9;
import static controllers.prototype.enums.PrototypeEquipment.PL5001;
import static java.util.concurrent.CompletableFuture.completedFuture;
import static play.mvc.Results.ok;

import com.google.inject.Inject;
import components.common.journey.JourneyManager;
import controllers.prototype.enums.PrototypeEquipment;
import journey.Events;
import play.data.Form;
import play.data.FormFactory;
import play.data.validation.Constraints.Required;
import play.mvc.Result;
import utils.common.SelectOption;
import views.html.prototype.prototype8_1;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletionStage;

public class PrototypeController8_1 {

  private final JourneyManager journeyManager;
  private final FormFactory formFactory;

  @Inject
  public PrototypeController8_1(JourneyManager journeyManager, FormFactory formFactory) {
    this.journeyManager = journeyManager;
    this.formFactory = formFactory;
  }

  public static List<SelectOption> getSelectOptions() {

    SelectOption option1 = new SelectOption(ML10.value(), ML10.value());
    SelectOption option2 = new SelectOption(ML3.value(), ML3.value());
    SelectOption option3 = new SelectOption(ML13.value(), ML13.value());
    SelectOption option4 = new SelectOption(ML4.value(), ML4.value());
    SelectOption option5 = new SelectOption(ML20.value(), ML20.value());
    SelectOption option6 = new SelectOption(ML19.value(), ML19.value());
    SelectOption option7 = new SelectOption(ML5.value(), ML5.value());
    SelectOption option8 = new SelectOption(ML6.value(), ML6.value());
    SelectOption option9 = new SelectOption(ML12.value(), ML12.value());
    SelectOption option10 = new SelectOption(ML15.value(), ML15.value());
    SelectOption option11 = new SelectOption( ML9.value(), ML9.value());
    SelectOption option12 = new SelectOption(PL5001.value(), PL5001.value());
    SelectOption option13 = new SelectOption(ML1.value(), ML1.value());
    SelectOption option14 = new SelectOption(ML2.value(), ML2.value());

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
    list.add(option10);
    list.add(option11);
    list.add(option12);
    list.add(option13);
    list.add(option14);
    return list;
  }

  public CompletionStage<Result> renderForm() {
    PrototypeController8Form templateForm = new PrototypeController8Form();

    return completedFuture(ok(prototype8_1.render(formFactory.form(PrototypeController8Form.class).fill(templateForm),
        new PrototypeDisplay(), getSelectOptions())));
  }

  public CompletionStage<Result> handleSubmit() {
    Form<PrototypeController8Form> form = formFactory.form(PrototypeController8Form.class).bindFromRequest();
    String physicalEquipment = form.get().physicalEquipment;

    return journeyManager.performTransition(Events.PROTOTYPE_EQUIPMENT_SELECTED, PrototypeEquipment.get(physicalEquipment));
  }

  public static class PrototypeController8Form {

    @Required(message = "Select one option")
    public String physicalEquipment;

  }

}
