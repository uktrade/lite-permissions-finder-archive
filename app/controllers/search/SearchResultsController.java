package controllers.search;

import components.common.journey.JourneyManager;
import components.services.search.SearchServiceClient;
import controllers.controlcode.ControlCodeController;
import controllers.ErrorController;
import components.services.controlcode.frontend.FrontendServiceClient;
import play.data.Form;
import play.data.FormFactory;

import java.util.Optional;

public class SearchResultsController {

  protected final JourneyManager journeyManager;
  private final FormFactory formFactory;
  protected final SearchServiceClient searchServiceClient;
  protected final FrontendServiceClient frontendServiceClient;
  protected final ControlCodeController controlCodeController;
  protected final ErrorController errorController;


  public SearchResultsController(JourneyManager journeyManager,
                                 FormFactory formFactory,
                                 SearchServiceClient searchServiceClient,
                                 FrontendServiceClient frontendServiceClient,
                                 ControlCodeController controlCodeController,
                                 ErrorController errorController) {
    this.journeyManager = journeyManager;
    this.formFactory = formFactory;
    this.searchServiceClient = searchServiceClient;
    this.frontendServiceClient = frontendServiceClient;
    this.controlCodeController = controlCodeController;
    this.errorController = errorController;
  }

  public enum SearchResultAction{
    NONE_MATCHED,
    SHORE_MORE,
  }

  public Form<ControlCodeSearchResultsForm> searchResultsForm() {
    return formFactory.form(ControlCodeSearchResultsForm.class);
  }

  public Form<ControlCodeSearchResultsForm> bindSearchResultsForm(){
    return searchResultsForm().bindFromRequest();
  }

  public Optional<String> getResult(ControlCodeSearchResultsForm form) {
    return !(form.result == null || form.result.isEmpty()) ? Optional.of(form.result) : Optional.empty();
  }

  public Optional<SearchResultAction> getAction(ControlCodeSearchResultsForm form) {
    if (form.action == null || form.action.isEmpty()){
      return Optional.empty();
    }
    if("no-matched-result".equals(form.action)){
      return Optional.of(SearchResultAction.NONE_MATCHED);
    }
    if("show-more-results".equals(form.action)){
      return Optional.of(SearchResultAction.SHORE_MORE);
    }
    return Optional.empty();
  }

  public static class ControlCodeSearchResultsForm {

    public String result;

    public String action;

  }
}