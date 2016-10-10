package controllers.search;

import static play.mvc.Results.ok;

import com.google.inject.Inject;
import components.common.journey.JourneyManager;
import components.persistence.PermissionsFinderDao;
import components.services.controlcode.frontend.FrontendServiceClient;
import components.services.controlcode.search.SearchServiceClient;
import components.services.controlcode.search.SearchServiceResult;
import controllers.ErrorController;
import controllers.controlcode.ControlCodeController;
import exceptions.FormStateException;
import journey.Events;
import play.data.Form;
import play.data.FormFactory;
import play.libs.concurrent.HttpExecutionContext;
import play.mvc.Result;
import views.html.search.physicalGoodsSearchResults;

import java.util.Optional;
import java.util.concurrent.CompletionStage;

public class PhysicalGoodsSearchResultsController extends SearchResultsController {

  private final HttpExecutionContext httpExecutionContext;
  private final PermissionsFinderDao dao;
  public static final int PAGINATION_SIZE = 5;

  @Inject
  public PhysicalGoodsSearchResultsController(JourneyManager journeyManager,
                                              FormFactory formFactory,
                                              SearchServiceClient searchServiceClient,
                                              FrontendServiceClient frontendServiceClient,
                                              ControlCodeController controlCodeController,
                                              ErrorController errorController,
                                              HttpExecutionContext httpExecutionContext,
                                              PermissionsFinderDao dao) {
    super(journeyManager, formFactory, searchServiceClient, frontendServiceClient, controlCodeController, errorController);
    this.httpExecutionContext = httpExecutionContext;
    this.dao = dao;
  }

  public CompletionStage<Result> renderForm() {
    return physicalGoodsSearch()
        .thenApplyAsync(result -> {
          int displayCount = Math.min(result.results.size(), PAGINATION_SIZE);
          dao.savePhysicalGoodSearchPaginationDisplayCount(displayCount);
          return ok(physicalGoodsSearchResults.render(searchResultsForm(), result.results, displayCount));
        }, httpExecutionContext.current());
  }

  public CompletionStage<Result> handleSubmit() {
    Form<ControlCodeSearchResultsForm> form = bindSearchResultsForm();

    if (form.hasErrors()) {
      return physicalGoodsSearch()
          .thenApplyAsync(result -> {
            int displayCount = dao.getPhysicalGoodSearchPaginationDisplayCount();
            int newDisplayCount = Math.min(displayCount, result.results.size());
            if (displayCount != newDisplayCount) {
              dao.savePhysicalGoodSearchPaginationDisplayCount(newDisplayCount);
            }
            return ok(physicalGoodsSearchResults.render(form, result.results, newDisplayCount));
          }, httpExecutionContext.current());
    }

    Optional<SearchResultAction> action = getAction(form.get());
    if (action.isPresent()){
      switch (action.get()) {
        case NONE_MATCHED:
          return journeyManager.performTransition(Events.NONE_MATCHED);
        case SHORE_MORE:
          return physicalGoodsSearch()
              .thenApplyAsync(result -> {
                int displayCount = dao.getPhysicalGoodSearchPaginationDisplayCount();
                int newDisplayCount = Math.min(displayCount + PAGINATION_SIZE, result.results.size());
                if (displayCount != newDisplayCount) {
                  dao.savePhysicalGoodSearchPaginationDisplayCount(newDisplayCount);
                }
                return ok(physicalGoodsSearchResults.render(form, result.results, newDisplayCount));
              }, httpExecutionContext.current());
      }
    }

    Optional<String> result = getResult(form.get());
    if (result.isPresent()) {
      dao.savePhysicalGoodControlCode(result.get());
      return journeyManager.performTransition(Events.CONTROL_CODE_SELECTED);
    }

    throw new FormStateException("Unhandled form state");
  }

  public CompletionStage<SearchServiceResult> physicalGoodsSearch() {
    String searchTerms = PhysicalGoodsSearchController.getSearchTerms(dao.getPhysicalGoodsSearchForm().get());
    return searchServiceClient.get(searchTerms);
  }

}