package components.cms.mapper;

import components.cms.mapper.util.ResultSetWrapper;
import models.cms.Journey;
import org.skife.jdbi.v2.StatementContext;
import org.skife.jdbi.v2.tweak.ResultSetMapper;

import java.sql.ResultSet;
import java.sql.SQLException;

public class JourneyRSMapper implements ResultSetMapper<Journey> {
  @Override
  public Journey map(int index, ResultSet r, StatementContext ctx) throws SQLException {
    ResultSetWrapper rsw = new ResultSetWrapper(r);
    Long id = rsw.getLong("id");
    String journeyName = r.getString("journey_name");
    Long initialStageId = rsw.getLong("initial_stage_id");
    return new Journey()
        .setId(id)
        .setJourneyName(journeyName)
        .setInitialStageId(initialStageId);
  }
}
