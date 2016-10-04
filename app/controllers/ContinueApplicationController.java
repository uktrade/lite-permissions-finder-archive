package controllers;

import static java.util.concurrent.CompletableFuture.completedFuture;
import static play.mvc.Results.badRequest;
import static play.mvc.Results.ok;

import com.google.inject.Inject;
import components.common.journey.JourneyManager;
import components.common.transaction.TransactionManager;
import components.persistence.ApplicationCodeDao;
import play.data.Form;
import play.data.FormFactory;
import play.data.validation.Constraints.Required;
import play.mvc.Result;
import views.html.continueApplication;

import java.util.concurrent.CompletionStage;

public class ContinueApplicationController {

  private final TransactionManager transactionManager;
  private final JourneyManager journeyManager;
  private final FormFactory formFactory;
  private final ApplicationCodeDao applicationCodeDao;

  @Inject
  public ContinueApplicationController(TransactionManager transactionManager,
                                       JourneyManager journeyManager,
                                       FormFactory formFactory,
                                       ApplicationCodeDao applicationCodeDao) {
    this.transactionManager = transactionManager;
    this.journeyManager = journeyManager;
    this.formFactory = formFactory;
    this.applicationCodeDao = applicationCodeDao;
  }

  public Result renderForm() {
    return ok(continueApplication.render(formFactory.form(ContinueApplicationForm.class)));
  }

  public CompletionStage<Result> handleSubmit() {
    Form<ContinueApplicationForm> form = formFactory.form(ContinueApplicationForm.class).bindFromRequest();
    if (form.hasErrors()) {
      return completedFuture(ok(continueApplication.render(form)));
    }
    String applicationCode = form.get().applicationCode;
    String memorableWord = form.get().memorableWord;
    if (applicationCode != null && !applicationCode.isEmpty() && memorableWord != null && !memorableWord.isEmpty()) {
      String transactionId = applicationCodeDao.readTransactionId(applicationCode);
      if (transactionId != null && !transactionId.isEmpty()) {
        transactionManager.createTransaction(transactionId);
        return journeyManager.restoreCurrentStage();
      }
      else {
        form.reject("applicationCode", "You have entered an invalid claim number");
        return completedFuture(ok(continueApplication.render(form)));
      }
    }
    return completedFuture(badRequest("Unhandled form state"));
  }

  public static class ContinueApplicationForm {

    @Required(message = "You must enter your application number")
    public String applicationCode;

    @Required(message = "You must enter your memorable word")
    public String memorableWord;

  }

}

