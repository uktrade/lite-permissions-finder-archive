package components.cms.jdbi;

import components.cms.mapper.NoteRSMapper;
import models.cms.Note;
import models.cms.enums.NoteType;
import org.skife.jdbi.v2.sqlobject.Bind;
import org.skife.jdbi.v2.sqlobject.BindBean;
import org.skife.jdbi.v2.sqlobject.SqlBatch;
import org.skife.jdbi.v2.sqlobject.SqlQuery;
import org.skife.jdbi.v2.sqlobject.SqlUpdate;
import org.skife.jdbi.v2.sqlobject.customizers.Mapper;

import java.util.List;

public interface NoteJDBIDao {

  @Mapper(NoteRSMapper.class)
  @SqlQuery("SELECT * FROM note WHERE stage_id = :stageId")
  List<Note> getForStageId(@Bind("stageId") long stageId);

  @SqlQuery(
      "INSERT INTO note (stage_id, note_text, note_type) " +
          "VALUES (:stageId, :noteText, :noteType) " +
          "RETURNING id")
  Long insert(@Bind("stageId") Long stageId, @Bind("noteText") String noteText, @Bind("noteType") NoteType noteType);

  @SqlBatch(
    "INSERT INTO note (stage_id, note_text, note_type) VALUES (:stageId, :noteText, :noteType)")
  void insert(@BindBean List<Note> notes);

  @SqlUpdate("DELETE FROM note")
  void truncate();

}
