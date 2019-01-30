package models.cms;

import lombok.Data;

@Data
public class GlobalDefinition {
  private Long id;
  private Long journeyId;
  private String term;
  private String definitionText;

  public GlobalDefinition(Long journeyId, String term, String definitionText) {
    this.journeyId = journeyId;
    this.term = term;
    this.definitionText = definitionText;
  }

  public GlobalDefinition(Long id, Long journeyId, String term, String definitionText) {
    this(journeyId, term, definitionText);
    this.id = id;
  }
}
