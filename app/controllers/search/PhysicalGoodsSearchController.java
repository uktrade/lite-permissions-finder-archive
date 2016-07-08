package controllers.search;

import static play.mvc.Results.ok;

import com.google.inject.Inject;
import controllers.ErrorController;
import play.data.Form;
import play.data.FormFactory;
import play.mvc.Result;
import views.html.search.physicalGoodsSearch;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;


public class PhysicalGoodsSearchController extends ControlCodeSearchController{

  private PhysicalGoodsSearchResultsController physicalGoodsSearchResultsController;

  @Inject
  public PhysicalGoodsSearchController(FormFactory formFactory, ControlCodeSearchClient controlCodeSearchClient,
                                       ErrorController errorController,
                                       PhysicalGoodsSearchResultsController physicalGoodsSearchResultsController) {
    super(formFactory, controlCodeSearchClient, errorController);
    this.physicalGoodsSearchResultsController = physicalGoodsSearchResultsController;
  }

  public Result renderForm() {
    return ok(physicalGoodsSearch.render(formFactory.form(ControlCodeSearchForm.class)));
  }

  public CompletionStage<Result> handleSubmit() {
    Form<ControlCodeSearchForm> form = bindForm();

    if(form.hasErrors()){
      return CompletableFuture.completedFuture(ok(physicalGoodsSearch.render(form)));
    }

    return physicalGoodsSearch(form)
        .thenApply(response -> {
          if (response.isOk()){
            return physicalGoodsSearchResultsController.renderForm(response.getSearchResults());
          }
          else {
            return errorController.renderForm("An issue occurred while processing your request, please try again later.");
          }
        });
  }
}