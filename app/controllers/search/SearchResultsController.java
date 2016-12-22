package controllers.search;

import components.common.journey.JourneyManager;
import components.services.search.SearchServiceClient;
import controllers.controlcode.ControlCodeSummaryController;
import controllers.ErrorController;
import components.services.controlcode.FrontendServiceClient;
import play.data.Form;
import play.data.FormFactory;

import java.util.Optional;

public class SearchResultsController {

  public static final int PAGINATION_SIZE = 5;

  protected final JourneyManager journeyManager;
  private final FormFactory formFactory;
  protected final SearchServiceClient searchServiceClient;
  protected final FrontendServiceClient frontendServiceClient;
  protected final ControlCodeSummaryController controlCodeSummaryController;
  protected final ErrorController errorController;


  public SearchResultsController(JourneyManager journeyManager,
                                 FormFactory formFactory,
                                 SearchServiceClient searchServiceClient,
                                 FrontendServiceClient frontendServiceClient,
                                 ControlCodeSummaryController controlCodeSummaryController,
                                 ErrorController errorController) {
    this.journeyManager = journeyManager;
    this.formFactory = formFactory;
    this.searchServiceClient = searchServiceClient;
    this.frontendServiceClient = frontendServiceClient;
    this.controlCodeSummaryController = controlCodeSummaryController;
    this.errorController = errorController;
  }

  public enum SearchResultAction{
    NONE_MATCHED,
    SHORE_MORE,
    EDIT_DESCRIPTION,
    CONTINUE
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
    else if("no-matched-result".equals(form.action)){
      return Optional.of(SearchResultAction.NONE_MATCHED);
    }
    else if("show-more-results".equals(form.action)){
      return Optional.of(SearchResultAction.SHORE_MORE);
    }
    else if("edit-description".equals(form.action)){
      return Optional.of(SearchResultAction.EDIT_DESCRIPTION);
    }
    else if("edit-description".equals(form.action)){
      return Optional.of(SearchResultAction.CONTINUE);
    }
    else {
      return Optional.empty();
    }
  }

  public static class ControlCodeSearchResultsForm {

    public String result;

    public String action;

    public String controlCodeSubJourney;

    public String goodsType;

    public String resultsDisplayCount;

    public String paginationSize;

  }
}