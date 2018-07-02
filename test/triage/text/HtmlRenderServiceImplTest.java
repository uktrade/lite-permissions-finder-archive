package triage.text;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;

import org.junit.Test;
import triage.config.DefinitionConfigService;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public class HtmlRenderServiceImplTest {

  private final HtmlRenderServiceImpl htmlRenderServiceImpl = new HtmlRenderServiceImpl(mock(DefinitionConfigService.class));

  @Test
  public void renderOneLevelListTest() {
    String text = "*Item 1\n*Item 2\n*Item 3\n";
    String html = htmlRenderServiceImpl.convertRichTextToHtml(new RichText(text));

    assertThat(html).isEqualTo("<ul><li>Item 1</li><li>Item 2</li><li>Item 3</li></ul>");
  }

  @Test
  public void renderTwoLevelListTest() {
    String text = "*Item 1\n**Part A\n**Part B\n**Part C\n";
    String html = htmlRenderServiceImpl.convertRichTextToHtml(new RichText(text));

    assertThat(html).isEqualTo("<ul><li>Item 1</li><ul><li>Part A</li><li>Part B</li><li>Part C</li></ul></ul>");
  }

  @Test
  public void renderThreeLevelListTest() {
    String text = "*Item 1\n**Part A\n***Info (i)\n*** Info (ii)\n***Info (iii)\n";
    String html = htmlRenderServiceImpl.convertRichTextToHtml(new RichText(text));

    assertThat(html).isEqualTo(
        "<ul><li>Item 1</li><ul><li>Part A</li><ul><li>Info (i)</li><li> Info (ii)</li><li>Info (iii)</li></ul></ul></ul>");
  }

  @Test
  public void textBeforeAndAfterListTest() {
    String text = "text before\n\n*Item1\n**Part A\n***Info (i)\n\ntext after";
    String html = htmlRenderServiceImpl.convertRichTextToHtml(new RichText(text));

    assertThat(html).isEqualTo(
        "<p>text before</p><ul><li>Item1</li><ul><li>Part A</li><ul><li>Info (i)</li></ul></ul></ul><p>text after</p>");
  }

  @Test
  public void listBackAndForthTest() {
    String text = "*1\n**A\n***(i)\n***(ii)\n**B\n**C\n***(iv)\n*2\n";
    String html = htmlRenderServiceImpl.convertRichTextToHtml(new RichText(text));

    assertThat(html).isEqualTo(
        "<ul><li>1</li><ul><li>A</li><ul><li>(i)</li><li>(ii)</li></ul><li>B</li><li>C</li><ul><li>(iv)</li></ul></ul><li>2</li></ul>");
  }

  @Test
  public void controlEntryTest() {
    ControlEntryReferenceNode controlEntryReferenceNode = new ControlEntryReferenceNode("This is control code ML1", "ML1");
    String html = htmlRenderServiceImpl.convertRichTextToHtml(new RichText(Collections.singletonList(controlEntryReferenceNode)));

    assertThat(html).isEqualTo(unescape(
        "<a href='/view-control-entry/ML1' data-control-entry-id='ML1' title='View This is control code ML1' target='_blank'>This is control code ML1</a>"));
  }

  @Test
  public void globalDefinitionTest() {
    DefinitionReferenceNode definitionReferenceNode = new DefinitionReferenceNode("\"laser\"", "123", true);
    String html = htmlRenderServiceImpl.convertRichTextToHtml(new RichText(Collections.singletonList(definitionReferenceNode)));

    assertThat(html).isEqualTo(unescape(
        "<a href='/view-definition/global/123' data-definition-id='123' data-definition-type='global' title='View definition of &quot;laser&quot;' target='_blank'>laser</a>"));
  }

  @Test
  public void localDefinitionTest() {
    DefinitionReferenceNode definitionReferenceNode = new DefinitionReferenceNode("'laser'", "123", false);
    String html = htmlRenderServiceImpl.convertRichTextToHtml(new RichText(Collections.singletonList(definitionReferenceNode)));

    assertThat(html).isEqualTo(unescape(
        "<a href='/view-definition/local/123' data-definition-id='123' data-definition-type='local' title='View definition of &quot;laser&quot;' target='_blank'>laser</a>"));
  }

  @Test
  public void definitionLinkTargetOmittedTest() {
    DefinitionReferenceNode definitionReferenceNode = new DefinitionReferenceNode("'laser'", "123", false);
    RichText richText = new RichText(Collections.singletonList(definitionReferenceNode));
    String html = htmlRenderServiceImpl.convertRichTextToHtml(richText, HtmlRenderOption.OMIT_LINK_TARGET_ATTR);

    assertThat(html).isEqualTo(unescape(
        "<a href='/view-definition/local/123' data-definition-id='123' data-definition-type='local' title='View definition of &quot;laser&quot;'>laser</a>"));
  }

  @Test
  public void definitionLinksOmittedTest() {
    DefinitionReferenceNode definitionReferenceNode = new DefinitionReferenceNode("'laser'", "123", false);
    RichText richText = new RichText(Collections.singletonList(definitionReferenceNode));
    String html = htmlRenderServiceImpl.convertRichTextToHtml(richText, HtmlRenderOption.OMIT_LINKS);

    assertThat(html).isEqualTo("'laser'");
  }

  @Test
  public void controlEntryLinkTest() {
    ControlEntryReferenceNode controlEntryReferenceNode = new ControlEntryReferenceNode("Code ML1", "ML1");
    RichText richText = new RichText(Collections.singletonList(controlEntryReferenceNode));
    String html = htmlRenderServiceImpl.convertRichTextToHtml(richText);

    assertThat(html).isEqualTo(unescape(
        "<a href='/view-control-entry/ML1' data-control-entry-id='ML1' title='View Code ML1' target='_blank'>Code ML1</a>"));
  }

  @Test
  public void controlEntryLinkTargetOmittedTest() {
    ControlEntryReferenceNode controlEntryReferenceNode = new ControlEntryReferenceNode("Code ML1", "ML1");
    RichText richText = new RichText(Collections.singletonList(controlEntryReferenceNode));
    String html = htmlRenderServiceImpl.convertRichTextToHtml(richText, HtmlRenderOption.OMIT_LINK_TARGET_ATTR);

    assertThat(html).isEqualTo(unescape(
                "<a href='/view-control-entry/ML1' data-control-entry-id='ML1' title='View Code ML1'>Code ML1</a>"));
  }

  @Test
  public void controlEntryLinksOmittedTest() {
    ControlEntryReferenceNode controlEntryReferenceNode = new ControlEntryReferenceNode("Code ML1", "ML1");
    RichText richText = new RichText(Collections.singletonList(controlEntryReferenceNode));
    String html = htmlRenderServiceImpl.convertRichTextToHtml(richText, HtmlRenderOption.OMIT_LINKS);

    assertThat(html).isEqualTo("Code ML1");
  }

  @Test
  public void textTest() {
    SimpleTextNode simpleTextNode = new SimpleTextNode("This is text.");
    String html = htmlRenderServiceImpl.convertRichTextToHtml(new RichText(Collections.singletonList(simpleTextNode)));

    assertThat(html).isEqualTo("This is text.");
  }

  @Test
  public void textWithNewlineTest() {
    SimpleTextNode simpleTextNode = new SimpleTextNode("This is line 1.\nThis is line 2");
    String html = htmlRenderServiceImpl.convertRichTextToHtml(new RichText(Collections.singletonList(simpleTextNode)));
    assertThat(html).isEqualTo("<p>This is line 1.<br>This is line 2</p>");
  }

  @Test
  public void textWithMultipleNewlinesTest() {
    SimpleTextNode simpleTextNode = new SimpleTextNode("This is line 1.\n\nThis is line 2");
    String html = htmlRenderServiceImpl.convertRichTextToHtml(new RichText(Collections.singletonList(simpleTextNode)));
    assertThat(html).isEqualTo("<p>This is line 1.</p><p>This is line 2</p>");
  }

  @Test
  public void modelContentLinkTest() {
    ModalContentLinkNode modalContentLinkNode = new ModalContentLinkNode("example", "exampleId");
    RichText richText = new RichText(Collections.singletonList(modalContentLinkNode));
    String html = htmlRenderServiceImpl.convertRichTextToHtml(richText);
    assertThat(html).isEqualTo(unescape(
        "<a href='/view-modal-content/exampleId' data-modal-content-id='exampleId' title='View example'>example</a>"));
  }

  @Test
  public void convertRichTextToHtmlTest() {
    ControlEntryReferenceNode ml1 = new ControlEntryReferenceNode("Code ML1", "ML1");
    ControlEntryReferenceNode ml2 = new ControlEntryReferenceNode("Code ML2", "ML2");
    DefinitionReferenceNode laser = new DefinitionReferenceNode("\"laser\"", "123", true);
    DefinitionReferenceNode radio = new DefinitionReferenceNode("radio", "abc", true);
    ModalContentLinkNode example = new ModalContentLinkNode("example", "exampleId");
    SimpleTextNode text1 = new SimpleTextNode("This is text 1\n\n");
    SimpleTextNode text2 = new SimpleTextNode("This is text 2 \nwith newline\n\n");
    SimpleTextNode list1 = new SimpleTextNode("*1\n**A\n**B\n***(i)\n***(ii)\n\n");
    SimpleTextNode list2 = new SimpleTextNode("*a\n*b\n*c\n\n");
    List<RichTextNode> richTextNodes = Arrays.asList(ml1, laser, text1, list1, ml2, radio, text2, list2, example);
    String html = htmlRenderServiceImpl.convertRichTextToHtml(new RichText(richTextNodes));

    assertThat(html)
        .isEqualTo(
            unescape(
                "<p><a href='/view-control-entry/ML1' data-control-entry-id='ML1' title='View Code ML1' target='_blank'>Code ML1</a>"
                    + "<a href='/view-definition/global/123' data-definition-id='123' data-definition-type='global' title='View definition of &quot;laser&quot;' target='_blank'>laser</a>"
                    + "This is text 1</p>"
                    + "<ul><li>1</li><ul><li>A</li><li>B</li><ul><li>(i)</li><li>(ii)</li></ul></ul></ul>"
                    + "<p><a href='/view-control-entry/ML2' data-control-entry-id='ML2' title='View Code ML2' target='_blank'>Code ML2</a>"
                    + "<a href='/view-definition/global/abc' data-definition-id='abc' data-definition-type='global' title='View definition of &quot;radio&quot;' target='_blank'>radio</a>"
                    + "This is text 2 <br>with newline</p>"
                    + "<ul><li>a</li><li>b</li><li>c</li></ul>"
                    + "<p><a href='/view-modal-content/exampleId' data-modal-content-id='exampleId' title='View example'>example</a></p>"));
  }

  private String unescape(String str) {
    return str.replace("'", "\"");
  }

}
