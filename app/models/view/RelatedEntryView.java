package models.view;

import java.util.List;

public class RelatedEntryView {

  private final String controlCode;
  private final String fullDescription;
  private final String changeUrl;
  private final List<SubAnswerView> subAnswerViews;

  public RelatedEntryView(String controlCode, String fullDescription, String changeUrl,
                          List<SubAnswerView> subAnswerViews) {
    this.controlCode = controlCode;
    this.fullDescription = fullDescription;
    this.changeUrl = changeUrl;
    this.subAnswerViews = subAnswerViews;
  }

  public String getControlCode() {
    return controlCode;
  }

  public String getFullDescription() {
    return fullDescription;
  }

  public String getChangeUrl() {
    return changeUrl;
  }

  public List<SubAnswerView> getSubAnswerViews() {
    return subAnswerViews;
  }
}
