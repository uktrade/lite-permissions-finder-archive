package controllers.search;

import static play.mvc.Results.ok;

import com.google.inject.Inject;
import controllers.ErrorController;
import controllers.services.controlcode.lookup.LookupServiceClient;
import controllers.services.controlcode.search.SearchServiceResult;
import play.data.Form;
import play.data.FormFactory;
import play.libs.Json;
import play.mvc.Result;
import views.html.search.physicalGoodsSearchResults;

import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;

public class PhysicalGoodsSearchResultsController extends SearchResultsController {

  @Inject
  public PhysicalGoodsSearchResultsController(FormFactory formFactory, LookupServiceClient lookupServiceClient, ErrorController errorController) {
    super(formFactory, lookupServiceClient, errorController);
  }

  public Result renderForm(List<SearchServiceResult> searchResults){
    return ok(physicalGoodsSearchResults.render(searchResults));
  }

  public CompletionStage<Result> handleSubmit() {
    Form<ControlCodeSearchResultsForm> form = bindForm();
    String result = form.field("result").value();

    if (form.hasErrors() || result == null || result.isEmpty()) {
      return CompletableFuture.completedFuture(errorController.renderForm("Something is wrong with the page you were on, please go back refresh the page."));
    }

    return lookupServiceClient.lookup(result).thenApply(response -> {
      if (response.isOk()) {
        return ok(Json.toJson(response.getLookupServiceResult()));
      }
      else {
        return errorController.renderForm("An issue occurred while processing your request, please try again later.");
      }
    });
  }
}