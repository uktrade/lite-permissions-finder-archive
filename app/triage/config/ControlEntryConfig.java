package triage.config;

import java.util.Optional;
import java.util.Set;
import triage.text.RichText;

public class ControlEntryConfig {

  private final String id;
  private final long journeyId;
  private final String controlCode;
  private final RichText fullDescription;
  private final RichText summaryDescription;
  private final ControlEntryConfig parentControlEntry;
  private final boolean nestedChildren;
  private final Set<String> jumpToControlCodes;
  private final boolean decontrolled;

  public ControlEntryConfig(String id, long journeyId, String controlCode, RichText fullDescription,
                            RichText summaryDescription, ControlEntryConfig parentControlEntry,
                            boolean nestedChildren, Set<String> jumpToControlCodes, boolean decontrolled) {
    this.id = id;
    this.journeyId = journeyId;
    this.controlCode = controlCode;
    this.fullDescription = fullDescription;
    this.summaryDescription = summaryDescription;
    this.parentControlEntry = parentControlEntry;
    this.nestedChildren = nestedChildren;
    this.jumpToControlCodes = jumpToControlCodes;
    this.decontrolled = decontrolled;
  }

  public String getId() {
    return id;
  }

  public long getJourneyId() {
    return journeyId;
  }

  public String getControlCode() {
    return controlCode;
  }

  public RichText getFullDescription() {
    return fullDescription;
  }

  public Optional<RichText> getSummaryDescription() {
    return Optional.ofNullable(summaryDescription);
  }

  public Optional<ControlEntryConfig> getParentControlEntry() {
    return Optional.ofNullable(parentControlEntry);
  }

  //True if this control entry has nested children (not if it is itself nested)
  public boolean hasNestedChildren() {
    return nestedChildren;
  }

  public Set<String> getJumpToControlCodes() {
    return jumpToControlCodes;
  }

  public boolean isDecontrolled() {
    return decontrolled;
  }
}
