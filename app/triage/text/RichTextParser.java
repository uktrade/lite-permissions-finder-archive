package triage.text;

public interface RichTextParser {

  RichText parseForStage(String text, String stageId, String journeyId);

  RichText parseForControlEntry(String text, String controlEntryId, String journeyId);

}
