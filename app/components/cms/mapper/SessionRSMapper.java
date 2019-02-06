package components.cms.mapper;

import components.cms.mapper.util.ResultSetWrapper;
import java.util.HashSet;
import org.skife.jdbi.v2.StatementContext;
import org.skife.jdbi.v2.tweak.ResultSetMapper;
import triage.session.TriageSession;

import java.sql.ResultSet;
import java.sql.SQLException;

public class SessionRSMapper implements ResultSetMapper<TriageSession> {

  @Override
  public TriageSession map(int index, ResultSet r, StatementContext ctx) throws SQLException {
    ResultSetWrapper rsw = new ResultSetWrapper(r);
    return new TriageSession(
      r.getString("id"),
      rsw.getLong("journey_id"),
      r.getString("resume_code"),
      rsw.getLong("last_stage_id"),
      new HashSet<>(rsw.getStrings("decontrol_codes_found")),
      new HashSet<>(rsw.getStrings("control_entry_ids_to_verify_decontrolled_status"))
    );
  }
}
