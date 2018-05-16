package components.cms.dao.impl;

import com.google.inject.Inject;
import components.cms.dao.NoteDao;
import components.cms.jdbi.NoteJDBIDao;
import models.cms.Note;
import org.skife.jdbi.v2.DBI;

public class NoteDaoImpl implements NoteDao {

  private final NoteJDBIDao noteJDBIDao;

  @Inject
  public NoteDaoImpl(DBI dbi) {
    this.noteJDBIDao = dbi.onDemand(NoteJDBIDao.class);
  }

  @Override
  public Note getNote(long id) {
    return noteJDBIDao.get(id);
  }

  @Override
  public Long insertNote(Note note) {
    return noteJDBIDao.insert(
        note.getStageId(),
        note.getNoteText(),
        note.getNoteType());
  }

  @Override
  public void deleteNote(long id) {
    noteJDBIDao.delete(id);
  }

  @Override
  public void deleteAllNotes() {
    noteJDBIDao.truncate();
  }
}
