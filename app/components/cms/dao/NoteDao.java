package components.cms.dao;

import models.cms.Note;

import java.util.List;

public interface NoteDao {

  Note getNote(long id);

  List<Note> getNotesForStageId(long stageId);

  Long insertNote(Note note);

  void deleteNote(long id);

  void deleteAllNotes();

}
