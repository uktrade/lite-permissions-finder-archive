package models.view;

import java.util.List;

public class AnswerView {

  private final String prompt;
  private final String value;
  private final List<SubAnswerView> subAnswerViews;

  public AnswerView(String prompt, String value, List<SubAnswerView> subAnswerViews) {
    this.prompt = prompt;
    this.value = value;
    this.subAnswerViews = subAnswerViews;
  }

  public String getPrompt() {
    return prompt;
  }

  public String getValue() {
    return value;
  }

  public List<SubAnswerView> getSubAnswerViews() {
    return subAnswerViews;
  }

}
