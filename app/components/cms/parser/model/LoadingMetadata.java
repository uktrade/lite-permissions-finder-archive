package components.cms.parser.model;

public class LoadingMetadata {
  private Long stageId;
  private Long stageAnswerId;
  private Long controlEntryId;
  private String id;

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

  public String getId() {
    return id;
  }

  public LoadingMetadata setId(String id) {
    this.id = id;
    return this;
  }

  @Override
  public String toString() {
    return "LoadingMetadata{" +
            "stageId=" + stageId +
            ", stageAnswerId=" + stageAnswerId +
            ", controlEntryId=" + controlEntryId +
            ", id='" + id + '\'' +
            '}';
  }
}
