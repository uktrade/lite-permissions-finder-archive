package models;


import utils.common.SelectOption;

import java.util.List;

public class ImportFormData {

  private String stageKey;
  private String question;
  private List<SelectOption> options;

  public ImportFormData() {
  }

  public ImportFormData(String stageKey) {
    this.stageKey = stageKey;
  }

  public String getStageKey() {
    return stageKey;
  }

  public void setStageKey(String stageKey) {
    this.stageKey = stageKey;
  }

  public String getQuestion() {
    return question;
  }

  public void setQuestion(String question) {
    this.question = question;
  }

  public List<SelectOption> getOptions() {
    return options;
  }

  public void setOptions(List<SelectOption> options) {
    this.options = options;
  }
}
