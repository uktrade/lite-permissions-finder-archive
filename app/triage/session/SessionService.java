package triage.session;

import java.util.Set;

public interface SessionService {

  TriageSession createNewSession();

  TriageSession getSessionById(String id);

  TriageSession getSessionByResumeCode(String resumeCode);

  //Gets 0 or more answer IDs which were selected on the given stage
  Set<String> getAnswerIdsForStageId(String sessionId, String stageId);

  void saveAnswerIdsForStageId(String sessionId, String stageId, Set<String> answerIds);

  void updateLastStageId(String sessionId, String lastStageId);
}
