package controllers;

import com.google.inject.Inject;
import components.common.transaction.TransactionManager;
import play.Logger;
import play.mvc.*;

import views.html.*;


public class EntryPointController extends Controller {

  private final TransactionManager transactionManager;

  @Inject
  public EntryPointController(TransactionManager transactionManager){
    this.transactionManager = transactionManager;
  }

  public Result index() {
    transactionManager.createTransaction();
    return ok(index.render());
  }

}
