package models.cms;

public class LocalDefinition {
  private Long id;
  private Long controlEntryId;
  private String term;
  private String definitionText;

  public Long getId() {
    return id;
  }

  public LocalDefinition setId(Long id) {
    this.id = id;
    return this;
  }

  public Long getControlEntryId() {
    return controlEntryId;
  }

  public LocalDefinition setControlEntryId(Long controlEntryId) {
    this.controlEntryId = controlEntryId;
    return this;
  }

  public String getTerm() {
    return term;
  }

  public LocalDefinition setTerm(String term) {
    this.term = term;
    return this;
  }

  public String getDefinitionText() {
    return definitionText;
  }

  public LocalDefinition setDefinitionText(String definitionText) {
    this.definitionText = definitionText;
    return this;
  }
}
