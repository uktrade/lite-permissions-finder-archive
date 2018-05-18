package components.cms.mapper;

import components.cms.mapper.util.ResultSetWrapper;
import models.cms.LocalDefinition;
import org.skife.jdbi.v2.StatementContext;
import org.skife.jdbi.v2.tweak.ResultSetMapper;

import java.sql.ResultSet;
import java.sql.SQLException;

public class LocalDefinitionRSMapper implements ResultSetMapper<LocalDefinition> {
  @Override
  public LocalDefinition map(int index, ResultSet r, StatementContext ctx) throws SQLException {
    ResultSetWrapper rsw = new ResultSetWrapper(r);
    Long id = rsw.getLong("id");
    Long controlEntryId = rsw.getLong("control_entry_id");
    String term = rsw.getString("term");
    String definitionText = rsw.getString("definition_text");
    return new LocalDefinition()
        .setId(id)
        .setControlEntryId(controlEntryId)
        .setTerm(term)
        .setDefinitionText(definitionText);
  }
}
