package triage.text;

import com.google.inject.Inject;
import models.enums.HtmlType;
import org.apache.commons.lang3.StringUtils;
import triage.config.DefinitionConfig;
import triage.config.DefinitionConfigService;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.EnumSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

public class HtmlRenderServiceImpl implements HtmlRenderService {

  private static final String DEFINITION_TEXT = unescape(
      "<a href='/view-definition/%s/%s' data-definition-id='%s' data-definition-type='%s' " +
          "title='View definition of &quot;%s&quot;'%s>%s</a>");
  private static final String CONTROL_ENTRY_TEXT = unescape(
      "<a href='/view-control-entry/%s' data-control-entry-id='%s' title='View %s'%s>%s</a>");
  private static final String TARGET_ATTR_BLANK = unescape(" target='_blank'");
  private static final Set<HtmlType> LEVELS = EnumSet.of(HtmlType.LIST_LEVEL_1, HtmlType.LIST_LEVEL_2, HtmlType.LIST_LEVEL_3);
  private static final Pattern PATTERN_LEVEL_1 = Pattern.compile("\\*(?!\\*)(.*?)(\\n|$)");
  private static final Pattern PATTERN_LEVEL_2 = Pattern.compile("\\*\\*(.*?)(\\n|$)");
  private static final Pattern PATTERN_LEVEL_3 = Pattern.compile("\\*\\*\\*(.*?)(\\n|$)");
  private static final Pattern PATTERN_UL_IN_P = Pattern.compile("<p>\\s*?(<ul>.*?<\\/ul>)\\s*?<\\/p>", Pattern.MULTILINE);

  private final DefinitionConfigService definitionConfigService;

  @Inject
  public HtmlRenderServiceImpl(DefinitionConfigService definitionConfigService) {
    this.definitionConfigService = definitionConfigService;
  }

  @Override
  public String convertRichTextToPlainText(RichText richText) {
    return richText.getRichTextNodes().stream().map(RichTextNode::getTextContent).collect(Collectors.joining());
  }

  @Override
  public String convertRichTextToHtml(RichText richText, HtmlRenderOption... htmlRenderOptions) {
    if (getOption(HtmlRenderOption.OMIT_LINKS, htmlRenderOptions)) {
      return convertToParagraphs(renderLists(convertRichTextToPlainText(richText)));
    } else {
      boolean omitLinkTargetAttr = getOption(HtmlRenderOption.OMIT_LINK_TARGET_ATTR, htmlRenderOptions);
      return convertToParagraphs(renderLists(addLinks(richText, omitLinkTargetAttr)));
    }
  }

  @Override
  public String createRelatedItemsHtml(List<RichText> richTextList, HtmlRenderOption... htmlRenderOptions) {
    boolean omitLinkTargetAttr = getOption(HtmlRenderOption.OMIT_LINK_TARGET_ATTR, htmlRenderOptions);
    return richTextList.stream()
        .map(RichText::getRichTextNodes)
        .flatMap(Collection::stream)
        .filter(richTextNode -> richTextNode instanceof ControlEntryReferenceNode)
        .map(richTextNode -> (ControlEntryReferenceNode) richTextNode)
        .sorted(Comparator.comparing(ControlEntryReferenceNode::getTextContent))
        .map(controlEntryReferenceNode -> createControlEntryHtml(controlEntryReferenceNode, omitLinkTargetAttr))
        .distinct()
        .collect(Collectors.joining(", "));
  }

  @Override
  public String createDefinitions(List<RichText> richTextList) {
    return richTextList.stream()
        .map(RichText::getRichTextNodes)
        .flatMap(Collection::stream)
        .filter(richTextNode -> richTextNode instanceof DefinitionReferenceNode)
        .map(richTextNode -> (DefinitionReferenceNode) richTextNode)
        .sorted(Comparator.comparing(DefinitionReferenceNode::getTextContent))
        .map(this::createDefinitionHtml)
        .distinct()
        .collect(Collectors.joining(", "));
  }

  private static String unescape(String str) {
    return str.replace("'", "\"");
  }

  private String convertToParagraphs(String input) {
    String output = input.trim();

    if (output.contains("\n")) {
      output = "<p>" + output + "</p>";
      output = output.replace("\n", "<br>");
      output = output.replace("<br><br>", "</p><p>");
      output = PATTERN_UL_IN_P.matcher(output).replaceAll("$1");
    }

    return output;
  }

  private String addLinks(RichText richText, boolean omitLinkTargetAttr) {
    StringBuilder stringBuilder = new StringBuilder();
    for (RichTextNode richTextNode : richText.getRichTextNodes()) {
      if (richTextNode instanceof DefinitionReferenceNode) {
        DefinitionReferenceNode definitionReferenceNode = (DefinitionReferenceNode) richTextNode;
        String definitionId = definitionReferenceNode.getReferencedDefinitionId();
        //Strip leading/trailing quote characters from the original string when generating a link as per screen designs
        String textContent = StringUtils.strip(definitionReferenceNode.getTextContent(), "\"'");
        String type = definitionReferenceNode.isGlobal() ? "global" : "local";
        String html;
        if (omitLinkTargetAttr) {
          html = String.format(DEFINITION_TEXT, type, definitionId, definitionId, type, textContent, "", textContent);
        } else {
          html = String.format(DEFINITION_TEXT, type, definitionId, definitionId, type, textContent, TARGET_ATTR_BLANK, textContent);
        }
        stringBuilder.append(html);
      } else if (richTextNode instanceof ControlEntryReferenceNode) {
        ControlEntryReferenceNode controlEntryReferenceNode = (ControlEntryReferenceNode) richTextNode;
        String html = createControlEntryHtml(controlEntryReferenceNode, omitLinkTargetAttr);
        stringBuilder.append(html);
      } else if (richTextNode instanceof SimpleTextNode) {
        stringBuilder.append(richTextNode.getTextContent());
      }
    }
    return stringBuilder.toString();
  }

