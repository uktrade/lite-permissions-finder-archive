package controllers.importcontent.forms;

import play.data.validation.Constraints.Required;

public class ImportWhatForm {
  @Required(message = "Select what type of item you're importing")
  public String selectedOption;
}
