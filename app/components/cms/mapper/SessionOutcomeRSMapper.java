package components.cms.mapper;

import models.enums.OutcomeType;
import org.skife.jdbi.v2.StatementContext;
import org.skife.jdbi.v2.tweak.ResultSetMapper;
import triage.session.SessionOutcome;

import java.sql.ResultSet;
import java.sql.SQLException;

public class SessionOutcomeRSMapper implements ResultSetMapper<SessionOutcome> {

  @Override
  public SessionOutcome map(int index, ResultSet r, StatementContext ctx) throws SQLException {
    long id = r.getLong("id");
    String sessionId = r.getString("session_id");
    String userId = r.getString("user_id");
    String customerId = r.getString("customer_id");
    String siteId = r.getString("site_id");
    String outcomeTypeStr = r.getString("outcome_type");
    OutcomeType outcomeType = OutcomeType.valueOf(outcomeTypeStr);
    String outcomeHtml = r.getString("outcome_html");
    return new SessionOutcome(id, sessionId, userId, customerId, siteId, outcomeType, outcomeHtml);
  }

}
