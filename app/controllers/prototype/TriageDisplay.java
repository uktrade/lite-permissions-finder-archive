package controllers.prototype;

import utils.common.SelectOption;

import java.util.List;

public class TriageDisplay {

  public String question;
  public List<SelectOption> answers;

  public TriageDisplay(String question, List<SelectOption> answers) {
    this.question = question;
    this.answers = answers;
  }
}
