package models.view.form;

import play.data.validation.Constraints;

public class ItemDescriptionForm {

  @Constraints.Required(message = "Item description is required.")
  public String description;

}
