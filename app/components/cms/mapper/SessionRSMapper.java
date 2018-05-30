package components.cms.mapper;

import org.skife.jdbi.v2.StatementContext;
import org.skife.jdbi.v2.tweak.ResultSetMapper;
import triage.session.TriageSession;

import java.sql.ResultSet;
import java.sql.SQLException;

public class SessionRSMapper implements ResultSetMapper<TriageSession> {

  @Override
  public TriageSession map(int index, ResultSet r, StatementContext ctx) throws SQLException {
    String id = r.getString("id");
    long journeyId = r.getLong("journey_id");
    String resumeCode = r.getString("resume_code");
    String outcomeType = r.getString("outcome_type");
    String outcomeHtml = r.getString("outcome_html");
    return new TriageSession(id, journeyId, resumeCode, outcomeType, outcomeHtml);
  }

}
