package controllers.prototype;

import static java.util.concurrent.CompletableFuture.completedFuture;
import static play.mvc.Results.ok;
import static play.mvc.Results.redirect;

import com.google.inject.Inject;
import components.common.journey.JourneyManager;
import controllers.prototype.enums.PrototypeChemicalType;
import controllers.prototype.enums.PrototypeEquipment;
import journey.Events;
import play.data.Form;
import play.data.FormFactory;
import play.data.validation.Constraints.Required;
import play.mvc.Result;
import utils.common.SelectOption;
import views.html.prototype.triageQuestion;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.CompletionStage;
import java.util.stream.Collectors;

public class TriageController {

  private final JourneyManager journeyManager;
  private final FormFactory formFactory;

  private final Map<String, TriageDisplay> displayMap;

  @Inject
  public TriageController(JourneyManager journeyManager, FormFactory formFactory) {
    this.journeyManager = journeyManager;
    this.formFactory = formFactory;

    this.displayMap = new HashMap<>();

    displayMap.put("INIT_1", new TriageDisplay("Have you exported controlled goods and services before?",
        Arrays.asList(new SelectOption("true", "Yes"), new SelectOption("false", "No"))));

    displayMap.put("INIT_2", new TriageDisplay("Have you had goods or services rated before?",
        Arrays.asList(new SelectOption("true", "Yes"), new SelectOption("false", "No"))));

    displayMap.put("INIT_3", new TriageDisplay("Are you registering the same goods and services now?",
        Arrays.asList(new SelectOption("true", "Yes"), new SelectOption("false", "No"))));

    displayMap.put("INIT_4", new TriageDisplay("What are you trying to do?", Arrays.asList(
        new SelectOption("more_export", "Find out more about export control and licensing for restricted goods"),
        new SelectOption("need_licence", "Find out if I need a licence to export goods and services"),
        new SelectOption("get_rating", "Get a control rating for some goods and services and apply for a licence"),
        new SelectOption("check_rating", "Find out if I need a licence to export goods and services")
        )));

    displayMap.put("MIL_1", new TriageDisplay("Has your export been specially designed or modified for military use?",
        Arrays.asList(new SelectOption("true", "Yes"), new SelectOption("false", "No"))));

    displayMap.put("MIL_2", new TriageDisplay("What are you exporting?", Arrays.asList(
        new SelectOption("equipment", "Equipment"),
        new SelectOption("materials", "Materials and substances"),
        new SelectOption("software", "Software"),
        new SelectOption("technology", "Technology")
    )));

    displayMap.put("MIL_3", new TriageDisplay("What type of equipment?", Arrays.asList(
        new SelectOption("physical", "Physical equipment"),
        new SelectOption("production", "Production equipment"),
        new SelectOption("training", "Training equipment")
    )));

    displayMap.put("MIL_4", new TriageDisplay("What type of materials?", Arrays.asList(
        new SelectOption("chemicals", "Chemical or biological toxic agents, toxic chemicals and mixtures, riot control agents, radioactive materials and related components, equipment and materials"),
        new SelectOption("forgings", "Forgings, castings and unfinished goods"),
        new SelectOption("energy", "Energetic materials and related substances")
    )));


    displayMap.put("MIL_EQUIP", new TriageDisplay("What type of physical equipment?",
        Arrays.stream(PrototypeEquipment.values()).map(e -> new SelectOption(e.toString(), e.value())).collect(Collectors.toList())));

    displayMap.put("MIL_CHEM", new TriageDisplay("What type of chemicals?",
        Arrays.stream(PrototypeChemicalType.values()).map(e -> new SelectOption(e.toString(), e.value())).collect(Collectors.toList())));
  }

  public CompletionStage<Result> renderForm(String page) {
    TriageForm templateForm = new TriageForm();

    TriageDisplay display = displayMap.getOrDefault(page, new TriageDisplay("Journey not implemented", Collections.emptyList()));

    return completedFuture(ok(triageQuestion.render(formFactory.form(TriageForm.class).fill(templateForm),
        display)));
  }

  public CompletionStage<Result> handleSubmit() {
    Form<TriageForm> form = formFactory.form(TriageForm.class).bindFromRequest();
    String answer = form.get().answer;

    String curStage = journeyManager.getCurrentInternalStageName();
    if (curStage.equals("milTriagePhysicalEquipment") || curStage.equals("milTriageChemicals")) {
      return completedFuture(redirect(routes.PrototypeControlCodeController.renderForm(answer)));
    }

    return journeyManager.performTransition(Events.TRIAGE_NEXT, answer);
  }

  public static class TriageForm {

    @Required(message = "Select one option")
    public String answer;

  }

}
