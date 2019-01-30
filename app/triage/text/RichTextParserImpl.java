package triage.text;

import com.google.inject.Inject;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.function.Function;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class RichTextParserImpl implements RichTextParser {

  private static final Pattern GLOBAL_DEFINITION_PATTERN = Pattern.compile("\"(.*?)\"");
  private static final Pattern LOCAL_DEFINITION_PATTERN = Pattern.compile("\'(.*?)'");
  //only go to p to avoid picking "0Hz"
  private static final Pattern CONTROL_ENTRY_PATTERN = Pattern.compile("(?:ML|PL|[0-9][A-Z])[0-9a-p]+");
  private static final Pattern MODAL_CONTENT_LINK_PATTERN = Pattern.compile("\\[(.*?)\\]\\((.*?)\\)");

  private final ParserLookupService parserLookupService;

  @Inject
  public RichTextParserImpl(ParserLookupService parserLookupService) {
    this.parserLookupService = parserLookupService;
  }

  @Override
  public RichText parseForStage(String text, String journeyId) {
    List<RichTextNode> withModalContentLinks = parseModalContentLinks(Collections.singletonList(new SimpleTextNode(text)));
    List<RichTextNode> withGlobalDefinitions = parseGlobalDefinitions(withModalContentLinks, journeyId);
    List<RichTextNode> withControlEntries = parseControlEntries(withGlobalDefinitions);
    List<RichTextNode> withFlattenedSimpleTextNodes = flattenConsecutiveSimpleTextNodes(withControlEntries);
    return new RichText(withFlattenedSimpleTextNodes);
  }

  @Override
  public RichText parseForControlEntry(String text, String controlEntryId, String journeyId) {
    List<RichTextNode> withModalContentLinks = parseModalContentLinks(Collections.singletonList(new SimpleTextNode(text)));
    List<RichTextNode> withGlobalDefinitions = parseGlobalDefinitions(withModalContentLinks, journeyId);
    List<RichTextNode> withLocalDefinitions = parseLocalDefinitions(withGlobalDefinitions, controlEntryId);
    List<RichTextNode> withControlEntries = parseControlEntries(withLocalDefinitions);
    List<RichTextNode> withFlattenedSimpleTextNodes = flattenConsecutiveSimpleTextNodes(withControlEntries);
    return new RichText(withFlattenedSimpleTextNodes);
  }

  private List<RichTextNode> parseGlobalDefinitions(List<RichTextNode> inputNodes, String journeyId) {
    return parseDefinitions(inputNodes, GLOBAL_DEFINITION_PATTERN, matcher -> createGlobalDefinition(matcher, journeyId));
  }

  private List<RichTextNode> parseLocalDefinitions(List<RichTextNode> inputNodes, String controlEntryId) {
    return parseDefinitions(inputNodes, LOCAL_DEFINITION_PATTERN, matcher -> createLocalDefinition(matcher, controlEntryId));
  }

  private List<RichTextNode> parseControlEntries(List<RichTextNode> inputNodes) {
    return parseDefinitions(inputNodes, CONTROL_ENTRY_PATTERN, this::createControlEntry);
  }

  private List<RichTextNode> parseModalContentLinks(List<RichTextNode> inputNodes) {
    return parseDefinitions(inputNodes, MODAL_CONTENT_LINK_PATTERN, this::createModalContentLinkNode);
  }

  private RichTextNode createGlobalDefinition(Matcher matcher, String journeyId) {
    String termInQuotes = matcher.group(0);
    String term = matcher.group(1);

    return parserLookupService.getGlobalDefinitionForTerm(term, journeyId)
        .map(definition -> (RichTextNode) new DefinitionReferenceNode(termInQuotes, definition.getId().toString(), true))
        .orElse(new SimpleTextNode(termInQuotes));
  }

  private RichTextNode createLocalDefinition(Matcher matcher, String controlEntryId) {
    String termInQuotes = matcher.group(0);
    String term = matcher.group(1);

    return parserLookupService.getLocalDefinitionForTerm(term, controlEntryId)
        .map(definition -> (RichTextNode) new DefinitionReferenceNode(termInQuotes, definition.getId().toString(), false))
        .orElse(new SimpleTextNode(termInQuotes));
  }

  private RichTextNode createControlEntry(Matcher matcher) {
    String controlCode = matcher.group(0);
    //If there's a matching code, create a ControlEntryReference - otherwise treat this as simple text
    return parserLookupService.getControlEntryForCode(controlCode)
        .map(controlEntry -> (RichTextNode) new ControlEntryReferenceNode(controlCode, controlEntry.getId().toString()))
        .orElse(new SimpleTextNode(controlCode));
  }

  private RichTextNode createModalContentLinkNode(Matcher matcher) {
    String linkText = matcher.group(1);
    String contentId = matcher.group(2);
    return new ModalContentLinkNode(linkText, contentId);
  }

  private List<RichTextNode> parseDefinitions(List<RichTextNode> inputNodes, Pattern pattern,
                                              Function<Matcher, RichTextNode> nodeFactory) {
    List<RichTextNode> resultNodes = new ArrayList<>();

    for (RichTextNode textNode : inputNodes) {
      if (textNode instanceof SimpleTextNode) {
        String textContent = textNode.getTextContent();
        Matcher matcher = pattern.matcher(textContent);

        int lastEndIndex = 0;
        while (matcher.find()) {
          if (matcher.start() > 0) {
            //Create node for any simple text between matches (or before first match)
            String leadingText = textContent.substring(lastEndIndex, matcher.start());
            resultNodes.add(new SimpleTextNode(leadingText));
          }

          resultNodes.add(nodeFactory.apply(matcher));
          lastEndIndex = matcher.end();
        }

        //Append remaining text
        String trailingText = textContent.substring(lastEndIndex);
        if (trailingText.length() > 0) {
          resultNodes.add(new SimpleTextNode(trailingText));
        }
      } else {
        //Only parse SimpleTextNodes - other types have already been parsed
        resultNodes.add(textNode);
      }
    }

    return resultNodes;
  }

  private List<RichTextNode> flattenConsecutiveSimpleTextNodes(List<RichTextNode> inputNodes) {
    List<RichTextNode> resultNodes = new ArrayList<>();
    StringBuilder simpleText = new StringBuilder();

    for (RichTextNode inputNode : inputNodes) {
      if (inputNode instanceof SimpleTextNode) {
        simpleText.append(inputNode.getTextContent());
      } else {
        if (simpleText.length() > 0) {
          resultNodes.add(new SimpleTextNode(simpleText.toString()));
          simpleText = new StringBuilder();
        }

        resultNodes.add(inputNode);
      }
    }

    //Append any simple text remaining at the end
    if (simpleText.length() > 0) {
      resultNodes.add(new SimpleTextNode(simpleText.toString()));
    }

    return resultNodes;
  }
}
