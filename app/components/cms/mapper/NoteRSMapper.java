package components.cms.mapper;

import components.cms.mapper.util.ResultSetWrapper;
import models.cms.Note;
import models.cms.enums.NoteType;
import org.apache.commons.lang3.EnumUtils;
import org.skife.jdbi.v2.StatementContext;
import org.skife.jdbi.v2.tweak.ResultSetMapper;

import java.sql.ResultSet;
import java.sql.SQLException;

public class NoteRSMapper implements ResultSetMapper<Note> {
  @Override
  public Note map(int index, ResultSet r, StatementContext ctx) throws SQLException {
    ResultSetWrapper rsw = new ResultSetWrapper(r);
    Long id = rsw.getLong("id");
    Long stageId = rsw.getLong("stage_id");
    String noteText = rsw.getString("note_text");
    NoteType noteType = EnumUtils.getEnum(NoteType.class, rsw.getString("note_type"));
    return new Note()
        .setId(id)
        .setStageId(stageId)
        .setNoteText(noteText)
        .setNoteType(noteType);
  }
}
