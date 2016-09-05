package controllers.categories;

import static java.util.concurrent.CompletableFuture.completedFuture;
import static play.mvc.Results.badRequest;
import static play.mvc.Results.ok;

import com.google.inject.Inject;
import components.common.journey.JourneyManager;
import components.common.journey.StandardEvents;
import play.data.Form;
import play.data.FormFactory;
import play.mvc.Result;
import views.html.categories.financialTechnicalAssistance;

import java.util.concurrent.CompletionStage;

public class FinancialTechnicalAssistanceController {

  private final JourneyManager jm;
  private final FormFactory formFactory;

  @Inject
  public FinancialTechnicalAssistanceController(FormFactory formFactory, JourneyManager jm) {
    this.formFactory = formFactory;
    this.jm = jm;
  }

  public Result renderForm() {
    return ok(financialTechnicalAssistance.render(formFactory.form(FinancialTechnicalAssistanceForm.class)));
  }

  public CompletionStage<Result> handleSubmit() {
    Form<FinancialTechnicalAssistanceForm> form = formFactory.form(FinancialTechnicalAssistanceForm.class).bindFromRequest();
    if ("true".equals(form.get().goToSearch)) {
      return jm.performTransition(StandardEvents.NEXT);
    }
    return completedFuture(badRequest("Unknown value of goToSearch: \"" + form.get().goToSearch + "\""));
  }

  public static class FinancialTechnicalAssistanceForm {

    public String goToSearch;

  }

}
