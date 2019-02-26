package components.cms.jdbi;

import components.cms.mapper.JourneyRSMapper;
import models.cms.Journey;
import org.skife.jdbi.v2.sqlobject.Bind;
import org.skife.jdbi.v2.sqlobject.SqlQuery;
import org.skife.jdbi.v2.sqlobject.SqlUpdate;
import org.skife.jdbi.v2.sqlobject.customizers.Mapper;

import java.util.List;

public interface JourneyJDBIDao {

  @Mapper(JourneyRSMapper.class)
  @SqlQuery("SELECT * FROM journey WHERE id = :id")
  Journey get(@Bind("id") long id);

  @Mapper(JourneyRSMapper.class)
  @SqlQuery("SELECT * FROM journey")
  List<Journey> getAll();

  @Mapper(JourneyRSMapper.class)
  @SqlQuery("SELECT * FROM journey WHERE journey_name = :journeyName ORDER BY id DESC LIMIT 1")
  Journey getByJourneyName(@Bind("journeyName") String journeyName);

  @SqlQuery(
      "INSERT INTO journey (journey_name, friendly_journey_name, initial_stage_id) " +
          "VALUES (:journeyName, :friendlyJourneyName, :initialStageId) " +
          "RETURNING id")
  Long insert(@Bind("journeyName") String journeyName, @Bind("friendlyJourneyName") String friendlyJourneyName,
              @Bind("initialStageId") Long initialStageId);

  @SqlUpdate(
      "UPDATE journey " +
          "SET " +
          "journey_name = :journeyName, " +
          "friendly_journey_name = :friendlyJourneyName, " +
          "initial_stage_id = :initialStageId " +
          "WHERE id = :id")
  void update(@Bind("id") long id, @Bind("journeyName") String journeyName,
              @Bind("friendlyJourneyName") String friendlyJourneyName, @Bind("initialStageId") Long initialStageId);

  @SqlUpdate("DELETE FROM journey")
  void truncate();

}
