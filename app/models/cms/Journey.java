package models.cms;

public class Journey {
  private Long id;
  private String journeyName;
  private String friendlyJourneyName;
  private Long initialStageId;

  public Long getId() {
    return id;
  }

  public Journey setId(Long id) {
    this.id = id;
    return this;
  }

  public String getJourneyName() {
    return journeyName;
  }

  public Journey setJourneyName(String journeyName) {
    this.journeyName = journeyName;
    return this;
  }

  public String getFriendlyJourneyName() {
    return friendlyJourneyName;
  }

  public Journey setFriendlyJourneyName(String friendlyJourneyName) {
    this.friendlyJourneyName = friendlyJourneyName;
    return this;
  }

  public Long getInitialStageId() {
    return initialStageId;
  }

  public Journey setInitialStageId(Long initialStageId) {
    this.initialStageId = initialStageId;
    return this;
  }
}
