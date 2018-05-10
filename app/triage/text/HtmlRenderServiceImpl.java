package triage.text;

import java.util.stream.Collectors;

public class HtmlRenderServiceImpl implements HtmlRenderService {
  @Override
  public String convertRichTextToHtml(RichText richText) {
    return null;
  }

  @Override
  public String convertRichTextToPlainText(RichText richText) {
    return richText.getRichTextNodes().stream().map(RichTextNode::getTextContent).collect(Collectors.joining());
  }
}
