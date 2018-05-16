package triage.text;

import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

public class RichText {

  private final List<RichTextNode> richTextNodes;

  public RichText(String text) {
    richTextNodes = Collections.singletonList(new SimpleTextNode(text));
  }

  RichText(List<RichTextNode> richTextNodes) {
    this.richTextNodes = richTextNodes;
  }

  public List<RichTextNode> getRichTextNodes() {
    return richTextNodes;
  }

  public Set<String> getReferencedControlEntryIds() {
    return richTextNodes.stream()
        .filter(e -> e instanceof ControlEntryReferenceNode)
        .map(e -> (ControlEntryReferenceNode) e)
        .map(ControlEntryReferenceNode::getControlEntryId)
        .collect(Collectors.toSet());
  }

  public Set<String> getReferencedGlobalDefinitionIds() {
    return getReferencedDefinitionIds(true);
  }

  public Set<String> getReferencedLocalDefinitionIds() {
    return getReferencedDefinitionIds(false);
  }

  private Set<String> getReferencedDefinitionIds(boolean global) {
    return richTextNodes.stream()
        .filter(e -> e instanceof DefinitionReferenceNode)
        .map(e -> (DefinitionReferenceNode) e)
        .filter(e -> (global && e.isGlobal()) || (!global && !e.isGlobal()))
        .map(DefinitionReferenceNode::getReferencedDefinitionId)
        .collect(Collectors.toSet());
  }
}
