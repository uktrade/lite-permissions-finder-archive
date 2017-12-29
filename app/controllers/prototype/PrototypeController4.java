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
import views.html.prototype.prototype4;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletionStage;

public class PrototypeController4 {

  private final JourneyManager journeyManager;
  private final FormFactory formFactory;

  @Inject
  public PrototypeController4(JourneyManager journeyManager, FormFactory formFactory) {
    this.journeyManager = journeyManager;
    this.formFactory = formFactory;
  }

  public CompletionStage<Result> renderForm() {
    PrototypeController4Form templateForm = new PrototypeController4Form();

    return completedFuture(ok(prototype4.render(formFactory.form(PrototypeController4Form.class).fill(templateForm),
        new PrototypeDisplay(), getSelectOptions())));
  }

  public CompletionStage<Result> handleSubmit() {

    Form<PrototypeController4Form> form = formFactory.form(PrototypeController4Form.class).bindFromRequest();
    String exportOption = form.get().exportOptions;

    return journeyManager.performTransition(StandardEvents.NEXT);
  }

  public static List<SelectOption> getSelectOptions() {

    SelectOption s_option1 = new SelectOption("option1", "Find out more about export control and licensing for restricted goods https://www.gov.uk/government/organisations/export-control-organisation");
    SelectOption s_option2 = new SelectOption("option2", "Find out if I need a licence to export goods and services https://www.gov.uk/guidance/beginners-guide-to-export-controls");
    SelectOption s_option3 = new SelectOption("option3", "Get a control rating for some goods and services and apply for a licence");
    SelectOption s_option4 = new SelectOption("option4", "Check a control rating");

    List<SelectOption> list = new ArrayList<>();
    list.add(s_option1);
    list.add(s_option2);
    list.add(s_option3);
    list.add(s_option4);
    return list;
  }

  public static class PrototypeController4Form {

    @Required(message = "Select one option")
    public String exportOptions;

  }

}
