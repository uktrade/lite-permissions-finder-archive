package triage.text;

import java.util.Collections;
import java.util.List;

public class RichText {

  private List<RichTextNode> richTextNodes;

  public RichText(String text) {
    richTextNodes = Collections.singletonList(new SimpleTextNode(text));
  }

  RichText(List<RichTextNode> richTextNodes) {
    this.richTextNodes = richTextNodes;
  }

  public List<RichTextNode> getRichTextNodes() {
    return richTextNodes;
  }

  public void setRichTextNodes(List<RichTextNode> richTextNodes) {
    this.richTextNodes = richTextNodes;
  }
}
