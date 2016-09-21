package controllers;

import static java.util.concurrent.CompletableFuture.completedFuture;

import com.google.inject.Inject;
import components.common.journey.JourneyManager;
import components.common.transaction.TransactionManager;
import journey.Events;
import play.data.Form;
import play.data.FormFactory;
import play.mvc.*;

import views.html.*;

import java.util.concurrent.CompletionStage;


public class EntryPointController extends Controller {

  private final TransactionManager transactionManager;
  private final JourneyManager journeyManager;
  private final FormFactory formFactory;

  @Inject
  public EntryPointController(TransactionManager transactionManager,
                              JourneyManager journeyManager,
                              FormFactory formFactory) {
    this.transactionManager = transactionManager;
    this.journeyManager = journeyManager;
    this.formFactory = formFactory;
  }

  public Result index() {
    journeyManager.startJourney("default");
    return ok(index.render());
  }

  public CompletionStage<Result> handleSubmit() {
    transactionManager.createTransaction();
    Form<EntryPointForm> form = formFactory.form(EntryPointForm.class).bindFromRequest();
    String action = form.get().action;
    if ("start".equals(action)) {
      return journeyManager.performTransition(Events.START_APPLICATION);
    }
    if ("continue".equals(action)) {
      return journeyManager.performTransition(Events.CONTINUE_APPLICATION);
    }
    return completedFuture(badRequest("Unknown value for action: \"" + action + "\""));
  }

  public static class EntryPointForm {

    public String action;

  }

}
