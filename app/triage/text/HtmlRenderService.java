package triage.text;

import play.twirl.api.Html;
import triage.config.ControlEntryConfig;

import java.util.List;

public interface HtmlRenderService {

  String convertRichTextToPlainText(RichText richText);

  String convertRichTextToHtml(RichText richText, HtmlRenderOption... htmlRenderOptions);

  String createRelatedItemsHtml(List<RichText> richTextList, HtmlRenderOption... htmlRenderOptions);

  String createDefinitions(List<RichText> richTextList);

  Html createControlEntryLinkHtml(ControlEntryConfig controlEntryConfig);

}
