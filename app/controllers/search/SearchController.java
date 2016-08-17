package controllers.search;

import components.services.controlcode.search.SearchServiceClient;
import controllers.ErrorController;
import play.data.Form;
import play.data.FormFactory;
import play.data.validation.Constraints.Required;

import java.util.Arrays;
import java.util.concurrent.CompletionStage;
import java.util.stream.Collectors;

public class SearchController {

  private final FormFactory formFactory;

  protected final SearchServiceClient searchServiceClient;

  protected final ErrorController errorController;

  public SearchController(FormFactory formFactory, SearchServiceClient searchServiceClient,
                          ErrorController errorController) {
    this.formFactory = formFactory;
    this.searchServiceClient = searchServiceClient;
    this.errorController = errorController;
  }

  public CompletionStage<SearchServiceClient.Response> physicalGoodsSearch(Form<ControlCodeSearchForm> form) {
    return searchServiceClient.get(getSearchTerms(form));
  }

  public Form<ControlCodeSearchForm> searchForm(){
    return formFactory.form(ControlCodeSearchForm.class);
  }

  public Form<ControlCodeSearchForm> bindSearchForm(){
    return searchForm().bindFromRequest();
  }

  public String getSearchTerms(Form<ControlCodeSearchForm> form){
    return Arrays.asList(
        form.get().description,
        form.get().component,
        form.get().brand,
        form.get().partNumber,
        form.get().hsCode
    ).stream()
        .filter(fv -> fv != null && !fv.isEmpty())
        .collect(Collectors.joining(", "));
  }

  public static class ControlCodeSearchForm {

    @Required(message = "You must enter a description of your goods")
    public String description;

    public String component;

    public String brand;

    public String partNumber;

    public String hsCode;
  }
}
