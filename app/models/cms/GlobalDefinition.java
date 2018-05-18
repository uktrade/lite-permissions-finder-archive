package models.cms;

public class GlobalDefinition {
  private Long id;
  private Long journeyId;
  private String term;
  private String definitionText;

  public Long getId() {
    return id;
  }

  public GlobalDefinition setId(Long id) {
    this.id = id;
    return this;
  }

  public Long getJourneyId() {
    return journeyId;
  }

  public GlobalDefinition setJourneyId(Long journeyId) {
    this.journeyId = journeyId;
    return this;
  }

  public String getTerm() {
    return term;
  }

  public GlobalDefinition setTerm(String term) {
    this.term = term;
    return this;
  }

  public String getDefinitionText() {
    return definitionText;
  }

  public GlobalDefinition setDefinitionText(String definitionText) {
    this.definitionText = definitionText;
    return this;
  }
}
