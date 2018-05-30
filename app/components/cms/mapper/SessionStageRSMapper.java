package components.cms.mapper;

import org.skife.jdbi.v2.StatementContext;
import org.skife.jdbi.v2.tweak.ResultSetMapper;
import triage.session.SessionStage;
import utils.JsonUtils;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

public class SessionStageRSMapper implements ResultSetMapper<SessionStage> {

  @Override
  public SessionStage map(int index, ResultSet r, StatementContext ctx) throws SQLException {
    String sessionId = r.getString("session_id");
    long stageId = r.getLong("stage_id");
    List<String> answers = JsonUtils.convertJsonToList(r.getString("answer_json"));
    return new SessionStage(sessionId, stageId, answers);
  }

}
