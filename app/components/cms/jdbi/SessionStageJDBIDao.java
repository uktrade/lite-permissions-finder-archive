package components.cms.jdbi;

import components.cms.mapper.SessionStageRSMapper;
import org.skife.jdbi.v2.sqlobject.Bind;
import org.skife.jdbi.v2.sqlobject.SqlQuery;
import org.skife.jdbi.v2.sqlobject.SqlUpdate;
import org.skife.jdbi.v2.sqlobject.customizers.RegisterMapper;
import triage.session.SessionStage;

public interface SessionStageJDBIDao {

  @SqlUpdate("INSERT INTO SESSION_STAGE (SESSION_ID,  STAGE_ID, ANSWER_JSON) VALUES " +
      "                                (:sessionId,  :stageId, :answerJson) " +
      "ON CONFLICT (SESSION_ID,  STAGE_ID) DO UPDATE SET ANSWER_JSON = :answerJson")
  void insertOrUpdate(@Bind("sessionId") String sessionId,
                      @Bind("stageId") long stageId,
                      @Bind("answerJson") String answerJson);

  @RegisterMapper(SessionStageRSMapper.class)
  @SqlQuery("SELECT * FROM SESSION_STAGE WHERE session_id = :sessionId AND stage_id = :stageId")
  SessionStage getSessionStage(@Bind("sessionId") String sessionId, @Bind("stageId") long stageId);

  @SqlUpdate("DELETE FROM SESSION_STAGE")
  void truncate();

}
