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
import journey.helpers.ControlCodeJourneyHelper;
import journey.helpers.SoftwareJourneyHelper;
import models.GoodsType;
import models.search.SearchResultsBaseDisplay;
import models.controlcode.ControlCodeJourney;
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
  private final ControlCodeJourneyHelper controlCodeJourneyHelper;
  private final SoftwareJourneyHelper softwareJourneyHelper;

  @Inject
  public PhysicalGoodsSearchResultsController(JourneyManager journeyManager,
                                              FormFactory formFactory,
                                              SearchServiceClient searchServiceClient,
                                              FrontendServiceClient frontendServiceClient,
                                              ControlCodeController controlCodeController,
                                              ErrorController errorController,
                                              HttpExecutionContext httpExecutionContext,
                                              PermissionsFinderDao permissionsFinderDao,
                                              ControlCodeJourneyHelper controlCodeJourneyHelper,
                                              SoftwareJourneyHelper softwareJourneyHelper) {
    super(journeyManager, formFactory, searchServiceClient, frontendServiceClient, controlCodeController, errorController);
    this.httpExecutionContext = httpExecutionContext;
    this.permissionsFinderDao = permissionsFinderDao;
    this.controlCodeJourneyHelper = controlCodeJourneyHelper;
    this.softwareJourneyHelper = softwareJourneyHelper;
  }

  private CompletionStage<Result> renderForm(ControlCodeJourney controlCodeJourney) {
    return physicalGoodsSearch(controlCodeJourney)
        .thenApplyAsync(result -> {
          int displayCount = Math.min(result.results.size(), PAGINATION_SIZE);
          Optional<Integer> optionalDisplayCount = permissionsFinderDao.getPhysicalGoodSearchPaginationDisplayCount(controlCodeJourney);
          if (optionalDisplayCount.isPresent()) {
            displayCount = Math.min(result.results.size(), optionalDisplayCount.get());
          }
          else {
            permissionsFinderDao.savePhysicalGoodSearchPaginationDisplayCount(controlCodeJourney, displayCount);
          }
          String lastChosenControlCode = permissionsFinderDao.getPhysicalGoodSearchLastChosenControlCode(controlCodeJourney);
          SearchResultsBaseDisplay display = new SearchResultsBaseDisplay(controlCodeJourney, searchResultsForm(), GoodsType.PHYSICAL,
              result.results, displayCount, lastChosenControlCode);
          return ok(physicalGoodsSearchResults.render(display));
        }, httpExecutionContext.current());
  }

  public CompletionStage<Result> renderForm() {
    return renderForm(ControlCodeJourney.PHYSICAL_GOODS_SEARCH);
  }

  public CompletionStage<Result> renderRelatedToSoftwareForm() {
    return renderForm(ControlCodeJourney.PHYSICAL_GOODS_SEARCH_RELATED_TO_SOFTWARE);
  }

  private CompletionStage<Result> handleSubmit(ControlCodeJourney controlCodeJourney) {
    Form<ControlCodeSearchResultsForm> form = bindSearchResultsForm();

    if (form.hasErrors()) {
      return physicalGoodsSearch(controlCodeJourney)
          .thenApplyAsync(result -> {
            int displayCount = Integer.parseInt(form.field("resultsDisplayCount").value());
            int newDisplayCount = Math.min(displayCount, result.results.size());
            if (displayCount != newDisplayCount) {
              permissionsFinderDao.savePhysicalGoodSearchPaginationDisplayCount(controlCodeJourney, newDisplayCount);
            }
            SearchResultsBaseDisplay display = new SearchResultsBaseDisplay(controlCodeJourney, form, GoodsType.PHYSICAL,
                result.results, newDisplayCount);
            return ok(physicalGoodsSearchResults.render(display));
          }, httpExecutionContext.current());
    }

    Optional<SearchResultAction> action = getAction(form.get());
    if (action.isPresent()){
      switch (action.get()) {
        case NONE_MATCHED:
          return noneMatched(controlCodeJourney);
        case SHORE_MORE:
          return physicalGoodsSearch(controlCodeJourney)
              .thenApplyAsync(result -> {
                int displayCount = Integer.parseInt(form.get().resultsDisplayCount);
                int newDisplayCount = Math.min(displayCount + PAGINATION_SIZE, result.results.size());
                if (displayCount != newDisplayCount) {
                  permissionsFinderDao.savePhysicalGoodSearchPaginationDisplayCount(controlCodeJourney, newDisplayCount);
                }
                SearchResultsBaseDisplay display = new SearchResultsBaseDisplay(controlCodeJourney, form, GoodsType.PHYSICAL, result.results, newDisplayCount);
                return ok(physicalGoodsSearchResults.render(display));
              }, httpExecutionContext.current());
      }
    }

    Optional<String> result = getResult(form.get());
    if (result.isPresent()) {
      int displayCount = Integer.parseInt(form.get().resultsDisplayCount);
      controlCodeJourneyHelper.clearControlCodeJourneyDaoFieldsIfChanged(controlCodeJourney, result.get());
      permissionsFinderDao.savePhysicalGoodSearchPaginationDisplayCount(controlCodeJourney, displayCount);
      permissionsFinderDao.saveSelectedControlCode(controlCodeJourney, result.get());
      permissionsFinderDao.savePhysicalGoodSearchLastChosenControlCode(controlCodeJourney, result.get());
      return journeyManager.performTransition(Events.CONTROL_CODE_SELECTED);
    }

    throw new FormStateException("Unhandled form state");
  }

  public CompletionStage<Result> handleSubmit() {
    return handleSubmit(ControlCodeJourney.PHYSICAL_GOODS_SEARCH);
  }

  public CompletionStage<Result> handleRelatedToSoftwareSubmit() {
    return handleSubmit(ControlCodeJourney.PHYSICAL_GOODS_SEARCH_RELATED_TO_SOFTWARE);
  }

  public CompletionStage<SearchServiceResult> physicalGoodsSearch(ControlCodeJourney controlCodeJourney) {
    String searchTerms = PhysicalGoodsSearchController.getSearchTerms(permissionsFinderDao.getPhysicalGoodsSearchForm(controlCodeJourney).get());
    return searchServiceClient.get(searchTerms);
  }

  private CompletionStage<Result> noneMatched(ControlCodeJourney controlCodeJourney){
    if (controlCodeJourney == ControlCodeJourney.PHYSICAL_GOODS_SEARCH ||
        controlCodeJourney == ControlCodeJourney.SOFTWARE_CONTROLS ||
        controlCodeJourney == ControlCodeJourney.SOFTWARE_CONTROLS_RELATED_TO_PHYSICAL_GOODS) {
      return journeyManager.performTransition(Events.NONE_MATCHED);
    }
    else if (controlCodeJourney == ControlCodeJourney.PHYSICAL_GOODS_SEARCH_RELATED_TO_SOFTWARE) {
      return softwareJourneyHelper.performCatchallSoftwareControlsTransition();
    }
    else {
      throw new RuntimeException(String.format("Unexpected member of ControlCodeJourney enum: \"%s\""
          , controlCodeJourney.toString()));
    }
  }

}