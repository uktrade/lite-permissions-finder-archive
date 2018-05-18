package models.cms;

public class Journey {
  private Long id;
  private String journeyName;

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
}
