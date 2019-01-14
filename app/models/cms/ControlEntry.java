package models.cms;

public class ControlEntry {
  private Long id;
  private Long parentControlEntryId;
  private String controlCode;
  private String fullDescription;
  private String summaryDescription;
  private boolean nested;
  private Integer displayOrder;
  private Long journeyId;

  public Long getId() {
    return id;
  }

  public ControlEntry setId(Long id) {
    this.id = id;
    return this;
  }

  public Long getParentControlEntryId() {
    return parentControlEntryId;
  }

  public ControlEntry setParentControlEntryId(Long parentControlEntryId) {
    this.parentControlEntryId = parentControlEntryId;
    return this;
  }

  public String getControlCode() {
    return controlCode;
  }

  public ControlEntry setControlCode(String controlCode) {
    this.controlCode = controlCode;
    return this;
  }

  public String getFullDescription() {
    return fullDescription;
  }

  public ControlEntry setFullDescription(String fullDescription) {
    this.fullDescription = fullDescription;
    return this;
  }

  public String getSummaryDescription() {
    return summaryDescription;
  }

  public ControlEntry setSummaryDescription(String summaryDescription) {
    this.summaryDescription = summaryDescription;
    return this;
  }

  public boolean isNested() {
    return nested;
  }

  public ControlEntry setNested(boolean nested) {
    this.nested = nested;
    return this;
  }

  public Integer getDisplayOrder() {
    return displayOrder;
  }

  public ControlEntry setDisplayOrder(Integer displayOrder) {
    this.displayOrder = displayOrder;
    return this;
  }

  public Long getJourneyId() {
    return journeyId;
  }

  public ControlEntry setJourneyId(Long journeyId) {
    this.journeyId = journeyId;
    return this;
  }

  public String getJumpToControlCodes() {
    return jumpToControlCodes;
  }

  public JumpToControlCodes setJumpToControlCodes(String jumpToControlCodes) {
    this.jumpToControlCodes = jumpToControlCodes;
    return this;
  }
}
