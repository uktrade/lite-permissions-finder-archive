package controllers.search;

import static java.util.concurrent.CompletableFuture.completedFuture;
import static play.mvc.Results.ok;

import com.google.inject.Inject;
import components.common.journey.JourneyManager;
import components.persistence.PermissionsFinderDao;
import components.services.search.SearchServiceClient;
import controllers.ErrorController;
import journey.Events;
import journey.helpers.ControlCodeSubJourneyHelper;
import models.controlcode.ControlCodeSubJourney;
import models.search.SearchBaseDisplay;
import play.data.Form;
import play.data.FormFactory;
import play.libs.concurrent.HttpExecutionContext;
import play.mvc.Result;
import views.html.search.physicalGoodsSearch;

import java.util.Optional;
import java.util.concurrent.CompletionStage;

public class PhysicalGoodsSearchController extends SearchController {

  @Inject
  public PhysicalGoodsSearchController(JourneyManager journeyManager,
                                       FormFactory formFactory,
                                       PermissionsFinderDao permissionsFinderDao,
                                       HttpExecutionContext httpExecutionContext,
                                       SearchServiceClient searchServiceClient,
                                       ErrorController errorController) {
    super(journeyManager, formFactory, permissionsFinderDao, httpExecutionContext, searchServiceClient, errorController);
  }

  private CompletionStage<Result> renderForm(ControlCodeSubJourney controlCodeSubJourney) {
    Optional<ControlCodeSearchForm> templateFormOptional = permissionsFinderDao.getPhysicalGoodsSearchForm(controlCodeSubJourney);
    ControlCodeSearchForm templateForm = templateFormOptional.isPresent() ? templateFormOptional.get() : new ControlCodeSearchForm();
    return completedFuture(ok(physicalGoodsSearch.render(new SearchBaseDisplay(controlCodeSubJourney, searchForm(templateForm)))));
  }

  public CompletionStage<Result> renderSearchForm() {
    return renderForm(models.controlcode.ControlCodeSubJourney.PHYSICAL_GOODS_SEARCH);
  }

  public CompletionStage<Result> renderSearchRelatedToForm (String goodsTypeText) {
    return ControlCodeSubJourneyHelper.getSearchRelatedToPhysicalGoodsResult(goodsTypeText, this::renderForm);
  }

  private CompletionStage<Result> handleSubmit(ControlCodeSubJourney controlCodeSubJourney) {
    Form<ControlCodeSearchForm> form = bindSearchForm();

    if(form.hasErrors()) {
      return completedFuture(ok(physicalGoodsSearch.render(new SearchBaseDisplay(controlCodeSubJourney, form))));
    }
    permissionsFinderDao.savePhysicalGoodSearchForm(controlCodeSubJourney, form.get());
    permissionsFinderDao.savePhysicalGoodSearchPaginationDisplayCount(controlCodeSubJourney,
        PhysicalGoodsSearchResultsController.PAGINATION_SIZE);
    permissionsFinderDao.clearPhysicalGoodSearchLastChosenControlCode(controlCodeSubJourney);
    return journeyManager.performTransition(Events.SEARCH_PHYSICAL_GOODS);
  }

  public CompletionStage<Result> handleSearchSubmit() {
    return handleSubmit(models.controlcode.ControlCodeSubJourney.PHYSICAL_GOODS_SEARCH);
  }

  public CompletionStage<Result> handleSearchRelatedToSubmit (String goodsTypeText) {
    return ControlCodeSubJourneyHelper.getSearchRelatedToPhysicalGoodsResult(goodsTypeText, this::handleSubmit);
  }

}