package triage.text;

import java.util.List;

public interface HtmlRenderService {

  String convertRichText(RichText richText, boolean html);

  String convertRichTextToHtmlWithoutLinks(RichText richText);

  String createRelatedItemsHtml(List<RichText> richTextList);

  String createDefinitions(List<RichText> richTextList);
}
