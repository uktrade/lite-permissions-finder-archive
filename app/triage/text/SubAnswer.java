package triage.text;

import java.util.List;

public class SubAnswer {

  private final RichText richText;
  private final List<SubAnswer> subAnswers;

  public SubAnswer(RichText richText, List<SubAnswer> subAnswers) {
    this.richText = richText;
    this.subAnswers = subAnswers;
  }

  public RichText getRichText() {
    return richText;
  }

  public List<SubAnswer> getSubAnswers() {
    return subAnswers;
  }

}
