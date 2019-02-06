package components.cms.dao.impl;

import com.google.inject.Inject;
import components.cms.dao.NoteDao;
import components.cms.jdbi.NoteJDBIDao;
import models.cms.Note;
import org.skife.jdbi.v2.DBI;

import java.util.List;

public class NoteDaoImpl implements NoteDao {

  private final NoteJDBIDao noteJDBIDao;

  @Inject
  public NoteDaoImpl(DBI dbi) {
    this.noteJDBIDao = dbi.onDemand(NoteJDBIDao.class);
  }

  @Override
  public List<Note> getNotesForStageId(long stageId) {
    return noteJDBIDao.getForStageId(stageId);
  }

  @Override
  public Long insertNote(Note note) {
    return noteJDBIDao.insert(
        note.getStageId(),
        note.getNoteText(),
        note.getNoteType());
  }

  @Override
  public void insertMultiple(List<Note> notes) {
    noteJDBIDao.insertMultiple(notes);
  }

  @Override
  public void deleteAllNotes() {
    noteJDBIDao.truncate();
  }
}
