package controllers.search;

import static java.util.concurrent.CompletableFuture.completedFuture;
import static play.mvc.Results.badRequest;
import static play.mvc.Results.ok;

import com.google.inject.Inject;
import components.common.journey.JourneyManager;
import components.persistence.PermissionsFinderDao;
import components.services.controlcode.frontend.FrontendServiceClient;
import components.services.controlcode.search.SearchServiceClient;
import components.services.controlcode.search.SearchServiceResult;
import controllers.ErrorController;
import controllers.controlcode.ControlCodeController;
import journey.Events;
import play.data.Form;
import play.data.FormFactory;
import play.libs.concurrent.HttpExecutionContext;
import play.mvc.Result;
import views.html.search.physicalGoodsSearchResults;

import java.util.List;
import java.util.Optional;
import java.util.concurrent.CompletionStage;

public class PhysicalGoodsSearchResultsController extends SearchResultsController {

  private final HttpExecutionContext ec;
  private final PermissionsFinderDao dao;
  public static final int PAGINATION_SIZE = 5;

  @Inject
  public PhysicalGoodsSearchResultsController(JourneyManager jm,
                                              FormFactory formFactory,
                                              SearchServiceClient searchServiceClient,
                                              FrontendServiceClient frontendServiceClient,
                                              ControlCodeController controlCodeController,
                                              ErrorController errorController,
                                              HttpExecutionContext ec,
                                              PermissionsFinderDao dao) {
    super(jm, formFactory, searchServiceClient, frontendServiceClient, controlCodeController, errorController);
    this.ec = ec;
    this.dao = dao;
  }

  public CompletionStage<Result> renderForm() {
    return physicalGoodsSearch()
        .thenApplyAsync(response -> {
          List<SearchServiceResult> searchResults = response.getSearchResults();
          int displayCount = Math.min(searchResults.size(), PAGINATION_SIZE);
          dao.savePhysicalGoodSearchPaginationDisplayCount(displayCount);
          return ok(physicalGoodsSearchResults.render(searchResultsForm(), searchResults, displayCount));
        }, ec.current());
  }

  public CompletionStage<Result> handleSubmit() {
    Form<ControlCodeSearchResultsForm> form = bindSearchResultsForm();

    if (form.hasErrors()) {
      return physicalGoodsSearch()
          .thenApplyAsync(response -> {
            int displayCount = dao.getPhysicalGoodSearchPaginationDisplayCount();
            int newDisplayCount = Math.min(displayCount, response.getSearchResults().size());
            if (displayCount != newDisplayCount) {
              dao.savePhysicalGoodSearchPaginationDisplayCount(newDisplayCount);
            }
            return ok(physicalGoodsSearchResults.render(form, response.getSearchResults(), newDisplayCount));
          }, ec.current());
    }

    Optional<SearchResultAction> action = getAction(form.get());
    if (action.isPresent()){
      switch (action.get()) {
        case NONE_MATCHED:
          return jm.performTransition(Events.NONE_MATCHED);
        case SHORE_MORE:
          return physicalGoodsSearch()
              .thenApplyAsync(response -> {
                int displayCount = dao.getPhysicalGoodSearchPaginationDisplayCount();
                int newDisplayCount = Math.min(displayCount + PAGINATION_SIZE, response.getSearchResults().size());
                if (displayCount != newDisplayCount) {
                  dao.savePhysicalGoodSearchPaginationDisplayCount(newDisplayCount);
                }
                return ok(physicalGoodsSearchResults.render(form, response.getSearchResults(), newDisplayCount));
              }, ec.current());
      }
    }

    Optional<String> result = getResult(form.get());
    if (result.isPresent()) {
      dao.savePhysicalGoodControlCode(result.get());
      return jm.performTransition(Events.CONTROL_CODE_SELECTED);
    }

    return completedFuture(badRequest("Invalid form state"));
  }

  public CompletionStage<SearchServiceClient.Response> physicalGoodsSearch() {
    String searchTerms = PhysicalGoodsSearchController.getSearchTerms(dao.getPhysicalGoodsSearchForm().get());
    return searchServiceClient.get(searchTerms);
  }

}