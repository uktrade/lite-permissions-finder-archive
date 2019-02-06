package components.cms.dao;

import models.cms.Note;

import java.util.List;

public interface NoteDao {

  List<Note> getNotesForStageId(long stageId);

  Long insertNote(Note note);

  void insertMultiple(List<Note> notes);

  void deleteAllNotes();

}
