package triage.text;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.Test;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public class HtmlRenderServiceImplTest {

  private final HtmlRenderServiceImpl htmlRenderServiceImpl = new HtmlRenderServiceImpl();

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
    String text = "text before *Item1\n**Part A\n***Info (i)\ntext after";
    String html = htmlRenderServiceImpl.convertRichTextToHtml(new RichText(text));

    assertThat(html).isEqualTo(
        "text before <ul><li>Item1</li><ul><li>Part A</li><ul><li>Info (i)</li></ul></ul></ul>text after");
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
    ControlEntryReferenceNode controlEntryReferenceNode = new ControlEntryReferenceNode("This is control code M1A", "ML1");
    String html = htmlRenderServiceImpl.convertRichTextToHtml(new RichText(Collections.singletonList(controlEntryReferenceNode)));

    assertThat(html).isEqualTo(unescape(
        "<a href='view-control-entry/ML1' data-control-entry-id='ML1' target='_blank'>This is control code M1A</a>"));
  }

  @Test
  public void globalDefinitionTest() {
    DefinitionReferenceNode definitionReferenceNode = new DefinitionReferenceNode("\"laser\"", "123", true);
    String html = htmlRenderServiceImpl.convertRichTextToHtml(new RichText(Collections.singletonList(definitionReferenceNode)));

    assertThat(html).isEqualTo(unescape(
        "<a href='/view-definition/123' data-definition-id='123' target='_blank'>laser</a>"));
  }

  @Test
  public void localDefinitionTest() {
    DefinitionReferenceNode definitionReferenceNode = new DefinitionReferenceNode("'laser'", "123", true);
    String html = htmlRenderServiceImpl.convertRichTextToHtml(new RichText(Collections.singletonList(definitionReferenceNode)));

    assertThat(html).isEqualTo(unescape(
        "<a href='/view-definition/123' data-definition-id='123' target='_blank'>laser</a>"));
  }

  @Test
  public void textTest() {
    SimpleTextNode simpleTextNode = new SimpleTextNode("This is text.");
    String html = htmlRenderServiceImpl.convertRichTextToHtml(new RichText(Collections.singletonList(simpleTextNode)));

    assertThat(html).isEqualTo("This is text.");
  }

  @Test
  public void convertRichTextToHtmlTest() {
    ControlEntryReferenceNode ml1 = new ControlEntryReferenceNode("Code M1A", "ML1");
    ControlEntryReferenceNode ml2 = new ControlEntryReferenceNode("Code ML2", "ML2");
    DefinitionReferenceNode laser = new DefinitionReferenceNode("\"laser\"", "123", true);
    DefinitionReferenceNode radio = new DefinitionReferenceNode("radio", "abc", true);
    SimpleTextNode text1 = new SimpleTextNode("This is text 1");
    SimpleTextNode text2 = new SimpleTextNode("This is text 2");
    SimpleTextNode list1 = new SimpleTextNode("*1\n**A\n**B\n***(i)\n***(ii)\n");
    SimpleTextNode list2 = new SimpleTextNode("*a\n*b\n*c\n");
    List<RichTextNode> richTextNodes = Arrays.asList(ml1, laser, text1, list1, ml2, radio, text2, list2);
    String html = htmlRenderServiceImpl.convertRichTextToHtml(new RichText(richTextNodes));

    assertThat(html).isEqualTo(unescape(
        "<a href='view-control-entry/ML1' data-control-entry-id='ML1' target='_blank'>Code M1A</a>" +
            "<a href='/view-definition/123' data-definition-id='123' target='_blank'>laser</a>" +
            "This is text 1" +
            "<ul><li>1</li><ul><li>A</li><li>B</li><ul><li>(i)</li><li>(ii)</li></ul></ul></ul>" +
            "<a href='view-control-entry/ML2' data-control-entry-id='ML2' target='_blank'>Code ML2</a>" +
            "<a href='/view-definition/abc' data-definition-id='abc' target='_blank'>radio</a>" +
            "This is text 2" +
            "<ul><li>a</li><li>b</li><li>c</li></ul>"));
  }

  private String unescape(String str) {
    return str.replace("'", "\"");
  }

}
