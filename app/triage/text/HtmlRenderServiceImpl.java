package triage.text;

import com.google.inject.Inject;
import components.cms.dao.GlobalDefinitionDao;
import components.cms.dao.LocalDefinitionDao;
import models.cms.GlobalDefinition;
import models.cms.LocalDefinition;
import models.enums.HtmlType;
import org.apache.commons.lang3.StringUtils;

import java.util.ArrayList;
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
      "<a href='/view-definition/%s' data-definition-id='%s' title='View definition of &quot;%s&quot;' target='_blank'>%s</a>");
  private static final String CONTROL_ENTRY_TEXT = unescape(
      "<a href='view-control-entry/%s' data-control-entry-id='%s' title='View %s' target='_blank'>%s</a>");
  private static final Set<HtmlType> LEVELS = EnumSet.of(HtmlType.LIST_LEVEL_1, HtmlType.LIST_LEVEL_2, HtmlType.LIST_LEVEL_3);
  private static final Pattern PATTERN_LEVEL_1 = Pattern.compile("\\*(?!\\*)(.*?)\\n");
  private static final Pattern PATTERN_LEVEL_2 = Pattern.compile("\\*\\*(.*?)\\n");
  private static final Pattern PATTERN_LEVEL_3 = Pattern.compile("\\*\\*\\*(.*?)\\n");

  private final GlobalDefinitionDao globalDefinitionDao;
  private final LocalDefinitionDao localDefinitionDao;

  @Inject
  public HtmlRenderServiceImpl(GlobalDefinitionDao globalDefinitionDao, LocalDefinitionDao localDefinitionDao) {
    this.globalDefinitionDao = globalDefinitionDao;
    this.localDefinitionDao = localDefinitionDao;
  }

  @Override
  public String convertRichText(RichText richText, boolean html) {
    if (html) {
      return convertNewlinesToBrs(renderLists(addLinks(richText)));
    } else {
      return convertRichTextToPlainText(richText);
    }
  }

  @Override
  public String convertRichTextToHtmlWithoutLinks(RichText richText) {
    return convertNewlinesToBrs(renderLists(convertRichTextToPlainText(richText)));
  }

  private String convertRichTextToPlainText(RichText richText) {
    return richText.getRichTextNodes().stream().map(RichTextNode::getTextContent).collect(Collectors.joining());
  }

  @Override
  public String createRelatedItemsHtml(List<RichText> richTextList) {
    return richTextList.stream()
        .map(RichText::getRichTextNodes)
        .flatMap(Collection::stream)
        .filter(richTextNode -> richTextNode instanceof ControlEntryReferenceNode)
        .map(richTextNode -> (ControlEntryReferenceNode) richTextNode)
        .sorted(Comparator.comparing(ControlEntryReferenceNode::getTextContent))
        .map(this::createControlEntryHtml)
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

  private String convertNewlinesToBrs(String input) {
    return input.replace("\n", "<br>");
  }

  private String addLinks(RichText richText) {
    StringBuilder stringBuilder = new StringBuilder();
    for (RichTextNode richTextNode : richText.getRichTextNodes()) {
      if (richTextNode instanceof DefinitionReferenceNode) {
        DefinitionReferenceNode definitionReferenceNode = (DefinitionReferenceNode) richTextNode;
        String definitionId = definitionReferenceNode.getReferencedDefinitionId();
        //Strip leading/trailing quote characters from the original string when generating a link as per screen designs
        String textContent = StringUtils.strip(definitionReferenceNode.getTextContent(), "\"'");
        String html = String.format(DEFINITION_TEXT, definitionId, definitionId, textContent, textContent);
        stringBuilder.append(html);
      } else if (richTextNode instanceof ControlEntryReferenceNode) {
        ControlEntryReferenceNode controlEntryReferenceNode = (ControlEntryReferenceNode) richTextNode;
        String html = createControlEntryHtml(controlEntryReferenceNode);
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
      GlobalDefinition globalDefinition = globalDefinitionDao.getGlobalDefinition(Long.parseLong(definitionId));
      text = globalDefinition.getTerm();
    } else {
      LocalDefinition localDefinition = localDefinitionDao.getLocalDefinition(Long.parseLong(definitionId));
      text = localDefinition.getTerm();
    }
    return String.format(DEFINITION_TEXT, definitionId, definitionId, text, text);
  }

  private String createControlEntryHtml(ControlEntryReferenceNode controlEntryReferenceNode) {
    String controlEntryId = controlEntryReferenceNode.getControlEntryId();
    String textContent = controlEntryReferenceNode.getTextContent();
    return String.format(CONTROL_ENTRY_TEXT, controlEntryId, controlEntryId, textContent, textContent);
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
        stringBuilder.append("</ul>");
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

}
