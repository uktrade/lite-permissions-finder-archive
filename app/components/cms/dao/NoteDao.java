package components.cms.dao;

import models.cms.Note;

public interface NoteDao {

  Note getNote(long id);

  Long insertNote(Note note);

  void deleteNote(long id);

  void deleteAllNotes();

}
