package triage.text;

import java.util.List;

public interface HtmlRenderService {

  String convertRichTextToPlainText(RichText richText);

  String convertRichTextToHtml(RichText richText, HtmlConversionOption... htmlConversionOptions);

  String createRelatedItemsHtml(List<RichText> richTextList);

  String createDefinitions(List<RichText> richTextList);
}
