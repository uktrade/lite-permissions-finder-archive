package triage.text;

public class ControlEntryReferenceNode implements RichTextNode {

  private final String originalText;
  private final String controlEntryId;

  public ControlEntryReferenceNode(String originalText, String controlEntryId) {
    this.originalText = originalText;
    this.controlEntryId = controlEntryId;
  }

  @Override
  public String getTextContent() {
    return originalText;
  }

  public String getControlEntryId() {
    return controlEntryId;
  }
}
