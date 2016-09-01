package controllers.search;

import static play.mvc.Results.ok;

import com.google.inject.Inject;
import components.persistence.PermissionsFinderDao;
import components.services.controlcode.frontend.FrontendServiceClient;
import components.services.controlcode.search.SearchServiceClient;
import components.services.controlcode.search.SearchServiceResult;
import controllers.ErrorController;
import controllers.controlcode.ControlCodeController;
import play.data.Form;
import play.data.FormFactory;
import play.libs.concurrent.HttpExecutionContext;
import play.mvc.Result;
import views.html.search.physicalGoodsSearchResults;

import java.util.List;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;

public class PhysicalGoodsSearchResultsController extends SearchResultsController {

  private final NoneDescribedController noneDescribedController;

  private final HttpExecutionContext ec;

  private final PermissionsFinderDao dao;

  public static final int PAGINATION_SIZE = 5;

  @Inject
  public PhysicalGoodsSearchResultsController(FormFactory formFactory,
                                              SearchServiceClient searchServiceClient,
                                              FrontendServiceClient frontendServiceClient,
                                              ControlCodeController controlCodeController,
                                              ErrorController errorController,
                                              NoneDescribedController noneDescribedController,
                                              HttpExecutionContext ec,
                                              PermissionsFinderDao dao) {
    super(formFactory, searchServiceClient, frontendServiceClient, controlCodeController, errorController);
    this.noneDescribedController = noneDescribedController;
    this.ec = ec;
    this.dao = dao;
  }

  public Result renderForm(List<SearchServiceResult> searchResults){
    int displayCount = Math.min(searchResults.size(), PAGINATION_SIZE);
    dao.savePhysicalGoodSearchPaginationDisplayCount(displayCount);
    return ok(physicalGoodsSearchResults.render(searchResultsForm(), searchResults, displayCount));
  }

  public CompletionStage<Result> handleSubmit() {
    Form<ControlCodeSearchResultsForm> form = bindSearchResultsForm();
    int currentDisplayCount = dao.getPhysicalGoodSearchPaginationDisplayCount();

    if (form.hasErrors()) {
      return physicalGoodsSearch()
          .thenApplyAsync(response -> ok(physicalGoodsSearchResults.render(bindSearchResultsForm(), response.getSearchResults(), currentDisplayCount)), ec.current());
    }

    Optional<SearchResultAction> action = form.get().getAction();
    if (action.isPresent()){
      switch (action.get()) {
        case NONE_MATCHED:
          return CompletableFuture.completedFuture(noneDescribedController.render());
        case SHORE_MORE:
          return physicalGoodsSearch().thenApplyAsync(response -> {
            if (response.isOk()) {
              int newDisplayCount = Math.min(currentDisplayCount + PAGINATION_SIZE, response.getSearchResults().size());
              dao.savePhysicalGoodSearchPaginationDisplayCount(newDisplayCount);
              return ok(physicalGoodsSearchResults.render(bindSearchResultsForm(), response.getSearchResults(), newDisplayCount));
            }
            return errorController.renderForm("An issue occurred while processing your request, please try again later.");
          }, ec.current());
      }
    }

    Optional<String> result = form.get().getResult();
    if (result.isPresent()) {
      return frontendServiceClient.get(result.get())
          .thenApplyAsync(response -> {
            if (response.isOk()){
              dao.savePhysicalGoodControlCode(response.getFrontendServiceResult().controlCodeData.controlCode);
              return controlCodeController.renderForm(response.getFrontendServiceResult());
            }
            return errorController.renderForm("An issue occurred while processing your request, please try again later.");
          }, ec.current());
    }

    return CompletableFuture.completedFuture(errorController.renderForm("An issue occurred while processing your request, please try again later."));
  }

  public CompletionStage<SearchServiceClient.Response> physicalGoodsSearch() {
    String searchTerms = dao.getPhysicalGoodSearchTerms();
    return searchServiceClient.get(searchTerms);
  }

}