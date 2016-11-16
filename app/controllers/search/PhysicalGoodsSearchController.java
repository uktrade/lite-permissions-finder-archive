package controllers.search;

import static java.util.concurrent.CompletableFuture.completedFuture;
import static play.mvc.Results.ok;

import com.google.inject.Inject;
import components.common.journey.JourneyManager;
import components.persistence.PermissionsFinderDao;
import controllers.ErrorController;
import components.services.search.SearchServiceClient;
import journey.Events;
import models.search.SearchBaseDisplay;
import models.controlcode.ControlCodeJourney;
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

  private Result renderForm(ControlCodeJourney controlCodeJourney) {
    Optional<ControlCodeSearchForm> templateFormOptional = permissionsFinderDao.getPhysicalGoodsSearchForm(controlCodeJourney);
    ControlCodeSearchForm templateForm = templateFormOptional.isPresent() ? templateFormOptional.get() : new ControlCodeSearchForm();
    return ok(physicalGoodsSearch.render(new SearchBaseDisplay(controlCodeJourney, searchForm(templateForm))));
  }

  public Result renderForm() {
    return renderForm(ControlCodeJourney.PHYSICAL_GOODS_SEARCH);
  }

  public Result renderRelatedToSoftwareForm() {
    return renderForm(ControlCodeJourney.PHYSICAL_GOODS_SEARCH_RELATED_TO_SOFTWARE);
  }

  private CompletionStage<Result> handleSubmit(ControlCodeJourney controlCodeJourney) {
    Form<ControlCodeSearchForm> form = bindSearchForm();

    if(form.hasErrors()){
      return completedFuture(ok(physicalGoodsSearch.render(new SearchBaseDisplay(controlCodeJourney, form))));
    }
    permissionsFinderDao.savePhysicalGoodSearchForm(controlCodeJourney, form.get());
    permissionsFinderDao.savePhysicalGoodSearchPaginationDisplayCount(controlCodeJourney,
        PhysicalGoodsSearchResultsController.PAGINATION_SIZE);
    permissionsFinderDao.clearPhysicalGoodSearchLastChosenControlCode(controlCodeJourney);
    return journeyManager.performTransition(Events.SEARCH_PHYSICAL_GOODS);
  }

  public CompletionStage<Result> handleSubmit() {
    return handleSubmit(ControlCodeJourney.PHYSICAL_GOODS_SEARCH);
  }

  public CompletionStage<Result> handleRelatedToSoftwareSubmit() {
    return handleSubmit(ControlCodeJourney.PHYSICAL_GOODS_SEARCH_RELATED_TO_SOFTWARE);
  }

}