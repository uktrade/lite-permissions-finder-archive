package components.cms.dao.impl;

import com.google.inject.Inject;
import components.cms.dao.SessionDao;
import components.cms.jdbi.SessionJDBIDao;
import org.skife.jdbi.v2.DBI;
import triage.session.TriageSession;

public class SessionDaoImpl implements SessionDao {

  private final SessionJDBIDao sessionJDBIDao;

  @Inject
  public SessionDaoImpl(DBI dbi) {
    this.sessionJDBIDao = dbi.onDemand(SessionJDBIDao.class);
  }

  @Override
  public void insert(TriageSession triageSession) {
    sessionJDBIDao.insert(triageSession.getId(),
        triageSession.getJourneyId(),
        triageSession.getResumeCode(),
        triageSession.getLastStageId());
  }

  @Override
  public void updateLastStageId(String sessionId, Long lastStageId) {
    sessionJDBIDao.updateLastStageId(sessionId, lastStageId);
  }

  @Override
  public void updateJourneyId(String sessionId, Long journeyId) {
    sessionJDBIDao.updateJourneyId(sessionId, journeyId);
  }

  @Override
  public TriageSession getSessionById(String id) {
    return sessionJDBIDao.getSessionById(id);
  }

  @Override
  public TriageSession getSessionByResumeCode(String resumeCode) {
    return sessionJDBIDao.getSessionByResumeCode(resumeCode);
  }

}
