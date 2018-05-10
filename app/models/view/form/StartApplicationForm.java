package models.view.form;

import play.data.validation.Constraints;

public class StartApplicationForm {

  @Constraints.Email()
  public String emailAddress;

}
