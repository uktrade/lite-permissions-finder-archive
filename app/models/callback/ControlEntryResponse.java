package models.callback;

import models.cms.ControlEntry;

import java.util.Objects;

public class ControlEntryResponse {
  private Long id;
  private String controlCode;
  private String description;

  public ControlEntryResponse(ControlEntry controlEntry) {
    this.id = controlEntry.getId();
    this.controlCode = controlEntry.getControlCode();
    this.description = controlEntry.getFullDescription();
  }

  public Long getId() {
    return id;
  }

  public String getControlCode() {
    return controlCode;
  }

  public String getDescription() {
    return description;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (o == null || getClass() != o.getClass()) return false;
    ControlEntryResponse that = (ControlEntryResponse) o;
    return Objects.equals(controlCode, that.controlCode);
  }

  @Override
  public int hashCode() {
    return Objects.hash(controlCode);
  }
}
