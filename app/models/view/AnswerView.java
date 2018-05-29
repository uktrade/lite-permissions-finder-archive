package models.view;

import java.util.List;

public class AnswerView {

  private final String prompt;
  private final String value;
  private final boolean dividerAbove;
  private final List<SubAnswerView> subAnswerViews;
  private final String nestedContent;
  private final String moreInformation;

  public AnswerView(String prompt, String value, boolean dividerAbove, List<SubAnswerView> subAnswerViews,
                    String nestedContent, String moreInformation) {
    this.prompt = prompt;
    this.value = value;
    this.dividerAbove = dividerAbove;
    this.subAnswerViews = subAnswerViews;
    this.nestedContent = nestedContent;
    this.moreInformation = moreInformation;
  }

  public AnswerView(String prompt, String value, List<SubAnswerView> subAnswerViews) {
    this.prompt = prompt;
    this.value = value;
    this.dividerAbove = false;
    this.subAnswerViews = subAnswerViews;
    this.nestedContent = null;
    this.moreInformation = null;
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

  public String getNestedContent() {
    return nestedContent;
  }

  public String getMoreInformation() {
    return moreInformation;
  }

}
