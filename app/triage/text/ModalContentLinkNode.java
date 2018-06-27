package triage.text;

public class ModalContentLinkNode implements RichTextNode {

  private final String linkText;
  private final String contentId;

  public ModalContentLinkNode(String linkText, String contentId) {
    this.linkText = linkText;
    this.contentId = contentId;
  }

  @Override
  public String getTextContent() {
    return linkText;
  }

  public String getLinkText() {
    return linkText;
  }

  public String getContentId() {
    return contentId;
  }
}
