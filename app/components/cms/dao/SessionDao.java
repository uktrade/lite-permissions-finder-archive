package components.cms.dao;

import java.util.Collection;
import java.util.List;
import triage.session.TriageSession;

public interface SessionDao {

  void insert(TriageSession triageSession);

  void updateLastStageId(String sessionId, Long lastStageId);

  void updateDecontrolCodesFound(String sessionId, List<String> decontrolCodesFound);

  void updateControlCodesToConfirmDecontrolledStatus(String sessionId, Collection<String> controlCodesToConfirmDecontrolledStatus);

  void updateJourneyId(String sessionId, Long journeyId);

  TriageSession getSessionById(String id);

  TriageSession getSessionByResumeCode(String resumeCode);
}
