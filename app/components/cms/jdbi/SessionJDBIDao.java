package components.cms.jdbi;

import components.cms.mapper.SessionRSMapper;
import org.skife.jdbi.v2.sqlobject.Bind;
import org.skife.jdbi.v2.sqlobject.SqlQuery;
import org.skife.jdbi.v2.sqlobject.SqlUpdate;
import org.skife.jdbi.v2.sqlobject.customizers.RegisterMapper;
import triage.session.TriageSession;

public interface SessionJDBIDao {

  @SqlUpdate("INSERT INTO SESSION (ID, RESUME_CODE, LAST_STAGE_ID, SPREADSHEET_VERSION_ID) VALUES " +
      "                          (:id, :resumeCode, :lastStageId, :spreadsheetVersionId)")
  void insert(@Bind("id") String id,
              @Bind("resumeCode") String resumeCode,
              @Bind("lastStageId") Long lastStageId,
              @Bind("spreadsheetVersionId") long spreadsheetVersionId);

  @SqlUpdate("UPDATE SESSION SET LAST_STAGE_ID = :lastStageId WHERE id = :id")
  void updateLastStageId(@Bind("id") String sessionId, @Bind("lastStageId") Long lastStageId);

  @SqlUpdate("UPDATE SESSION SET DECONTROL_CODES_FOUND = :decontrolCodesFound WHERE id = :id")
  void updateDecontrolCodesFound(@Bind("id") String sessionId, @Bind("decontrolCodesFound") String decontrolCodesFound);

  @SqlUpdate("UPDATE SESSION SET CONTROL_ENTRY_IDS_TO_VERIFY_DECONTROLLED_STATUS = :controlEntryIdsToVerifyDecontrolledStatus WHERE id = :id")
  void updateControlEntryIdsToVerifyDecontrolledStatus(@Bind("id") String sessionId, @Bind("controlEntryIdsToVerifyDecontrolledStatus") String controlEntryIdsToVerifyDecontrolledStatus);

  @SqlUpdate("UPDATE SESSION SET JOURNEY_ID = :journeyId WHERE id = :id")
  void updateJourneyId(@Bind("id") String sessionId, @Bind("journeyId") Long journeyId);

  @RegisterMapper(SessionRSMapper.class)
  @SqlQuery("SELECT * FROM SESSION WHERE ID = :id")
  TriageSession getSessionById(@Bind("id") String id);

  @RegisterMapper(SessionRSMapper.class)
  @SqlQuery("SELECT * FROM SESSION WHERE REPLACE(RESUME_CODE, '-', '') = :resumeCode")
  TriageSession getSessionByResumeCode(@Bind("resumeCode") String resumeCode);
}
