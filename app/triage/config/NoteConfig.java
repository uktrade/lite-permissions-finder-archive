package triage.config;

import models.cms.enums.NoteType;
import triage.text.RichText;

public class NoteConfig {

  private final String noteId;
  private final String stageId;

  private final RichText noteText;
  private final NoteType noteType;

  public NoteConfig(String noteId, String stageId, RichText noteText, NoteType noteType) {
    this.noteId = noteId;
    this.stageId = stageId;
    this.noteText = noteText;
    this.noteType = noteType;
  }

  public String getNoteId() {
    return noteId;
  }

  public String getStageId() {
    return stageId;
  }

  public RichText getNoteText() {
    return noteText;
  }

  public NoteType getNoteType() {
    return noteType;
  }
}
