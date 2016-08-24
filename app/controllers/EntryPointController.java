package controllers;

import com.google.inject.Inject;
import components.common.transaction.TransactionManager;
import play.data.Form;
import play.data.FormFactory;
import play.mvc.*;

import views.html.*;


public class EntryPointController extends Controller {

  private final TransactionManager transactionManager;
  private final FormFactory formFactory;
  private final StartApplicationController startApplicationController;
  private final ContinueApplicationController continueApplicationController;

  @Inject
  public EntryPointController(TransactionManager transactionManager,
                              FormFactory formFactory,
                              StartApplicationController startApplicationController,
                              ContinueApplicationController continueApplicationController) {
    this.transactionManager = transactionManager;
    this.formFactory = formFactory;
    this.startApplicationController = startApplicationController;
    this.continueApplicationController = continueApplicationController;
  }

  public Result index() {
    return ok(index.render());
  }

  public Result handleSubmit() {
    transactionManager.createTransaction();
    Form<EntryPointForm> form = formFactory.form(EntryPointForm.class).bindFromRequest();
    String action = form.get().action;
    if ("start".equals(action)) {
      return startApplicationController.renderForm();
    }
    if ("continue".equals(action)) {
      return continueApplicationController.renderForm();
    }
    return badRequest("Unknown value for action: \"" + action + "\"");
  }

  public static class EntryPointForm {

    public String action;

  }

}
