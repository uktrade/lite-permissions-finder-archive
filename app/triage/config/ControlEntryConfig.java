package triage.config;

import triage.text.RichText;

import java.util.Optional;

public class ControlEntryConfig {

  private final String id;
  private final String controlCode;
  private final RichText fullDescription;
  private final RichText summaryDescription;
  private final ControlEntryConfig parentControlEntry;
  private final boolean nestedChildren;

  public ControlEntryConfig(String id, String controlCode, RichText fullDescription,
                            RichText summaryDescription, ControlEntryConfig parentControlEntry,
                            boolean nestedChildren) {
    this.id = id;
    this.controlCode = controlCode;
    this.fullDescription = fullDescription;
    this.summaryDescription = summaryDescription;
    this.parentControlEntry = parentControlEntry;
    this.nestedChildren = nestedChildren;
  }

  public String getId() {
    return id;
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

}
