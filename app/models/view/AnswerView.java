package models.view;

import lombok.Data;
import play.twirl.api.Html;

import java.util.List;

@Data
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
}
