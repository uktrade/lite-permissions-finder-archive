package models.view.form;

import play.data.validation.Constraints;

public class ContinueApplicationForm {

  @Constraints.Required(message = "You must enter your resume code")
  public String resumeCode;

}
