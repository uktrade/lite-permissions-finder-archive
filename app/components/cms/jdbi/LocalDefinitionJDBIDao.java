package components.cms.jdbi;

import components.cms.mapper.LocalDefinitionRSMapper;
import models.cms.LocalDefinition;
import org.skife.jdbi.v2.sqlobject.Bind;
import org.skife.jdbi.v2.sqlobject.SqlQuery;
import org.skife.jdbi.v2.sqlobject.SqlUpdate;
import org.skife.jdbi.v2.sqlobject.customizers.Mapper;

public interface LocalDefinitionJDBIDao {

  @Mapper(LocalDefinitionRSMapper.class)
  @SqlQuery("SELECT * FROM local_definition WHERE id = :id")
  LocalDefinition get(@Bind("id") long id);

  @SqlQuery(
      "INSERT INTO local_definition (control_entry_id, term, definition_text) " +
          "VALUES (:controlEntryId, :term, :definitionText) " +
          "RETURNING id")
  Long insert(@Bind("controlEntryId") Long controlEntryId, @Bind("term") String term, @Bind("definitionText") String definitionText);

  @SqlUpdate("DELETE FROM local_definition WHERE id = :id")
  void delete(@Bind("id") long id);

  @SqlUpdate("DELETE FROM local_definition")
  void truncate();

}
