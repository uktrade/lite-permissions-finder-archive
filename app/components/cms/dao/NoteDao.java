package components.cms.dao;

import models.cms.Note;

import java.util.List;

public interface NoteDao {

  List<Note> getNotesForStageId(long stageId);

  Long insert(Note note);

  void insert(List<Note> notes);

  void deleteAllNotes();

}
