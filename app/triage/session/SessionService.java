package triage.session;

import java.util.Set;

public interface SessionService {

  TriageSession createNewSession();

  TriageSession getSessionById(String id);

  TriageSession getSessionByResumeCode(String resumeCode);

  //Gets 0 or more answer IDs which were selected on the given stage
  Set<String> getAnswersForStageId(String sessionId, String stageId);

  void saveAnswersForStageId(String sessionId, String stageId, Set<String> answerIds);

  String getStageId(String sessionId);

}
