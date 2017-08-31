package controllers.importcontent.forms;

import play.data.validation.Constraints.Required;

public class ImportFoodWhatForm {
  @Required(message = "Select what you're importing")
  public String selectedOption;
}
