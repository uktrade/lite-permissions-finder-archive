package controllers.search;

import controllers.ErrorController;
import controllers.services.controlcode.lookup.LookupServiceClient;
import play.data.Form;
import play.data.FormFactory;

public class SearchResultsController {

  private final FormFactory formFactory;

  protected final LookupServiceClient lookupServiceClient;

  protected final ErrorController errorController;

  public SearchResultsController(FormFactory formFactory, LookupServiceClient lookupServiceClient, ErrorController errorController) {
    this.formFactory = formFactory;
    this.lookupServiceClient = lookupServiceClient;
    this.errorController = errorController;
  }

  public Form<ControlCodeSearchResultsForm> searchResultsForm() {
    return formFactory.form(ControlCodeSearchResultsForm.class);
  }

  public Form<ControlCodeSearchResultsForm> bindSearchResultsForm(){
    return searchResultsForm().bindFromRequest();
  }

  public static class ControlCodeSearchResultsForm {

    public String result;

    public String action;

    public String validate() {
      if (isResultValid() || isActionValid()) {
        return null;
      }
      else {
        return "Please pick a button on this screen to continue";
      }
    }

    private boolean isResultValid(){
      return !(result == null || result.isEmpty());
    }

    private boolean isActionValid(){
      if (action == null || action.isEmpty()){
        return false;
      }
      else if(action.equals("no-matched-result")){
        return true;
      }
      return false;
    }
  }
}