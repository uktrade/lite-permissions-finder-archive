package components.cms.parser.model.navigation.column;

import java.util.List;

public class Loops {
  private final String relatedCodes;
  private final String jumpTo;
  private final String defining;
  private final String deferred;
  private final List<String> jumpToControlCodes;

  public Loops(String relatedCodes, String jumpTo, String defining, String deferred, List<String> jumpToControlCodes) {
    this.relatedCodes = relatedCodes;
    this.jumpTo = jumpTo;
    this.defining = defining;
    this.deferred = deferred;
    this.jumpToControlCodes = jumpToControlCodes;
  }

  public String getRelatedCodes() {
    return relatedCodes;
  }

  public String getJumpTo() {
    return jumpTo;
  }

  public String getDefining() {
    return defining;
  }

  public String getDeferred() {
    return deferred;
  }

  public List<String> getJumpToControlCodes() {
    return jumpToControlCodes;
  }
}
