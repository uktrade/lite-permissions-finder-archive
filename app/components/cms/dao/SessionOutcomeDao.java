package components.cms.dao;

import triage.session.SessionOutcome;

public interface SessionOutcomeDao {

  void insert(SessionOutcome sessionOutcome);

  SessionOutcome getSessionOutcomeBySessionId(String sessionId);

  SessionOutcome getSessionOutcomeById(String id);

}
