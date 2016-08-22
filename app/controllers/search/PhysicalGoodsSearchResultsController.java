package controllers.search;

import static play.mvc.Results.ok;

import com.google.inject.Inject;
import components.persistence.PermissionsFinderDao;
import components.services.controlcode.frontend.FrontendServiceClient;
import components.services.controlcode.search.SearchServiceResult;
import controllers.ErrorController;
import controllers.controlcode.ControlCodeController;
import play.data.Form;
import play.data.FormFactory;
import play.libs.concurrent.HttpExecutionContext;
import play.mvc.Result;
import views.html.search.physicalGoodsSearchResults;

import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;

public class PhysicalGoodsSearchResultsController extends SearchResultsController {

  private final NoneDescribedController noneDescribedController;

  private final HttpExecutionContext ec;

  private final PermissionsFinderDao dao;

  @Inject
  public PhysicalGoodsSearchResultsController(FormFactory formFactory,
                                              FrontendServiceClient frontendServiceClient,
                                              ControlCodeController controlCodeController,
                                              ErrorController errorController,
                                              NoneDescribedController noneDescribedController,
                                              HttpExecutionContext ec,
                                              PermissionsFinderDao dao) {
    super(formFactory, frontendServiceClient, controlCodeController, errorController);
    this.noneDescribedController = noneDescribedController;
    this.ec = ec;
    this.dao = dao;
  }

  public Result renderForm(List<SearchServiceResult> searchResults){
    return ok(physicalGoodsSearchResults.render(searchResultsForm(), searchResults, !searchResults.isEmpty()));
  }

  public CompletionStage<Result> handleSubmit() {
    Form<ControlCodeSearchResultsForm> form = bindSearchResultsForm();
    String result = form.get().result;
    String action = form.get().action;

    if (form.hasErrors()) {
      //TODO Re-render the page here with an error summary
      return CompletableFuture.completedFuture(errorController.renderForm("Something is wrong with the page you were on, please go back refresh the page."));
    }

    if (ControlCodeSearchResultsForm.isActionValid(action)){
      return CompletableFuture.completedFuture(noneDescribedController.render());
    }

    if (ControlCodeSearchResultsForm.isResultValid(result)) {
      return frontendServiceClient.get(result)
          .thenApplyAsync(response -> {
            if (response.isOk()){
              dao.savePhysicalGoodControlCode(response.getFrontendServiceResult().controlCodeData.controlCode);
            }
            if (response.isOk()) {
              return controlCodeController.renderForm(response.getFrontendServiceResult());
            }
            return errorController.renderForm("An issue occurred while processing your request, please try again later.");
          }, ec.current());
    }

    return CompletableFuture.completedFuture(errorController.renderForm("An issue occurred while processing your request, please try again later."));
  }

}