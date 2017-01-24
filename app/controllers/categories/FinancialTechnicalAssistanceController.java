package controllers.categories;

import static play.mvc.Results.ok;

import com.google.inject.Inject;
import components.common.journey.JourneyManager;
import components.common.journey.StandardEvents;
import exceptions.FormStateException;
import play.data.Form;
import play.data.FormFactory;
import play.mvc.Result;
import views.html.categories.financialTechnicalAssistance;

import java.util.concurrent.CompletionStage;

public class FinancialTechnicalAssistanceController {

  private final JourneyManager journeyManager;
  private final FormFactory formFactory;

  @Inject
  public FinancialTechnicalAssistanceController(FormFactory formFactory, JourneyManager journeyManager) {
    this.formFactory = formFactory;
    this.journeyManager = journeyManager;
  }

  public Result renderForm() {
    return ok(financialTechnicalAssistance.render(formFactory.form(FinancialTechnicalAssistanceForm.class)));
  }

  public CompletionStage<Result> handleSubmit() {
    Form<FinancialTechnicalAssistanceForm> form = formFactory.form(FinancialTechnicalAssistanceForm.class).bindFromRequest();
    if ("goToSearch".equals(form.get().action)) {
      return journeyManager.performTransition(StandardEvents.NEXT);
    }
    throw new FormStateException("Unknown value of action: \"" + form.get().action + "\"");
  }

  public static class FinancialTechnicalAssistanceForm {

    public String action;

  }

}
