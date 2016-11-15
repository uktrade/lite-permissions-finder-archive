package controllers.search;

import static play.mvc.Results.ok;

import com.google.inject.Inject;
import components.common.journey.JourneyManager;
import components.persistence.PermissionsFinderDao;
import components.services.controlcode.FrontendServiceClient;
import components.services.search.SearchServiceClient;
import components.services.search.SearchServiceResult;
import controllers.ErrorController;
import controllers.controlcode.ControlCodeController;
import exceptions.FormStateException;
import journey.Events;
import models.GoodsType;
import models.SearchResultsBaseDisplay;
import play.data.Form;
import play.data.FormFactory;
import play.libs.concurrent.HttpExecutionContext;
import play.mvc.Result;
import views.html.search.physicalGoodsSearchResults;

import java.util.Optional;
import java.util.concurrent.CompletionStage;

public class PhysicalGoodsSearchResultsController extends SearchResultsController {

  private final HttpExecutionContext httpExecutionContext;
  private final PermissionsFinderDao permissionsFinderDao;

  @Inject
  public PhysicalGoodsSearchResultsController(JourneyManager journeyManager,
                                              FormFactory formFactory,
                                              SearchServiceClient searchServiceClient,
                                              FrontendServiceClient frontendServiceClient,
                                              ControlCodeController controlCodeController,
                                              ErrorController errorController,
                                              HttpExecutionContext httpExecutionContext,
                                              PermissionsFinderDao permissionsFinderDao) {
    super(journeyManager, formFactory, searchServiceClient, frontendServiceClient, controlCodeController, errorController);
    this.httpExecutionContext = httpExecutionContext;
    this.permissionsFinderDao = permissionsFinderDao;
  }

  public CompletionStage<Result> renderForm() {
    return physicalGoodsSearch()
        .thenApplyAsync(result -> {
          int displayCount = Math.min(result.results.size(), PAGINATION_SIZE);
          Optional<Integer> optionalDisplayCount = permissionsFinderDao.getPhysicalGoodSearchPaginationDisplayCount();
          if (optionalDisplayCount.isPresent()) {
            displayCount = Math.min(result.results.size(), optionalDisplayCount.get());
          }
          else {
            permissionsFinderDao.savePhysicalGoodSearchPaginationDisplayCount(displayCount);
          }
          String lastChosenControlCode = permissionsFinderDao.getPhysicalGoodSearchLastChosenControlCode();
          SearchResultsBaseDisplay display = new SearchResultsBaseDisplay(searchResultsForm(), GoodsType.PHYSICAL,
              result.results, displayCount, lastChosenControlCode);
          return ok(physicalGoodsSearchResults.render(display));
        }, httpExecutionContext.current());
  }

  public CompletionStage<Result> renderRelatedToSoftwareForm() {
    return renderForm();
  }

  public CompletionStage<Result> handleSubmit() {
    Form<ControlCodeSearchResultsForm> form = bindSearchResultsForm();

    if (form.hasErrors()) {
      return physicalGoodsSearch()
          .thenApplyAsync(result -> {
            int displayCount = Integer.parseInt(form.field("resultsDisplayCount").value());
            int newDisplayCount = Math.min(displayCount, result.results.size());
            if (displayCount != newDisplayCount) {
              permissionsFinderDao.savePhysicalGoodSearchPaginationDisplayCount(newDisplayCount);
            }
            SearchResultsBaseDisplay display = new SearchResultsBaseDisplay(form, GoodsType.PHYSICAL,
                result.results, newDisplayCount);
            return ok(physicalGoodsSearchResults.render(display));
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
                int displayCount = Integer.parseInt(form.get().resultsDisplayCount);
                int newDisplayCount = Math.min(displayCount + PAGINATION_SIZE, result.results.size());
                if (displayCount != newDisplayCount) {
                  permissionsFinderDao.savePhysicalGoodSearchPaginationDisplayCount(newDisplayCount);
                }
                SearchResultsBaseDisplay display = new SearchResultsBaseDisplay(form, GoodsType.PHYSICAL, result.results, newDisplayCount);
                return ok(physicalGoodsSearchResults.render(display));
              }, httpExecutionContext.current());
      }
    }

    Optional<String> result = getResult(form.get());
    if (result.isPresent()) {
      int displayCount = Integer.parseInt(form.get().resultsDisplayCount);
      permissionsFinderDao.savePhysicalGoodSearchPaginationDisplayCount(displayCount);
      permissionsFinderDao.savePhysicalGoodControlCode(result.get());
      permissionsFinderDao.savePhysicalGoodSearchLastChosenControlCode(result.get());
      clearControlCodeDaoFields();
      return journeyManager.performTransition(Events.CONTROL_CODE_SELECTED);
    }

    throw new FormStateException("Unhandled form state");
  }

  public CompletionStage<SearchServiceResult> physicalGoodsSearch() {
    String searchTerms = PhysicalGoodsSearchController.getSearchTerms(permissionsFinderDao.getPhysicalGoodsSearchForm().get());
    return searchServiceClient.get(searchTerms);
  }

  private void clearControlCodeDaoFields() {
    permissionsFinderDao.clearControlCodeApplies();
    permissionsFinderDao.clearControlCodeDecontrolsApply();
    permissionsFinderDao.clearControlCodeAdditionalSpecificationsApply();
    permissionsFinderDao.clearControlCodeTechnicalNotesApply();
  }

}