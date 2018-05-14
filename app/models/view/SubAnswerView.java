package models.view;

import java.util.List;

public class SubAnswerView {

  private final String text;
  private final List<SubAnswerView> subAnswerViews;

  public SubAnswerView(String text, List<SubAnswerView> subAnswerViews) {
    this.text = text;
    this.subAnswerViews = subAnswerViews;
  }

  public String getText() {
    return text;
  }

  public List<SubAnswerView> getSubAnswerViews() {
    return subAnswerViews;
  }

}
