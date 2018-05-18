package models.view.form;

import play.data.validation.Constraints;

public class RequestOgelForm {

  @Constraints.Required(message = "Confirm button not checked.")
  public String answer;

}
