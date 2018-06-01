package components.cms.jdbi;

import components.cms.mapper.SessionRSMapper;
import org.skife.jdbi.v2.sqlobject.Bind;
import org.skife.jdbi.v2.sqlobject.SqlQuery;
import org.skife.jdbi.v2.sqlobject.SqlUpdate;
import org.skife.jdbi.v2.sqlobject.customizers.RegisterMapper;
import triage.session.TriageSession;

public interface SessionJDBIDao {

  @SqlUpdate("INSERT INTO SESSION (ID,  JOURNEY_ID, RESUME_CODE, OUTCOME_TYPE, OUTCOME_HTML) VALUES " +
      "                          (:id, :journeyId, :resumeCode, :outcomeType, :outcomeHtml)")
  void insert(@Bind("id") String id,
              @Bind("journeyId") long journeyId,
              @Bind("resumeCode") String resumeCode,
              @Bind("outcomeType") String outcomeType,
              @Bind("outcomeHtml") String outcomeHtml);

  @RegisterMapper(SessionRSMapper.class)
  @SqlQuery("SELECT * FROM SESSION WHERE ID = :id")
  TriageSession getSessionById(@Bind("id") String id);

  @RegisterMapper(SessionRSMapper.class)
  @SqlQuery("SELECT * FROM SESSION WHERE RESUME_CODE = :resumeCode")
  TriageSession getSessionByResumeCode(@Bind("resumeCode") String resumeCode);

}