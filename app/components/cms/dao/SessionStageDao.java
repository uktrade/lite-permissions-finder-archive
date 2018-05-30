package components.cms.dao;

import triage.session.SessionStage;

public interface SessionStageDao {

  void insert(SessionStage sessionStage);

  SessionStage getSessionStage(String sessionId, long stageId);
}
