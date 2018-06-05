package components.cms.dao;

import triage.session.TriageSession;

public interface SessionDao {

  void insert(TriageSession triageSession);

  TriageSession getSessionById(String id);

  TriageSession getSessionByResumeCode(String resumeCode);
}
