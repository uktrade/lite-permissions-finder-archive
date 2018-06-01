package triage.text;

public interface RichTextParser {

  RichText parseForStage(String text, String stageId);

  RichText parseForControlEntry(String text, String controlEntryId);

}
