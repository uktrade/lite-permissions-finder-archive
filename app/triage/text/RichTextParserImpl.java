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
  private static final Pattern CONTROL_ENTRY_PATTERN = Pattern.compile("(?:ML|PL|[0-9][A-Z])[0-9a-p.]+");

  private final ParserLookupService parserLookupService;

  @Inject
  public RichTextParserImpl(ParserLookupService parserLookupService) {
    this.parserLookupService = parserLookupService;
  }

  @Override
  public RichText parse(String text, String stageId) {
    List<RichTextNode> withGlobalDefinitions = parseGlobalDefinitions(Collections.singletonList(new SimpleTextNode(text)));
    List<RichTextNode> withLocalDefinitions = parseLocalDefinitions(withGlobalDefinitions, stageId);
    List<RichTextNode> withControlEntries = parseControlEntries(withLocalDefinitions);
    List<RichTextNode> withFlattenedSimpleTextNodes = flattenConsecutiveSimpleTextNodes(withControlEntries);
    return new RichText(withFlattenedSimpleTextNodes);
  }

  private List<RichTextNode> parseGlobalDefinitions(List<RichTextNode> inputNodes) {
    return parseDefinitions(inputNodes, GLOBAL_DEFINITION_PATTERN, this::createGlobalDefinition);
  }

  private List<RichTextNode> parseLocalDefinitions(List<RichTextNode> inputNodes, String stageId) {
    return parseDefinitions(inputNodes, LOCAL_DEFINITION_PATTERN, matcher -> createLocalDefinition(matcher, stageId));
  }

  private List<RichTextNode> parseControlEntries(List<RichTextNode> inputNodes) {
    return parseDefinitions(inputNodes, CONTROL_ENTRY_PATTERN, this::createControlEntry);
  }

  private RichTextNode createGlobalDefinition(Matcher matcher) {
    String termInQuotes = matcher.group(0);
    String term = matcher.group(1);

    return parserLookupService.getGlobalDefinitionForTerm(term)
        .map(definition -> (RichTextNode) new DefinitionReferenceNode(termInQuotes, definition.getId(), true))
        .orElse(new SimpleTextNode(termInQuotes));
  }

  private RichTextNode createLocalDefinition(Matcher matcher, String stageId) {
    String termInQuotes = matcher.group(0);
    String term = matcher.group(1);

    return parserLookupService.getLocalDefinitionForTerm(term, stageId)
        .map(definition -> (RichTextNode) new DefinitionReferenceNode(termInQuotes, definition.getId(), false))
        .orElse(new SimpleTextNode(termInQuotes));
  }

  private RichTextNode createControlEntry(Matcher matcher) {
    String controlCode = matcher.group(0);
    //If there's a matching code, create a ControlEntryReference - otherwise treat this as simple text
    return parserLookupService.getControlEntryForCode(controlCode)
        .map(controlEntry -> (RichTextNode) new ControlEntryReferenceNode(controlCode, controlEntry.getId()))
        .orElse(new SimpleTextNode(controlCode));
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
          //TODO does this need an option to skip first match?

          if (matcher.start() > 0) {
            //Create node for any simple text between matches (or before first match)
            String leadingText = textContent.substring(lastEndIndex, matcher.start());
            resultNodes.add(new SimpleTextNode(leadingText));
          }

          resultNodes.add(nodeFactory.apply(matcher));
          lastEndIndex = matcher.end();
        }

        //Append remaining text
        String trailingText = textContent.substring(lastEndIndex, textContent.length());
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
    StringBuffer simpleText = new StringBuffer();

    for (RichTextNode inputNode : inputNodes) {
      if (inputNode instanceof SimpleTextNode) {
        simpleText.append(inputNode.getTextContent());
      } else {
        if (simpleText.length() > 0) {
          resultNodes.add(new SimpleTextNode(simpleText.toString()));
          simpleText = new StringBuffer();
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
