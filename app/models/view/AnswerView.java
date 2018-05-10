package models.view;

public class AnswerView {

  private final String prompt;
  private final String value;

  public AnswerView(String prompt, String value) {
    this.prompt = prompt;
    this.value = value;
  }

  public String getPrompt() {
    return prompt;
  }

  public String getValue() {
    return value;
  }
}
