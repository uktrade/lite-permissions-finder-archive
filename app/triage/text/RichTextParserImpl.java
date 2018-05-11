package triage.text;

import com.google.inject.Inject;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.function.Function;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class RichTextParserImpl implements RichTextParser {

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
    Pattern pattern = Pattern.compile("\"(.*?)\"");
    return parseDefinitions(inputNodes, pattern, matcher -> {
      String termInQuotes = matcher.group(0);
      String term = matcher.group(1);

      return parserLookupService.getGlobalDefinitionForTerm(term)
          .map(definition -> (RichTextNode) new DefinitionReferenceNode(termInQuotes, definition.getId(), true))
          .orElse(new SimpleTextNode(termInQuotes));
    });
  }

  private List<RichTextNode> parseLocalDefinitions(List<RichTextNode> inputNodes, String stageId) {
    Pattern pattern = Pattern.compile("\'(.*?)'");
    return parseDefinitions(inputNodes, pattern, matcher -> {
      String termInQuotes = matcher.group(0);
      String term = matcher.group(1);

      return parserLookupService.getLocalDefinitionForTerm(term, stageId)
          .map(definition -> (RichTextNode) new DefinitionReferenceNode(termInQuotes, definition.getId(), false))
          .orElse(new SimpleTextNode(termInQuotes));
    });
  }

  private List<RichTextNode> parseControlEntries(List<RichTextNode> inputNodes) {
    Pattern pattern = Pattern.compile("(?:ML|PL|[0-9][A-Z])[0-9a-p.]+"); //only go to p to avoid picking "0Hz"
    return parseDefinitions(inputNodes, pattern, matcher -> {
      String controlCode = matcher.group(0);

      //If there's a matching code, create a ControlEntryReference - otherwise treat this as simple text
      return parserLookupService.getControlEntryForCode(controlCode)
          .map(controlEntry -> (RichTextNode) new ControlEntryReferenceNode(controlCode, controlEntry.getId()))
          .orElse(new SimpleTextNode(controlCode));
    });
  }

  private List<RichTextNode> parseDefinitions(List<RichTextNode> inputNodes, Pattern pattern,
                                              Function<Matcher, RichTextNode> nodeFactory) {
    List<RichTextNode> resultNodes = new ArrayList<>();

    for (RichTextNode textNode : inputNodes) {
      if (!(textNode instanceof SimpleTextNode)) {
        //Only parse SimpleNodes - other types have already been parsed
        resultNodes.add(textNode);
      } else {
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
      }
    }

    return resultNodes;
  }

  private List<RichTextNode> flattenConsecutiveSimpleTextNodes(List<RichTextNode> inputNodes) {
    List<RichTextNode> resultNodes = new ArrayList<>();
    StringBuffer simpleText = new StringBuffer();

    for (RichTextNode inputNode : inputNodes) {
      if (!(inputNode instanceof SimpleTextNode)) {
        if (simpleText.length() > 0) {
          resultNodes.add(new SimpleTextNode(simpleText.toString()));
          simpleText = new StringBuffer();
        }

        resultNodes.add(inputNode);
      } else {
        simpleText.append(inputNode.getTextContent());
      }
    }

    //Append any simple text remaining at the end
    if (simpleText.length() > 0) {
      resultNodes.add(new SimpleTextNode(simpleText.toString()));
    }

    return resultNodes;
  }
}
