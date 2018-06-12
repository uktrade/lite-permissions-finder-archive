package triage.session;

public class TriageSession {

  private final String id;
  private final long journeyId;
  private final String resumeCode;
  private final Long lastStageId;

  public TriageSession(String id, long journeyId, String resumeCode, Long lastStageId) {
    this.id = id;
    this.journeyId = journeyId;
    this.resumeCode = resumeCode;
    this.lastStageId = lastStageId;
  }

  public String getId() {
    return id;
  }

  public long getJourneyId() {
    return journeyId;
  }

  public String getResumeCode() {
    return resumeCode;
  }

  public Long getLastStageId() {
    return lastStageId;
  }

}
