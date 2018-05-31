package components.persistence.enums;

public enum SubmissionStatus {
  /** Not yet submitted registration */
  PENDING,

  /** Registration submitted, awaiting callback */
  SUBMITTED,

  /** Callback received */
  COMPLETED,

  /** Error sending registration request */
  FAILED
}
