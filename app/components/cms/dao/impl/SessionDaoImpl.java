package components.cms.dao.impl;

import com.google.inject.Inject;
import components.cms.dao.SessionDao;
import components.cms.jdbi.SessionJDBIDao;
import java.util.List;
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
    sessionJDBIDao.insert(triageSession.getId(), triageSession.getResumeCode(), triageSession.getLastStageId(),
        triageSession.getSpreadsheetVersionId());
  }

  @Override
  public void updateLastStageId(String sessionId, Long lastStageId) {
    sessionJDBIDao.updateLastStageId(sessionId, lastStageId);
  }

  @Override
  public void updateDecontrolCodesFound(String sessionId, List<String> decontrolCodesFound) {
    sessionJDBIDao.updateDecontrolCodesFound(sessionId, String.join(",", decontrolCodesFound));
  }

  @Override
  public void updateControlEntryIdsToVerifyDecontrolledStatus(String sessionId, List<String> controlEntryIdsToVerifyDecontrolledStatus) {
    sessionJDBIDao.updateControlEntryIdsToVerifyDecontrolledStatus(sessionId, String.join(",",
      controlEntryIdsToVerifyDecontrolledStatus));
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
