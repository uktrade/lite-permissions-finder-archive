package components.cms.dao.impl;

import com.google.inject.Inject;
import components.cms.dao.SessionStageDao;
import components.cms.jdbi.SessionStageJDBIDao;
import org.skife.jdbi.v2.DBI;
import triage.session.SessionStage;
import utils.JsonUtils;

public class SessionStageDaoImpl implements SessionStageDao {

  private final SessionStageJDBIDao sessionStageJDBIDao;

  @Inject
  public SessionStageDaoImpl(DBI dbi) {
    this.sessionStageJDBIDao = dbi.onDemand(SessionStageJDBIDao.class);
  }

  @Override
  public void insert(SessionStage sessionStage) {
    sessionStageJDBIDao.insert(sessionStage.getSessionId(),
        sessionStage.getStageId(),
        JsonUtils.convertListToJson(sessionStage.getAnswers()));
  }

  @Override
  public SessionStage getSessionStage(String sessionId, long stageId) {
    return sessionStageJDBIDao.getSessionStage(sessionId, stageId);
  }

}
