package models.importcontent;


import utils.common.SelectOption;

import java.util.List;

public class ImportStageData {

  private String stageKey;
  private String question;
  private List<SelectOption> options;

  public ImportStageData() {
  }

  public ImportStageData(String stageKey) {
    this.stageKey = stageKey;
  }

  public ImportStageData(String stageKey, String question, List<SelectOption> options) {

    this.stageKey = stageKey;
    this.question = question;
    this.options = options;
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
