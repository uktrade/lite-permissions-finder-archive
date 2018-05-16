package models.view;

public class CheckboxView {

  private final AnswerView answerView;
  private final boolean checked;

  public CheckboxView(AnswerView answerView, boolean checked) {
    this.answerView = answerView;
    this.checked = checked;
  }

  public AnswerView getAnswerView() {
    return answerView;
  }

  public boolean isChecked() {
    return checked;
  }

}
