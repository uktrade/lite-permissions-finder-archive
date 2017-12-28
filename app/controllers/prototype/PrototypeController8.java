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

  public CompletionStage<Result> renderForm() {
    PrototypeController8Form templateForm = new PrototypeController8Form();

    return completedFuture(ok(prototype8.render(formFactory.form(PrototypeController8Form.class).fill(templateForm),
        new PrototypeDisplay(), getSelectOptions())));
  }

  public CompletionStage<Result> handleSubmit() {

    Form<PrototypeController8Form> form = formFactory.form(PrototypeController8Form.class).bindFromRequest();

    //todo: save to permissionDao

    return journeyManager.performTransition(StandardEvents.NEXT);
  }

  public static List<SelectOption> getSelectOptions() {

    SelectOption s_option1 = new SelectOption("option1", "aircraft, lighter-than-air vehicles and unmanned aerial vehicles [UAVs]. Aero-engines and aircraft components, equipment and related goods specially designed or modified for military use");
    SelectOption s_option2 = new SelectOption("option2", "ammunition, fuse setting devices and specially designed components ");
    SelectOption s_option3 = new SelectOption("option3", "armoured or protective goods and constructions");
    SelectOption s_option4 = new SelectOption("option4", "bombs, missiles, torpedoes, rockets, other explosive devices and charges and related accessories, equipment and specially designed components");
    SelectOption s_option5 = new SelectOption("option5", "cryogenic and superconductive equipment and specially designed components");
    SelectOption s_option6 = new SelectOption("option6", "directed energy weapon [DEW] systems, specially designed components, countermeasures, equipment and test models");
    SelectOption s_option7 = new SelectOption("option7", "fire control equipment, related alerting and warning equipment, related systems, test and alignment and countermeasure equipment specially designed for military use and specially designed components and accessories");
    SelectOption s_option8 = new SelectOption("option8", "ground vehicles and components");
    SelectOption s_option9 = new SelectOption("option9", "high velocity kinetic energy weapon systems, components and equipment");
    SelectOption s_option10 = new SelectOption("option10", "imaging or countermeasure equipment, accessories and components");
    SelectOption s_option11 = new SelectOption("option11", "naval vessels, special accessories, components and equipment");
    SelectOption s_option12 = new SelectOption("option12", "security and para-military police goods ");
    SelectOption s_option13 = new SelectOption("option13", "smooth-bore weapons [calibre less than 20mm] / Other weapons [calibre 12.7mm or less] - accessories and specially designed components");
    SelectOption s_option14 = new SelectOption("option14", "smooth-bore weapons [calibre 20mm or more] / Other armament or weapons [calibre greater than 12.7mm] - accessories, projectors and specially designed components");

    List<SelectOption> list = new ArrayList<>();
    list.add(s_option1);
    list.add(s_option2);
    list.add(s_option3);
    list.add(s_option4);
    list.add(s_option5);
    list.add(s_option6);
    list.add(s_option7);
    list.add(s_option8);
    list.add(s_option9);
    list.add(s_option10);
    list.add(s_option11);
    list.add(s_option12);
    list.add(s_option13);
    list.add(s_option14);
    return list;
  }

  public static class PrototypeController8Form {

    @Required(message = "Select one option")
    public Boolean physicalEquipment;

  }

}
