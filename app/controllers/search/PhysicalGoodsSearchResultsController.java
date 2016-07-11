package controllers.search;

import static play.mvc.Results.ok;

import com.google.inject.Inject;
import controllers.services.controlcode.search.SearchServiceResults;
import play.data.Form;
import play.data.FormFactory;
import play.mvc.Result;
import views.html.search.physicalGoodsSearchResults;

public class PhysicalGoodsSearchResultsController extends SearchResultsController {

  @Inject
  public PhysicalGoodsSearchResultsController(FormFactory formFactory) {
    super(formFactory);
  }

  public Result renderForm(SearchServiceResults searchResults){
    return ok(physicalGoodsSearchResults.render(searchResults));
  }

  public Result handleSubmit() {
    Form<ControlCodeSearchResultsForm> form = bindForm();
    return ok("submitted " +form.field("result").value());
  }
}