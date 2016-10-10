package controllers;

import static java.util.concurrent.CompletableFuture.completedFuture;
import static play.mvc.Results.badRequest;
import static play.mvc.Results.ok;

import com.google.inject.Inject;
import components.common.state.ContextParamManager;
import components.common.transaction.TransactionManager;
import components.persistence.ApplicationCodeDao;
import components.persistence.PermissionsFinderDao;
import components.services.ogels.registration.OgelRegistrationServiceClient;
import org.apache.commons.lang3.StringUtils;
import play.data.Form;
import play.data.FormFactory;
import play.data.validation.Constraints.Required;
import play.mvc.Result;
import views.html.continueApplication;

import java.util.Optional;
import java.util.concurrent.CompletionStage;

public class ContinueApplicationController {

  private final TransactionManager transactionManager;
  private final FormFactory formFactory;
  private final ApplicationCodeDao applicationCodeDao;
  private final PermissionsFinderDao permissionsFinderDao;
  private final ContextParamManager contextParamManager;
  private final OgelRegistrationServiceClient ogelRegistrationServiceClient;

  @Inject
  public ContinueApplicationController(TransactionManager transactionManager,
                                       FormFactory formFactory,
                                       ApplicationCodeDao applicationCodeDao,
                                       PermissionsFinderDao permissionsFinderDao,
                                       ContextParamManager contextParamManager,
                                       OgelRegistrationServiceClient ogelRegistrationServiceClient) {
    this.transactionManager = transactionManager;
    this.formFactory = formFactory;
    this.permissionsFinderDao = permissionsFinderDao;
    this.applicationCodeDao = applicationCodeDao;
    this.contextParamManager = contextParamManager;
    this.ogelRegistrationServiceClient = ogelRegistrationServiceClient;
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

    if (StringUtils.isNoneBlank(applicationCode)) {
      String transactionId = applicationCodeDao.readTransactionId(applicationCode.trim());
      if (transactionId != null && !transactionId.isEmpty()) {
        transactionManager.createTransaction(transactionId);
        Optional<Boolean> ogelRegistrationExists = permissionsFinderDao.getOgelRegistrationServiceTransactionExists();
        if (ogelRegistrationExists.isPresent() && ogelRegistrationExists.get()) {
          return ogelRegistrationServiceClient.updateTransactionAndRedirect(transactionId);
        }
        else {
          return contextParamManager.addParamsAndRedirect(routes.SummaryController.renderFormContinue());
        }
      }
      else {
        form.reject("applicationCode", "You have entered an invalid application code");
        return completedFuture(ok(continueApplication.render(form)));
      }
    }
    return completedFuture(badRequest("Unhandled form state"));
  }

  public static class ContinueApplicationForm {

    @Required(message = "You must enter your application code")
    public String applicationCode;

  }

}

