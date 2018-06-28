package models.cms;

public class RelatedControlEntry {
  private Long controlEntryId;
  private Long relatedControlEntryId;

  public Long getControlEntryId() {
    return controlEntryId;
  }

  public RelatedControlEntry setControlEntryId(Long controlEntryId) {
    this.controlEntryId = controlEntryId;
    return this;
    }

  public Long getRelatedControlEntryId() {
    return relatedControlEntryId;
  }

  public RelatedControlEntry setRelatedControlEntryId(Long relatedControlEntryId) {
    this.relatedControlEntryId = relatedControlEntryId;
    return this;
  }
}
