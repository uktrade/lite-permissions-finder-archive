package controllers.registration;

import com.google.inject.Inject;
import components.auth.SamlAuthorizer;
import components.common.CommonContextAction;
import components.common.auth.SpireSAML2Client;
import components.common.state.ContextParamManager;
import components.common.transaction.TransactionManager;
import components.persistence.enums.SubmissionStatus;
import components.services.registration.RegistrationSubmissionService;
import org.pac4j.play.java.Secure;
import play.mvc.Controller;
import play.mvc.Result;
import play.mvc.With;
import uk.gov.bis.lite.permissions.api.view.CallbackView;

import java.util.concurrent.CompletionStage;

@With(CommonContextAction.class)
@Secure(clients = SpireSAML2Client.CLIENT_NAME, authorizers = SamlAuthorizer.AUTHORIZER_NAME)
public class RegistrationOutcomeController extends Controller {

  private final RegistrationSubmissionService submissionService;
  private final ContextParamManager contextParamManager;
  private final TransactionManager transactionManager;
  private final views.html.registration.registrationConfirmation registrationConfirmation;
  private final views.html.registration.registrationInterval registrationInterval;
  private final views.html.registration.registrationRejection registrationRejection;
  private final views.html.registration.permissionDenied permissionDenied;

  @Inject
  public RegistrationOutcomeController(RegistrationSubmissionService submissionService,
                                       ContextParamManager contextParamManager, TransactionManager transactionManager,
                                       views.html.registration.registrationConfirmation registrationConfirmation,
                                       views.html.registration.registrationInterval registrationInterval,
                                       views.html.registration.registrationRejection registrationRejection, views.html.registration.permissionDenied permissionDenied) {
    this.submissionService = submissionService;
    this.contextParamManager = contextParamManager;
    this.transactionManager = transactionManager;
    this.registrationConfirmation = registrationConfirmation;
    this.registrationInterval = registrationInterval;
    this.registrationRejection = registrationRejection;
    this.permissionDenied = permissionDenied;
  }

  public Result renderForm() {
    String transactionId = transactionManager.getTransactionId();

    SubmissionStatus status = submissionService.getSubmissionStatus(transactionId);

    switch (status) {
      case SUBMITTED:
        boolean showLongRunningPrompt = submissionService.getSecondsSinceRegistrationSubmission(transactionId) >= 15;
        return ok(registrationInterval.render(showLongRunningPrompt));
      case COMPLETED:
        return handleSubmissionCompleted(transactionId);
      case FAILED:
      default:
        return ok(registrationRejection.render(submissionService.getCallbackResult(transactionId).orElse(null)));
    }
  }

  private Result handleSubmissionCompleted(String transactionId) {
    CallbackView.Result result = submissionService.getCallbackResult(transactionId).orElse(CallbackView.Result.FAILED);
    switch (result) {
      case SUCCESS:
        return ok(registrationConfirmation.render(submissionService.getRegistrationRef(transactionId)));
      case PERMISSION_DENIED:
        return ok(permissionDenied.render());
      default:
        return ok(registrationRejection.render(result));
    }
  }

  /**
   * Handles the RegistrationInterval form submission
   */
  public CompletionStage<Result> handleRegistrationProcessed() {
    return contextParamManager.addParamsAndRedirect(routes.RegistrationOutcomeController.renderForm());
  }

}
