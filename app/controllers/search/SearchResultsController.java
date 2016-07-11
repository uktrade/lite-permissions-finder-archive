package controllers.search;

import controllers.ErrorController;
import controllers.services.controlcode.lookup.LookupServiceClient;
import play.data.Form;
import play.data.FormFactory;
import play.data.validation.Constraints.Required;

public class SearchResultsController {

  private final FormFactory formFactory;

  protected final LookupServiceClient lookupServiceClient;

  protected final ErrorController errorController;

  public SearchResultsController(FormFactory formFactory, LookupServiceClient lookupServiceClient, ErrorController errorController) {
    this.formFactory = formFactory;
    this.lookupServiceClient = lookupServiceClient;
    this.errorController = errorController;
  }

  public Form<ControlCodeSearchResultsForm> bindForm(){
    return formFactory.form(ControlCodeSearchResultsForm.class).bindFromRequest();
  }

  public static class ControlCodeSearchResultsForm {

    @Required
    public String result;

  }
}