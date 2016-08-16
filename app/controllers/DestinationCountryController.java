package controllers;

import com.google.inject.Inject;
import components.common.client.CountryServiceClient;
import components.persistence.PermissionsFinderDao;
import play.data.FormFactory;
import play.libs.concurrent.HttpExecutionContext;
import play.mvc.Controller;
import play.mvc.Result;
import views.html.destinationCountry;

public class DestinationCountryController extends Controller{

  private final FormFactory formFactory;

  private final PermissionsFinderDao dao;

  private final HttpExecutionContext ec;

  private final CountryServiceClient countryServiceClient;

  private final ErrorController errorController;

  @Inject
  public DestinationCountryController(FormFactory formFactory,
                                      PermissionsFinderDao dao,
                                      HttpExecutionContext ec,
                                      CountryServiceClient countryServiceClient,
                                      ErrorController errorController) {
    this.formFactory = formFactory;
    this.dao = dao;
    this.ec = ec;
    this.countryServiceClient = countryServiceClient;
    this.errorController = errorController;
  }

  public Result renderForm() {
    return countryServiceClient.getCountries()
        .thenApply(response -> {
          if (response.isOk()){
            return ok(destinationCountry.render(formFactory.form(DestinationCountryForm.class), response.getCountries()));
          }
          else {
            return errorController.renderForm("An issue occurred while processing your request, please try again later.");
          }
        }).toCompletableFuture().join();
  }

  public Result handleSubmit() {
    return ok("Not yet implemented");
  }

  public static class DestinationCountryForm {

    public String destinationCountry;

  }

}
