package triage.session;

import java.util.List;

public class SessionStage {

  private final String sessionId;
  private final long stageId;
  private final List<String> answers;

  public SessionStage(String sessionId, long stageId, List<String> answers) {
    this.sessionId = sessionId;
    this.stageId = stageId;
    this.answers = answers;
  }

  public String getSessionId() {
    return sessionId;
  }

  public long getStageId() {
    return stageId;
  }

  public List<String> getAnswers() {
    return answers;
  }

}
