package models.view.form;

import play.data.validation.Constraints;

public class AnswerForm {

  @Constraints.Required(message = "Please select an answer")
  public String answer;

  @Constraints.Required
  public String action;

}
