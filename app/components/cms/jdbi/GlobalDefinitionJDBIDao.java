package components.cms.jdbi;

import components.cms.mapper.GlobalDefinitionRSMapper;
import models.cms.GlobalDefinition;
import org.skife.jdbi.v2.sqlobject.Bind;
import org.skife.jdbi.v2.sqlobject.SqlQuery;
import org.skife.jdbi.v2.sqlobject.SqlUpdate;
import org.skife.jdbi.v2.sqlobject.customizers.Mapper;

import java.util.List;

public interface GlobalDefinitionJDBIDao {

  @Mapper(GlobalDefinitionRSMapper.class)
  @SqlQuery("SELECT * FROM global_definition WHERE id = :id")
  GlobalDefinition get(@Bind("id") long id);

  @Mapper(GlobalDefinitionRSMapper.class)
  @SqlQuery("SELECT * FROM global_definition WHERE LOWER(term) = LOWER(:term)")
  GlobalDefinition getByTerm(@Bind("term") String term);

  @SqlQuery(
      "INSERT INTO global_definition (journey_id, term, definition_text) " +
          "VALUES (:journeyId, :term, :definitionText) " +
          "RETURNING id")
  Long insert(@Bind("journeyId") Long journeyId, @Bind("term") String term,
              @Bind("definitionText") String definitionText);

  @SqlQuery("SELECT id FROM global_definition")
  List<Long> getAllIds();

  @SqlUpdate("DELETE FROM global_definition")
  void truncate();

}
