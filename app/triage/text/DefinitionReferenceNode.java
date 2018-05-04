package triage.text;

public class DefinitionReferenceNode implements RichTextNode {

  private final String originalText; //to preserve original case
  private final String referencedDefinition;
  private final boolean isGlobal;

  public DefinitionReferenceNode(String originalText, String referencedDefinition, boolean isGlobal) {
    this.originalText = originalText;
    this.referencedDefinition = referencedDefinition;
    this.isGlobal = isGlobal;
  }

  @Override
  public String getTextContent() {
    return originalText;
  }

  public String getReferencedDefinition() {
    return referencedDefinition;
  }

  public boolean isGlobal() {
    return isGlobal;
  }
}