  private String createDefinitionHtml(DefinitionReferenceNode definitionReferenceNode) {
    String definitionId = definitionReferenceNode.getReferencedDefinitionId();
    String text;
    if (definitionReferenceNode.isGlobal()) {
      DefinitionConfig definitionConfig = definitionConfigService.getGlobalDefinition(definitionId);
      text = definitionConfig.getTerm();
    } else {
      DefinitionConfig definitionConfig = definitionConfigService.getLocalDefinition(definitionId);
      text = definitionConfig.getTerm();
    }
    String type = definitionReferenceNode.isGlobal() ? "global" : "local";
    return String.format(DEFINITION_TEXT, type, definitionId, definitionId, type, text, TARGET_ATTR_BLANK, text);
  }

  private String createControlEntryHtml(ControlEntryReferenceNode controlEntryReferenceNode, boolean omitLinkTargetAttr) {
    String controlEntryId = controlEntryReferenceNode.getControlEntryId();
    String textContent = controlEntryReferenceNode.getTextContent();
    if (omitLinkTargetAttr) {
      return String.format(CONTROL_ENTRY_TEXT, controlEntryId, controlEntryId, textContent, "", textContent);
    } else {
      return String.format(CONTROL_ENTRY_TEXT, controlEntryId, controlEntryId, textContent, TARGET_ATTR_BLANK, textContent);
    }
  }

  private String renderLists(String input) {
    List<HtmlPart> htmlParts = parse(input, PATTERN_LEVEL_3, HtmlType.LIST_LEVEL_3)
        .stream()
        .map(htmlPart -> parseHtml(htmlPart, PATTERN_LEVEL_2, HtmlType.LIST_LEVEL_2))
        .flatMap(Collection::stream)
        .map(htmlPart -> parseHtml(htmlPart, PATTERN_LEVEL_1, HtmlType.LIST_LEVEL_1))
        .flatMap(Collection::stream)
        .collect(Collectors.toList());
    return addHtmlListElements(htmlParts);
  }

  private String addHtmlListElements(List<HtmlPart> htmlParts) {
    StringBuilder stringBuilder = new StringBuilder();
    AtomicBoolean level1 = new AtomicBoolean();
    AtomicBoolean level2 = new AtomicBoolean();
    AtomicBoolean level3 = new AtomicBoolean();
    for (HtmlPart htmlPart : htmlParts) {
      HtmlType htmlType = htmlPart.getHtmlType();
      if (htmlType == HtmlType.TEXT) {
        deselectList(stringBuilder, level1, level2, level3);
      } else if (htmlType == HtmlType.LIST_LEVEL_1) {
        deselectList(stringBuilder, level2, level3);
        selectList(stringBuilder, level1);
      } else if (htmlType == HtmlType.LIST_LEVEL_2) {
        deselectList(stringBuilder, level3);
        selectList(stringBuilder, level2);
      } else if (htmlType == HtmlType.LIST_LEVEL_3) {
        selectList(stringBuilder, level3);
      }
      addText(stringBuilder, htmlPart);
    }
    deselectList(stringBuilder, level1, level2, level3);
    return stringBuilder.toString();
  }

  private void addText(StringBuilder stringBuilder, HtmlPart htmlPart) {
    boolean isLevel = LEVELS.contains(htmlPart.getHtmlType());
    if (isLevel) {
      stringBuilder.append("<li>");
    }
    stringBuilder.append(htmlPart.getText());
    if (isLevel) {
      stringBuilder.append("</li>");
    }
  }

  private void selectList(StringBuilder stringBuilder, AtomicBoolean... atomicBooleans) {
    for (AtomicBoolean atomicBoolean : atomicBooleans) {
      if (!atomicBoolean.get()) {
        atomicBoolean.set(true);
        stringBuilder.append("<ul>");
      }
    }
  }

  private void deselectList(StringBuilder stringBuilder, AtomicBoolean... atomicBooleans) {
    for (AtomicBoolean atomicBoolean : atomicBooleans) {
      if (atomicBoolean.get()) {
        atomicBoolean.set(false);
        stringBuilder.append("</ul>\n");
      }
    }
  }


  private List<HtmlPart> parseHtml(HtmlPart htmlPart, Pattern pattern, HtmlType htmlType) {
    if (htmlPart.getHtmlType() == HtmlType.TEXT) {
      return parse(htmlPart.getText(), pattern, htmlType);
    } else {
      return Collections.singletonList(htmlPart);
    }
  }

  private List<HtmlPart> parse(String text, Pattern pattern, HtmlType htmlType) {
    List<HtmlPart> htmlParts = new ArrayList<>();
    Matcher matcher = pattern.matcher(text);
    int lastEndIndex = 0;
    while (matcher.find()) {
      if (matcher.start() > lastEndIndex) {
        String leadingText = text.substring(lastEndIndex, matcher.start());
        htmlParts.add(new HtmlPart(HtmlType.TEXT, leadingText));
      }
      htmlParts.add(new HtmlPart(htmlType, matcher.group(1)));
      lastEndIndex = matcher.end();
    }
    if (text.length() > lastEndIndex) {
      String trailingText = text.substring(lastEndIndex, text.length());
      htmlParts.add(new HtmlPart(HtmlType.TEXT, trailingText));
    }
    return htmlParts;
  }

  private boolean getOption(HtmlRenderOption option, HtmlRenderOption...options) {
    return Arrays.stream(options).anyMatch(htmlRenderOption -> option == htmlRenderOption);
  }
}
