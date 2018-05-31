package components.services;

import components.persistence.enums.SubmissionStatus;
import uk.gov.bis.lite.permissions.api.view.CallbackView;

import java.util.Optional;
import java.util.concurrent.CompletionStage;

public interface LicenceFinderService {

  void persistCustomerAndSiteData();

  CompletionStage<Void> registerOgel(String transactionId);

  void handleCallback(String transactionId, CallbackView callbackView);

  Optional<String> getRegistrationReference(String transactionId);

  SubmissionStatus getSubmissionStatus(String transactionId);

  long getSecondsSinceRegistrationSubmission(String transactionId);

  Optional<CallbackView.Result> getCallbackResult(String transactionId);

  String getRegistrationRef(String transactionId);

}
