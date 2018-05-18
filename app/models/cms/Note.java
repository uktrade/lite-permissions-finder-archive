package models.cms;

import models.cms.enums.NoteType;

public class Note {
  private Long id;
  private Long stageId;
  private String noteText;
  private NoteType noteType;

  public Long getId() {
    return id;
  }

  public Note setId(Long id) {
    this.id = id;
    return this;
  }

  public Long getStageId() {
    return stageId;
  }

  public Note setStageId(Long stageId) {
    this.stageId = stageId;
    return this;
  }

  public String getNoteText() {
    return noteText;
  }

  public Note setNoteText(String noteText) {
    this.noteText = noteText;
    return this;
  }

  public NoteType getNoteType() {
    return noteType;
  }

  public Note setNoteType(NoteType noteType) {
    this.noteType = noteType;
    return this;
  }
}
