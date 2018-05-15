package components.persistence;

import com.google.inject.Inject;
import com.google.inject.name.Named;
import components.common.persistence.StatelessRedisDao;
import models.persistence.ApplicantDetails;
import org.apache.commons.lang3.StringUtils;
import uk.gov.bis.lite.permissions.api.view.CallbackView;

import java.time.Instant;
import java.util.Optional;

public class RegistrationSubmissionDao {

  private static final String SUBMISSION_REQUEST_SUCCESS = "submissionRequest:success";
  private static final String SUBMISSION_REQUEST_ID = "submissionRequest:id";
  private static final String SUBMISSION_REQUEST_DATE_TIME = "submissionRequest:dateTime";
  private static final String REGISTRATION_REF = "registrationRef";
  private static final String APPLICANT_DETAILS = "applicantDetails";
  private static final String CALLBACK_RESULT = "callbackResult";

  private final StatelessRedisDao statelessRedisDao;

  @Inject
  public RegistrationSubmissionDao(@Named("registrationSubmissionStateless") StatelessRedisDao statelessRedisDao) {
    this.statelessRedisDao = statelessRedisDao;
  }

  public void saveSubmissionRequestSuccess(String transactionId, boolean success) {
    statelessRedisDao.writeString(transactionId, SUBMISSION_REQUEST_SUCCESS, Boolean.toString(success));
  }

  public void saveSubmissionRequestId(String transactionId, String requestId) {
    statelessRedisDao.writeString(transactionId, SUBMISSION_REQUEST_ID, requestId);
  }

  public void saveRegistrationRef(String transactionId, String licenceNumber) {
    statelessRedisDao.writeString(transactionId, REGISTRATION_REF, licenceNumber);
  }

  public void saveApplicantDetails(String transactionId, ApplicantDetails applicantDetails) {
    statelessRedisDao.writeObject(transactionId, APPLICANT_DETAILS, applicantDetails);
  }

  public void saveCallbackResult(String transactionId, CallbackView.Result callbackResult) {
    statelessRedisDao.writeString(transactionId, CALLBACK_RESULT, callbackResult.toString());
  }

  public void saveSubmissionDateTime(String transactionId, Instant instant) {
    statelessRedisDao.writeString(transactionId, SUBMISSION_REQUEST_DATE_TIME, instant.toString());
  }

  public Optional<Boolean> getSubmissionRequestSuccess(String transactionId) {
    String isSuccess = statelessRedisDao.readString(transactionId, SUBMISSION_REQUEST_SUCCESS);
    if (StringUtils.isBlank(isSuccess)) {
      return Optional.empty();
    } else {
      return Optional.of(Boolean.parseBoolean(isSuccess));
    }
  }

  public String getSubmissionRequestId(String transactionId) {
    return statelessRedisDao.readString(transactionId, SUBMISSION_REQUEST_ID);
  }

  public String getRegistrationRef(String transactionId) {
    return statelessRedisDao.readString(transactionId, REGISTRATION_REF);
  }

  public Optional<Instant> getSubmissionRequestDateTime(String transactionId) {
    String dateTime = statelessRedisDao.readString(transactionId, SUBMISSION_REQUEST_DATE_TIME);
    if (StringUtils.isNotEmpty(dateTime)) {
      return Optional.of(Instant.parse(dateTime));
    } else {
      return Optional.empty();
    }
  }

  public Optional<ApplicantDetails> getApplicantDetails(String transactionId) {
    return statelessRedisDao.readObject(transactionId, APPLICANT_DETAILS, ApplicantDetails.class);
  }

  public Optional<CallbackView.Result> getCallbackResult(String transactionId) {
    String value = statelessRedisDao.readString(transactionId, CALLBACK_RESULT);
    if (value == null) {
      return Optional.empty();
    } else {
      return Optional.of(CallbackView.Result.valueOf(value));
    }
  }

}
