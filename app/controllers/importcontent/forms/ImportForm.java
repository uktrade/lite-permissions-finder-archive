package controllers.importcontent.forms;

import play.data.validation.Constraints.Required;

public class ImportForm {
  @Required(message = "Answer this question")
  public String selectedOption;
}
