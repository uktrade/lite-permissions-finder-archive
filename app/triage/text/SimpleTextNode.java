package triage.text;

public class SimpleTextNode implements RichTextNode {

  private final String text;

  public SimpleTextNode(String text) {
    this.text = text;
  }

  @Override
  public String getTextContent() {
    return text;
  }
}
