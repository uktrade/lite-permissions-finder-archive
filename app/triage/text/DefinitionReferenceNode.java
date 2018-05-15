package triage.text;

public class DefinitionReferenceNode implements RichTextNode {

  private final String originalText; //to preserve original case
  private final String referencedDefinitionId;
  private final boolean isGlobal;

  DefinitionReferenceNode(String originalText, String referencedDefinitionId, boolean isGlobal) {
    this.originalText = originalText;
    this.referencedDefinitionId = referencedDefinitionId;
    this.isGlobal = isGlobal;
  }

  @Override
  public String getTextContent() {
    return originalText;
  }

  public String getReferencedDefinitionId() {
    return referencedDefinitionId;
  }

  public boolean isGlobal() {
    return isGlobal;
  }
}
