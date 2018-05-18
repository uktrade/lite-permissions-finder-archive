package components.cms.parser.model;

public class LoadingMetadata {
  private Long stageId;
  private Long stageAnswerId;
  private Long controlEntryId;

  public LoadingMetadata() {
  }

  public Long getStageId() {
    return stageId;
  }

  public LoadingMetadata setStageId(Long stageId) {
    this.stageId = stageId;
    return this;
  }

  public Long getStageAnswerId() {
    return stageAnswerId;
  }

  public LoadingMetadata setStageAnswerId(Long stageAnswerId) {
    this.stageAnswerId = stageAnswerId;
    return this;
  }

  public Long getControlEntryId() {
    return controlEntryId;
  }

  public LoadingMetadata setControlEntryId(Long controlEntryId) {
    this.controlEntryId = controlEntryId;
    return this;
  }
}
