package controllers.search;

import static play.mvc.Results.ok;

import com.google.inject.Inject;
import components.persistence.PermissionsFinderDao;
import controllers.ErrorController;
import components.services.controlcode.search.SearchServiceClient;
import play.data.Form;
import play.data.FormFactory;
import play.libs.concurrent.HttpExecutionContext;
import play.mvc.Result;
import views.html.search.physicalGoodsSearch;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;


public class PhysicalGoodsSearchController extends SearchController {

  private PhysicalGoodsSearchResultsController physicalGoodsSearchResultsController;

  @Inject
  public PhysicalGoodsSearchController(FormFactory formFactory,
                                       PermissionsFinderDao dao,
                                       HttpExecutionContext ec,
                                       SearchServiceClient searchServiceClient,
                                       ErrorController errorController,
                                       PhysicalGoodsSearchResultsController physicalGoodsSearchResultsController) {
    super(formFactory, dao, ec, searchServiceClient, errorController);
    this.physicalGoodsSearchResultsController = physicalGoodsSearchResultsController;
  }

  public Result renderForm() {
    return ok(physicalGoodsSearch.render(searchForm()));
  }

  public CompletionStage<Result> handleSubmit() {
    Form<ControlCodeSearchForm> form = bindSearchForm();

    if(form.hasErrors()){
      return CompletableFuture.completedFuture(ok(physicalGoodsSearch.render(form)));
    }

    return physicalGoodsSearch(form)
        .thenApplyAsync(response -> {
          if (response.isOk()){
            return physicalGoodsSearchResultsController.renderForm(response.getSearchResults());
          }
          else {
            return errorController.renderForm("An issue occurred while processing your request, please try again later.");
          }
        }, ec.current());
  }

  public CompletionStage<SearchServiceClient.Response> physicalGoodsSearch(Form<ControlCodeSearchForm> form) {
    String searchTerms = getSearchTerms(form);
    dao.savePhysicalGoodSearchTerms(searchTerms);
    return searchServiceClient.get(searchTerms);
  }
}