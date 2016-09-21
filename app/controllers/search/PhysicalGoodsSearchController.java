package controllers.search;

import static java.util.concurrent.CompletableFuture.completedFuture;
import static play.mvc.Results.ok;

import com.google.inject.Inject;
import components.common.journey.JourneyManager;
import components.persistence.PermissionsFinderDao;
import controllers.ErrorController;
import components.services.controlcode.search.SearchServiceClient;
import journey.Events;
import play.data.Form;
import play.data.FormFactory;
import play.libs.concurrent.HttpExecutionContext;
import play.mvc.Result;
import views.html.search.physicalGoodsSearch;

import java.util.Optional;
import java.util.concurrent.CompletionStage;


public class PhysicalGoodsSearchController extends SearchController {

  @Inject
  public PhysicalGoodsSearchController(JourneyManager jm,
                                       FormFactory formFactory,
                                       PermissionsFinderDao permissionsFinderDao,
                                       HttpExecutionContext ec,
                                       SearchServiceClient searchServiceClient,
                                       ErrorController errorController) {
    super(jm, formFactory, permissionsFinderDao, ec, searchServiceClient, errorController);
  }

  public Result renderForm() {
    Optional<ControlCodeSearchForm> templateFormOptional = permissionsFinderDao.getPhysicalGoodsSearchForm();
    ControlCodeSearchForm templateForm = templateFormOptional.isPresent() ? templateFormOptional.get() : new ControlCodeSearchForm();
    return ok(physicalGoodsSearch.render(searchForm(templateForm)));
  }

  public CompletionStage<Result> handleSubmit() {
    Form<ControlCodeSearchForm> form = bindSearchForm();

    if(form.hasErrors()){
      return completedFuture(ok(physicalGoodsSearch.render(form)));
    }
    permissionsFinderDao.savePhysicalGoodSearchForm(form.get());
    return jm.performTransition(Events.SEARCH_PHYSICAL_GOODS);
  }

}