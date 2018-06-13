package models.view.form;

import play.data.validation.Constraints;

public class ItemDescriptionForm {

  @Constraints.MinLength(value = 2, message = "Minimum length is 2")
  @Constraints.MaxLength(value = 200, message = "Maximum length is 200")
  @Constraints.Required(message = "Item description is required.")
  public String description;

}
