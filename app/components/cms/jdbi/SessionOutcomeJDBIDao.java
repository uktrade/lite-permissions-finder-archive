package components.cms.jdbi;

import components.cms.mapper.SessionOutcomeRSMapper;
import org.skife.jdbi.v2.sqlobject.Bind;
import org.skife.jdbi.v2.sqlobject.SqlQuery;
import org.skife.jdbi.v2.sqlobject.SqlUpdate;
import org.skife.jdbi.v2.sqlobject.customizers.RegisterMapper;
import triage.session.SessionOutcome;

public interface SessionOutcomeJDBIDao {

  @SqlUpdate("INSERT INTO SESSION_OUTCOME (ID,  SESSION_ID, USER_ID, CUSTOMER_ID, SITE_ID, OUTCOME_TYPE, OUTCOME_HTML) VALUES " +
      "                                  (:id, :sessionId, :userId, :customerId, :siteId, :outcomeType, :outcomeHtml)")
  void insert(@Bind("id") String id,
              @Bind("sessionId") String sessionId,
              @Bind("userId") String userId,
              @Bind("customerId") String customerId,
              @Bind("siteId") String siteId,
              @Bind("outcomeType") String outcomeType,
              @Bind("outcomeHtml") String outcomeHtml);

  @RegisterMapper(SessionOutcomeRSMapper.class)
  @SqlQuery("SELECT * FROM SESSION_OUTCOME WHERE SESSION_ID = :sessionId")
  SessionOutcome getSessionOutcomeBySessionId(@Bind("sessionId") String sessionId);

  @RegisterMapper(SessionOutcomeRSMapper.class)
  @SqlQuery("SELECT * FROM SESSION_OUTCOME WHERE ID = :id")
  SessionOutcome getSessionOutcomeById(@Bind("id") String id);

}
