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
import journey.helpers.ControlCodeSubJourneyHelper;
import journey.helpers.SoftTechJourneyHelper;
import models.GoodsType;
import models.controlcode.ControlCodeSubJourney;
import models.search.SearchResultsBaseDisplay;
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
  private final SoftTechJourneyHelper softTechJourneyHelper;

  @Inject
  public PhysicalGoodsSearchResultsController(JourneyManager journeyManager,
                                              FormFactory formFactory,
                                              SearchServiceClient searchServiceClient,
                                              FrontendServiceClient frontendServiceClient,
                                              ControlCodeController controlCodeController,
                                              ErrorController errorController,
                                              HttpExecutionContext httpExecutionContext,
                                              PermissionsFinderDao permissionsFinderDao,
                                              SoftTechJourneyHelper softTechJourneyHelper) {
    super(journeyManager, formFactory, searchServiceClient, frontendServiceClient, controlCodeController, errorController);
    this.httpExecutionContext = httpExecutionContext;
    this.permissionsFinderDao = permissionsFinderDao;
    this.softTechJourneyHelper = softTechJourneyHelper;
  }

  private CompletionStage<Result> renderForm(ControlCodeSubJourney controlCodeSubJourney) {
    return physicalGoodsSearch(controlCodeSubJourney)
        .thenApplyAsync(result -> {
          int displayCount = Math.min(result.results.size(), PAGINATION_SIZE);
          Optional<Integer> optionalDisplayCount = permissionsFinderDao.getPhysicalGoodSearchPaginationDisplayCount(controlCodeSubJourney);
          if (optionalDisplayCount.isPresent()) {
            displayCount = Math.min(result.results.size(), optionalDisplayCount.get());
          }
          else {
            permissionsFinderDao.savePhysicalGoodSearchPaginationDisplayCount(controlCodeSubJourney, displayCount);
          }
          String lastChosenControlCode = permissionsFinderDao.getPhysicalGoodSearchLastChosenControlCode(controlCodeSubJourney);
          SearchResultsBaseDisplay display = new SearchResultsBaseDisplay(controlCodeSubJourney, searchResultsForm(), GoodsType.PHYSICAL,
              result.results, displayCount, lastChosenControlCode);
          return ok(physicalGoodsSearchResults.render(display));
        }, httpExecutionContext.current());
  }

  public CompletionStage<Result> renderSearchForm() {
    return renderForm(models.controlcode.ControlCodeSubJourney.PHYSICAL_GOODS_SEARCH);
  }

  public CompletionStage<Result> renderSearchRelatedToForm(String goodsTypeText) {
    return ControlCodeSubJourneyHelper.getSearchRelatedToPhysicalGoodsResult(goodsTypeText, this::renderForm);
  }

  private CompletionStage<Result> handleSubmit(ControlCodeSubJourney controlCodeSubJourney) {
    Form<ControlCodeSearchResultsForm> form = bindSearchResultsForm();

    if (form.hasErrors()) {
      return physicalGoodsSearch(controlCodeSubJourney)
          .thenApplyAsync(result -> {
            int displayCount = Integer.parseInt(form.field("resultsDisplayCount").value());
            int newDisplayCount = Math.min(displayCount, result.results.size());
            if (displayCount != newDisplayCount) {
              permissionsFinderDao.savePhysicalGoodSearchPaginationDisplayCount(controlCodeSubJourney, newDisplayCount);
            }
            SearchResultsBaseDisplay display = new SearchResultsBaseDisplay(controlCodeSubJourney, form, GoodsType.PHYSICAL,
                result.results, newDisplayCount);
            return ok(physicalGoodsSearchResults.render(display));
          }, httpExecutionContext.current());
    }

    Optional<SearchResultAction> action = getAction(form.get());
    if (action.isPresent()){
      switch (action.get()) {
        case NONE_MATCHED:
          return noneMatched(controlCodeSubJourney);
        case SHORE_MORE:
          return physicalGoodsSearch(controlCodeSubJourney)
              .thenApplyAsync(result -> {
                int displayCount = Integer.parseInt(form.get().resultsDisplayCount);
                int newDisplayCount = Math.min(displayCount + PAGINATION_SIZE, result.results.size());
                if (displayCount != newDisplayCount) {
                  permissionsFinderDao.savePhysicalGoodSearchPaginationDisplayCount(controlCodeSubJourney, newDisplayCount);
                }
                SearchResultsBaseDisplay display = new SearchResultsBaseDisplay(controlCodeSubJourney, form, GoodsType.PHYSICAL, result.results, newDisplayCount);
                return ok(physicalGoodsSearchResults.render(display));
              }, httpExecutionContext.current());
        case EDIT_DESCRIPTION:
          return editDescription(controlCodeSubJourney);
        case CONTINUE:
          return continueWithService(controlCodeSubJourney);
      }
    }

    Optional<String> result = getResult(form.get());
    if (result.isPresent()) {
      int displayCount = Integer.parseInt(form.get().resultsDisplayCount);
      permissionsFinderDao.clearAndUpdateControlCodeJourneyDaoFieldsIfChanged(controlCodeSubJourney, result.get());
      permissionsFinderDao.savePhysicalGoodSearchPaginationDisplayCount(controlCodeSubJourney, displayCount);
      permissionsFinderDao.savePhysicalGoodSearchLastChosenControlCode(controlCodeSubJourney, result.get());
      return journeyManager.performTransition(Events.CONTROL_CODE_SELECTED);
    }

    throw new FormStateException("Unhandled form state");
  }

  public CompletionStage<Result> handleSearchSubmit() {
    return handleSubmit(models.controlcode.ControlCodeSubJourney.PHYSICAL_GOODS_SEARCH);
  }

  public CompletionStage<Result> handleSearchRelatedToSubmit(String goodsTypeText) {
    return ControlCodeSubJourneyHelper.getSearchRelatedToPhysicalGoodsResult(goodsTypeText, this::handleSubmit);
  }

  public CompletionStage<SearchServiceResult> physicalGoodsSearch(ControlCodeSubJourney controlCodeSubJourney) {
    String searchTerms = PhysicalGoodsSearchController.getSearchTerms(permissionsFinderDao.getPhysicalGoodsSearchForm(controlCodeSubJourney).get());
    return searchServiceClient.get(searchTerms);
  }

  private CompletionStage<Result> noneMatched(ControlCodeSubJourney controlCodeSubJourney){
      if (controlCodeSubJourney == models.controlcode.ControlCodeSubJourney.PHYSICAL_GOODS_SEARCH ||
          controlCodeSubJourney == models.controlcode.ControlCodeSubJourney.SOFTWARE_CONTROLS ||
          controlCodeSubJourney == models.controlcode.ControlCodeSubJourney.TECHNOLOGY_CONTROLS) {
      return journeyManager.performTransition(Events.NONE_MATCHED);
    }
    else if (controlCodeSubJourney == models.controlcode.ControlCodeSubJourney.PHYSICAL_GOODS_SEARCH_RELATED_TO_SOFTWARE ||
          controlCodeSubJourney == models.controlcode.ControlCodeSubJourney.PHYSICAL_GOODS_SEARCH_RELATED_TO_TECHNOLOGY) {
      return continueWithService(controlCodeSubJourney);
    }
    else {
      throw new RuntimeException(String.format("Unexpected member of ControlCodeSubJourney enum: \"%s\""
          , controlCodeSubJourney.toString()));
    }
  }

  private CompletionStage<Result> editDescription(ControlCodeSubJourney controlCodeSubJourney) {
    if (controlCodeSubJourney == ControlCodeSubJourney.PHYSICAL_GOODS_SEARCH_RELATED_TO_SOFTWARE ||
        controlCodeSubJourney == models.controlcode.ControlCodeSubJourney.PHYSICAL_GOODS_SEARCH_RELATED_TO_TECHNOLOGY) {
      return journeyManager.performTransition(Events.EDIT_SEARCH_DESCRIPTION);
    }
    else {
      throw new RuntimeException(String.format("Unexpected member of ControlCodeSubJourney enum: \"%s\""
          , controlCodeSubJourney.toString()));
    }
  }

  private CompletionStage<Result> continueWithService(ControlCodeSubJourney controlCodeSubJourney) {
    if (controlCodeSubJourney == models.controlcode.ControlCodeSubJourney.PHYSICAL_GOODS_SEARCH_RELATED_TO_SOFTWARE) {
      return softTechJourneyHelper.performCatchallSoftTechControlsTransition(GoodsType.SOFTWARE);
    }
    else if (controlCodeSubJourney == models.controlcode.ControlCodeSubJourney.PHYSICAL_GOODS_SEARCH_RELATED_TO_TECHNOLOGY) {
      return softTechJourneyHelper.performCatchallSoftTechControlsTransition(GoodsType.TECHNOLOGY);
    }
    else {
      throw new RuntimeException(String.format("Unexpected member of ControlCodeSubJourney enum: \"%s\""
          , controlCodeSubJourney.toString()));
    }
  }

}