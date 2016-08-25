package controllers;

import com.google.inject.Inject;
import components.common.client.CountryServiceClient;
import components.persistence.PermissionsFinderDao;
import controllers.ogel.OgelQuestionsController;
import play.data.Form;
import play.data.FormFactory;
import play.libs.concurrent.HttpExecutionContext;
import play.mvc.Controller;
import play.mvc.Result;
import views.html.destinationCountry;

import java.util.List;
import java.util.concurrent.CompletionStage;

public class DestinationCountryController extends Controller{

  private final FormFactory formFactory;

  private final PermissionsFinderDao dao;

  private final HttpExecutionContext ec;

  private final CountryServiceClient countryServiceClient;

  private final ErrorController errorController;

  private final OgelQuestionsController ogelQuestionsController;

  public static final int MIN_NUMBER_OF_COUNTRIES = 1;

  public static final int MAX_NUMBER_OF_COUNTRIES = 5;

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
            return ok(destinationCountry.render(formFactory.form(DestinationCountryForm.class), response.getCountries(), MIN_NUMBER_OF_COUNTRIES));
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
            int numberOfDestinationCountries = Integer.parseInt(form.field("numberOfDestinationCountries").value());
            if (numberOfDestinationCountries > MAX_NUMBER_OF_COUNTRIES || MAX_NUMBER_OF_COUNTRIES < MIN_NUMBER_OF_COUNTRIES) {
              return badRequest("Unhandled value of numberOfDestinationCountries: \"" + numberOfDestinationCountries + "\"");
            }
            if(form.hasErrors()){
              return ok(destinationCountry.render(form, response.getCountries(), numberOfDestinationCountries));
            }
            String addAnotherDestination = form.field("addAnotherDestination").value();
            if (addAnotherDestination != null) {
              if ("true".equals(addAnotherDestination)) {
                if (numberOfDestinationCountries == MAX_NUMBER_OF_COUNTRIES) {
                  return badRequest("Unhandled form state, numberOfDestinationCountries already at maximum value");
                }
                return ok(destinationCountry.render(form, response.getCountries(), numberOfDestinationCountries + 1));
              }
              return badRequest("Unhandled value of addAnotherDestination: \"" + addAnotherDestination + "\"");
            }
            List<String> destinationCountries = form.get().destinationCountry;
            for (int i = 0; i < destinationCountries.size(); i++) {
              if (destinationCountries.get(i) == null || destinationCountries.get(i).isEmpty()) {
                form.reject("destinationCountry[" + i + ']', "You must enter a destination or territory");
              }
            }
            // Check again for errors raised during manual validation
            if(form.hasErrors()){
              return ok(destinationCountry.render(form, response.getCountries(), numberOfDestinationCountries));
            }
            // TODO server side validation of destinationCountry value
            dao.saveDestinationCountryList(form.get().destinationCountry);
            return ogelQuestionsController.renderForm();
          }
          else {
            return errorController.renderForm("An issue occurred while processing your request, please try again later.");
          }
        }, ec.current());
  }

  public static class DestinationCountryForm {

    public List<String> destinationCountry;

  }

}
