package triage.session;

import java.util.Optional;
import java.util.Set;
import models.cms.Journey;

public interface SessionService {

  TriageSession createNewSession();

  TriageSession getSessionById(String id);

  TriageSession getSessionByResumeCode(String resumeCode);

  //Gets 0 or more answer IDs which were selected on the given stage
  Set<String> getAnswerIdsForStageId(String sessionId, String stageId);

  void saveAnswerIdsForStageId(String sessionId, String stageId, Set<String> answerIds);

  void updateLastStageId(String sessionId, String lastStageId);

  void addDecontrolledCodeFound(String sessionId, String controlCode, Set<String> jumpToControlCodes);

  Set<String> getControlCodesToConfirmDecontrolledStatus(String sessionId);

  Optional<String> getAndRemoveControlCodeToConfirmDecontrolledStatus(String sessionId);

  void bindSessionToJourney(String sessionId, Journey journey);
}
