package controllers.search;

import play.data.Form;
import play.data.FormFactory;
import play.data.validation.Constraints.Required;

public class SearchResultsController {

  private final FormFactory formFactory;

  public SearchResultsController(FormFactory formFactory) {
    this.formFactory = formFactory;
  }

  public Form<ControlCodeSearchResultsForm> bindForm(){
    return formFactory.form(ControlCodeSearchResultsForm.class).bindFromRequest();
  }

  public static class ControlCodeSearchResultsForm {

    @Required
    public String result;

  }
}