package components.cms.mapper;

import components.cms.mapper.util.ResultSetWrapper;
import org.skife.jdbi.v2.StatementContext;
import org.skife.jdbi.v2.tweak.ResultSetMapper;
import triage.session.TriageSession;

import java.sql.ResultSet;
import java.sql.SQLException;

public class SessionRSMapper implements ResultSetMapper<TriageSession> {

  @Override
  public TriageSession map(int index, ResultSet r, StatementContext ctx) throws SQLException {
    ResultSetWrapper rsw = new ResultSetWrapper(r);
    String id = rsw.getString("id");
    long journeyId = rsw.getLong("journey_id");
    String resumeCode = rsw.getString("resume_code");
    String outcomeType = rsw.getString("outcome_type");
    String outcomeHtml = rsw.getString("outcome_html");
    Long lastStageId = rsw.getLong("last_stage_id");
    return new TriageSession(id, journeyId, resumeCode, outcomeType, outcomeHtml, lastStageId);
  }

}
