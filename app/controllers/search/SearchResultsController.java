package controllers.search;

import components.services.controlcode.search.SearchServiceClient;
import controllers.controlcode.ControlCodeController;
import controllers.ErrorController;
import components.services.controlcode.frontend.FrontendServiceClient;
import play.data.Form;
import play.data.FormFactory;

import java.util.Optional;

public class SearchResultsController {

  private final FormFactory formFactory;

  protected final SearchServiceClient searchServiceClient;

  protected final FrontendServiceClient frontendServiceClient;

  protected final ControlCodeController controlCodeController;

  protected final ErrorController errorController;

  public enum SearchResultAction{
    NONE_MATCHED,
    SHORE_MORE,
  }

  public SearchResultsController(FormFactory formFactory,
                                 SearchServiceClient searchServiceClient,
                                 FrontendServiceClient frontendServiceClient,
                                 ControlCodeController controlCodeController,
                                 ErrorController errorController) {
    this.formFactory = formFactory;
    this.searchServiceClient = searchServiceClient;
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

    public Optional<String> getResult() {
      return !(result == null || result.isEmpty()) ? Optional.of(result) : Optional.empty();
    }

    public Optional<SearchResultAction> getAction() {
      if (action == null || action.isEmpty()){
        return Optional.empty();
      }
      if("no-matched-result".equals(action)){
        return Optional.of(SearchResultAction.NONE_MATCHED);
      }
      if("show-more-results".equals(action)){
        return Optional.of(SearchResultAction.SHORE_MORE);
      }
      return Optional.empty();
    }
  }
}