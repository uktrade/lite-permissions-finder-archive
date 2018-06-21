package models.view;

import play.twirl.api.Html;

import java.util.List;

public class AnswerView {

  private final String prompt;
  private final String value;
  private final boolean dividerAbove;
  private final List<SubAnswerView> subAnswerViews;
  private final String nestedContent;
  private final String moreInformation;
  private final String definitions;
  private final String relatedItems;
  private final boolean detailPanel;
  private final Html htmlAbove;

  public AnswerView(String prompt, String value, boolean dividerAbove, List<SubAnswerView> subAnswerViews,
                    String nestedContent, String moreInformation, String definitions, String relatedItems,
                    boolean detailPanel, Html htmlAbove) {
    this.prompt = prompt;
    this.value = value;
    this.dividerAbove = dividerAbove;
    this.subAnswerViews = subAnswerViews;
    this.nestedContent = nestedContent;
    this.moreInformation = moreInformation;
    this.definitions = definitions;
    this.relatedItems = relatedItems;
    this.detailPanel = detailPanel;
    this.htmlAbove = htmlAbove;
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

  public String getDefinitions() {
    return definitions;
  }

  public String getRelatedItems() {
    return relatedItems;
  }

  public boolean isDetailPanel() {
    return detailPanel;
  }

  public Html getHtmlAbove() {
    return htmlAbove;
  }

}
