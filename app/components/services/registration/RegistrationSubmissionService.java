package components.services.registration;


import com.google.inject.Inject;
import components.persistence.RegistrationSubmissionDao;
import components.persistence.enums.SubmissionStatus;
import play.Logger;
import uk.gov.bis.lite.permissions.api.view.CallbackView;

import java.time.Duration;
import java.time.Instant;
import java.util.Optional;

public class RegistrationSubmissionService {

  private static final Logger.ALogger LOGGER = Logger.of(RegistrationSubmissionService.class);

  private final RegistrationSubmissionDao submissionDao;

  @Inject
  public RegistrationSubmissionService(RegistrationSubmissionDao submissionDao) {
    this.submissionDao = submissionDao;
  }

  ///
  public SubmissionStatus getSubmissionStatus(String transactionId) {
    Optional<Boolean> isSuccess = submissionDao.getSubmissionRequestSuccess(transactionId);
    if (isSuccess.isPresent()) {
      if (isSuccess.get()) {
        Optional<CallbackView.Result> result = submissionDao.getCallbackResult(transactionId);
        if (result.isPresent()) {
          return SubmissionStatus.COMPLETED;
        } else {
          return SubmissionStatus.SUBMITTED;
        }
      } else {
        return SubmissionStatus.FAILED;
      }
    } else {
      return SubmissionStatus.PENDING;
    }
  }

  ///
  public Optional<CallbackView.Result> getCallbackResult(String transactionId) {
    return submissionDao.getCallbackResult(transactionId);
  }

  //
  public String getRegistrationRef(String transactionId) {
    return submissionDao.getRegistrationRef(transactionId);
  }

  ///
  public long getSecondsSinceRegistrationSubmission(String transactionId) {
    Optional<Instant> submissionDateTime = submissionDao.getSubmissionRequestDateTime(transactionId);
    if (submissionDateTime.isPresent()) {
      return Duration.between(submissionDateTime.get(), Instant.now()).getSeconds();
    } else {
      return 0L;
    }
  }

}

