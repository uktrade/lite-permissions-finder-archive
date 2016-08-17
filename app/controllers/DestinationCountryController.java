package controllers;

import com.google.inject.Inject;
import components.common.client.CountryServiceClient;
import components.persistence.PermissionsFinderDao;
import controllers.ogel.OgelQuestionsController;
import play.data.Form;
import play.data.FormFactory;
import play.data.validation.Constraints.Required;
import play.libs.concurrent.HttpExecutionContext;
import play.mvc.Controller;
import play.mvc.Result;
import views.html.destinationCountry;

import java.util.Collections;
import java.util.concurrent.CompletionStage;

public class DestinationCountryController extends Controller{

  private final FormFactory formFactory;

  private final PermissionsFinderDao dao;

  private final HttpExecutionContext ec;

  private final CountryServiceClient countryServiceClient;

  private final ErrorController errorController;

  private final OgelQuestionsController ogelQuestionsController;

  @Inject
  public DestinationCountryController(FormFactory formFactory,
                                      PermissionsFinderDao dao,
                                      HttpExecutionContext ec,
                                      CountryServiceClient countryServiceClient,
                                      ErrorController errorController,
                                      OgelQuestionsController ogelQuestionsController) {
    this.formFactory = formFactory;
    this.dao = dao;
    this.ec = ec;
    this.countryServiceClient = countryServiceClient;
    this.errorController = errorController;
    this.ogelQuestionsController = ogelQuestionsController;
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

  public CompletionStage<Result> handleSubmit() {
    return countryServiceClient.getCountries()
        .thenApplyAsync(response -> {
          Form <DestinationCountryForm> form = formFactory.form(DestinationCountryForm.class).bindFromRequest();
          if (response.isOk()){
            if(form.hasErrors()){
              return ok(destinationCountry.render(form, response.getCountries()));
            }
            // TODO server side validation of destinationCountry value
            dao.saveDestinationCountryList(Collections.singletonList(form.get().destinationCountry));
            return ogelQuestionsController.renderForm();
          }
          else {
            return errorController.renderForm("An issue occurred while processing your request, please try again later.");
          }
        }, ec.current());
  }

  public static class DestinationCountryForm {

    @Required(message = "You must select a destination country")
    public String destinationCountry;

  }

}
