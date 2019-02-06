package triage.session;

import java.util.Collections;
import java.util.Set;

public class TriageSession {

  private final String id;
  private final Long journeyId;
  private final String resumeCode;
  private final Long lastStageId;
  private final Set<String> decontrolledCodesFound;
  private final Set<String> controlEntryIdsToVerifyDecontrolledStatus;

  public TriageSession(String id, Long journeyId, String resumeCode, Long lastStageId, Set<String> decontrolledCodesFound,
    Set<String> controlEntryIdsToVerifyDecontrolledStatus) {
    this.id = id;
    this.journeyId = journeyId;
    this.resumeCode = resumeCode;
    this.lastStageId = lastStageId;
    this.decontrolledCodesFound = decontrolledCodesFound;
    this.controlEntryIdsToVerifyDecontrolledStatus = controlEntryIdsToVerifyDecontrolledStatus;
  }

  public TriageSession(String id, String resumeCode) {
    this(id, null, resumeCode, null, Collections.EMPTY_SET, Collections.EMPTY_SET);
  }

  public String getId() {
    return id;
  }

  public Long getJourneyId() {
    return journeyId;
  }

  public String getResumeCode() {
    return resumeCode;
  }

  public Long getLastStageId() {
    return lastStageId;
  }

  public Set<String> getDecontrolledCodesFound() {
    return decontrolledCodesFound;
  }

  public Set<String> getControlEntryIdsToVerifyDecontrolledStatus() {
    return controlEntryIdsToVerifyDecontrolledStatus;
  }
}
