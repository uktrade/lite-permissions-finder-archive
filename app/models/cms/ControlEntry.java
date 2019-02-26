package models.cms;

import java.util.List;

public class ControlEntry {
  private Long id;
  private Long parentControlEntryId;
  private String controlCode;
  private String description;
  private boolean nested;
  private Integer displayOrder;
  private long journeyId;
  private Boolean decontrolled;
  private List<String> jumpToControlCodes;

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

  public String getDescription() {
    return description;
  }

  public ControlEntry setDescription(String description) {
    this.description = description;
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

  public long getJourneyId() {
    return journeyId;
  }

  public ControlEntry setJourneyId(long journeyId) {
    this.journeyId = journeyId;
    return this;
  }

  public List<String> getJumpToControlCodes() {
    return jumpToControlCodes;
  }

  public ControlEntry setJumpToControlCodes(List<String> jumpToControlCodes) {
    this.jumpToControlCodes = jumpToControlCodes;
    return this;
  }

  public Boolean isDecontrolled() {
    return decontrolled;
  }

  public ControlEntry setDecontrolled(Boolean decontrolled) {
    this.decontrolled = decontrolled;
    return this;
  }
}
