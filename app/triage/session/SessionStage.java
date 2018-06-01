package triage.session;

import java.util.List;

public class SessionStage {

  private final String sessionId;
  private final long stageId;
  private final List<String> answerIds;

  public SessionStage(String sessionId, long stageId, List<String> answerIds) {
    this.sessionId = sessionId;
    this.stageId = stageId;
    this.answerIds = answerIds;
  }

  public String getSessionId() {
    return sessionId;
  }

  public long getStageId() {
    return stageId;
  }

  public List<String> getAnswerIds() {
    return answerIds;
  }

}
