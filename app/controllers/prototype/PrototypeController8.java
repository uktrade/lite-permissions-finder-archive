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
import components.persistence.PermissionsFinderDao;
import controllers.prototype.enums.PrototypeEquipment;
import journey.Events;
import play.data.Form;
import play.data.FormFactory;
import play.data.validation.Constraints.Required;
import play.mvc.Result;
import utils.common.SelectOption;
import views.html.prototype.prototype8;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletionStage;

public class PrototypeController8 {

  private final JourneyManager journeyManager;
  private final FormFactory formFactory;

  @Inject
  public PrototypeController8(JourneyManager journeyManager, FormFactory formFactory) {
    this.journeyManager = journeyManager;
    this.formFactory = formFactory;
  }

  public static List<SelectOption> getSelectOptions() {

    SelectOption option1 = new SelectOption("option1", ML10.value());
    SelectOption option2 = new SelectOption("option2", ML3.value());
    SelectOption option3 = new SelectOption("option3", ML13.value());
    SelectOption option4 = new SelectOption("option4", ML4.value());
    SelectOption option5 = new SelectOption("option5", ML20.value());
    SelectOption option6 = new SelectOption("option6", ML19.value());
    SelectOption option7 = new SelectOption("option7", ML5.value());
    SelectOption option8 = new SelectOption("option8", ML6.value());
    SelectOption option9 = new SelectOption("option9", ML12.value());
    SelectOption option10 = new SelectOption("option10", ML15.value());
    SelectOption option11 = new SelectOption("option11", ML9.value());
    SelectOption option12 = new SelectOption("option12", PL5001.value());
    SelectOption option13 = new SelectOption("option13", ML1.value());
    SelectOption option14 = new SelectOption("option14", ML2.value());

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

    return completedFuture(ok(prototype8.render(formFactory.form(PrototypeController8Form.class).fill(templateForm),
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
