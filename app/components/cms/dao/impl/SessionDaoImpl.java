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
        triageSession.getOutcomeType(),
        triageSession.getOutcomeHtml());
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
