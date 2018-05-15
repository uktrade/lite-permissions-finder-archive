package triage.config;

import triage.text.RichText;

public class DefinitionConfig {

  private final String id;
  private final String term;
  private final RichText definitionText;
  private final String stageId;

  public DefinitionConfig(String id, String term, RichText definitionText, String stageId) {
    this.id = id;
    this.term = term;
    this.definitionText = definitionText;
    this.stageId = stageId;
  }

  public String getId() {
    return id;
  }

  public String getTerm() {
    return term;
  }

  public RichText getDefinitionText() {
    return definitionText;
  }

  public String getStageId() {
    return stageId;
  }

  public boolean isLocal() {
    return stageId != null;
  }
}
