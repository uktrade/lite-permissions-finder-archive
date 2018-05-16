package components.cms.jdbi;

import components.cms.mapper.JourneyRSMapper;
import models.cms.Journey;
import org.skife.jdbi.v2.sqlobject.Bind;
import org.skife.jdbi.v2.sqlobject.SqlQuery;
import org.skife.jdbi.v2.sqlobject.SqlUpdate;
import org.skife.jdbi.v2.sqlobject.customizers.Mapper;

public interface JourneyJDBIDao {

  @Mapper(JourneyRSMapper.class)
  @SqlQuery("SELECT * FROM journey WHERE id = :id")
  Journey get(@Bind("id") long id);

  @SqlQuery(
      "INSERT INTO journey (journey_name) " +
          "VALUES (:journeyName) " +
          "RETURNING id")
  Long insert(@Bind("journeyName") String journeyName);

  @SqlUpdate("DELETE FROM journey WHERE id = :id")
  void delete(@Bind("id") long id);

  @SqlUpdate("DELETE FROM journey")
  void truncate();

}
