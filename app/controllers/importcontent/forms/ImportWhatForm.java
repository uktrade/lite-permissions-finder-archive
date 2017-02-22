package controllers.importcontent.forms;

import play.data.validation.Constraints.Required;

public class ImportWhatForm {
  @Required(message = "Choose an item category")
  public String selectedOption;
}
