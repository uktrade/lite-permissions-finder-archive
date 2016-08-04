package controllers.search;

import controllers.controlcode.ControlCodeController;
import controllers.ErrorController;
import controllers.services.controlcode.frontend.FrontendServiceClient;
import play.data.Form;
import play.data.FormFactory;

public class SearchResultsController {

  private final FormFactory formFactory;

  protected final FrontendServiceClient frontendServiceClient;

  protected final ControlCodeController controlCodeController;

  protected final ErrorController errorController;

  public SearchResultsController(FormFactory formFactory, FrontendServiceClient frontendServiceClient,
                                 ControlCodeController controlCodeController, ErrorController errorController) {
    this.formFactory = formFactory;
    this.frontendServiceClient = frontendServiceClient;
    this.controlCodeController = controlCodeController;
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
      if (isResultValid(result) || isActionValid(action)) {
        return null;
      }
      else {
        return "Please pick a button on this screen to continue";
      }
    }

    public static boolean isResultValid(String result){
      return !(result == null || result.isEmpty());
    }

    public static boolean isActionValid(String action){
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