package controllers.importcontent.forms;

import play.data.validation.Constraints.Required;

public class ImportFoodWhatForm {
  @Required(message = "Choose an import type")
  public String selectedOption;
}
