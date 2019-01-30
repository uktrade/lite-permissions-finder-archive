package models.view;

public class QuestionView {
  private final String question;
  private final String answer;

  public QuestionView(String question, String answer) {
    this.question = question;
    this.answer = answer;
  }

  public String getQuestion() {
    return question;
  }

  public String getAnswer() {
    return answer;
  }
}
