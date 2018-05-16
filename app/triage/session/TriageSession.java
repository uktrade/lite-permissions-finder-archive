package triage.session;

public class TriageSession {

  public enum NlrType {
    DECONTROL, NOT_FOUND
  }

  private final String sessionId;
  private final String resumeCode;

  //Mutually exclusive
  private final NlrType nlrOutcome;
  private final String ogelRegistrationId;

  public TriageSession(String sessionId, String resumeCode) {
    this.sessionId = sessionId;
    this.resumeCode = resumeCode;
    this.nlrOutcome = null;
    this.ogelRegistrationId = null;
  }

  public String getSessionId() {
    return sessionId;
  }

  public String getResumeCode() {
    return resumeCode;
  }

  public boolean isLocked() {
    return nlrOutcome != null && ogelRegistrationId != null;
  }

}
