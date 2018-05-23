package models.view;

import java.util.List;

public class AnswerView {

  private final String prompt;
  private final String value;
  private final boolean dividerAbove;
  private final List<SubAnswerView> subAnswerViews;

  public AnswerView(String prompt, String value, boolean dividerAbove, List<SubAnswerView> subAnswerViews) {
    this.prompt = prompt;
    this.value = value;
    this.dividerAbove = dividerAbove;
    this.subAnswerViews = subAnswerViews;
  }

  public String getPrompt() {
    return prompt;
  }

  public String getValue() {
    return value;
  }

  public boolean isDividerAbove() {
    return dividerAbove;
  }

  public List<SubAnswerView> getSubAnswerViews() {
    return subAnswerViews;
  }

}
