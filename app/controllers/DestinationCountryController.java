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

import java.util.Collections;
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
        .thenApplyAsync(response -> {
          if (response.getStatus() == CountryServiceClient.Status.SUCCESS && !response.getCountries().isEmpty()){
            List<String> countries = Collections.singletonList("");
            dao.saveDestinationCountryList(countries);
            return ok(destinationCountry.render(formFactory.form(DestinationCountryForm.class), response.getCountries(), countries.size()));
          }
          else {
            return errorController.renderForm("An issue occurred while processing your request, please try again later.");
          }
        }, ec.current()).toCompletableFuture().join();
  }

  public CompletionStage<Result> handleSubmit() {
    return countryServiceClient.getCountries()
        .thenApplyAsync(response -> {
          Form <DestinationCountryForm> form = formFactory.form(DestinationCountryForm.class).bindFromRequest();
          if (response.getStatus() == CountryServiceClient.Status.SUCCESS && !response.getCountries().isEmpty()){
            List<String> countries = dao.getDestinationCountryList();
            if(form.hasErrors()){
              return ok(destinationCountry.render(form, response.getCountries(), countries.size()));
            }

            String addAnotherDestination = form.get().addAnotherDestination;
            if (addAnotherDestination != null) {
              if ("true".equals(addAnotherDestination)) {
                if (countries.size() == MAX_NUMBER_OF_COUNTRIES) {
                  return badRequest("Unhandled form state, numberOfDestinationCountries already at maximum value");
                }
                countries.add("");
                dao.saveDestinationCountryList(countries);
                return ok(destinationCountry.render(form, response.getCountries(), countries.size()));
              }
              return badRequest("Unhandled value of addAnotherDestination: \"" + addAnotherDestination + "\"");
            }

            String removeLastDestination = form.get().removeLastDestination;
            if (removeLastDestination != null) {
              if ("true".equals(removeLastDestination)) {
                if (countries.size() == MIN_NUMBER_OF_COUNTRIES) {
                  return badRequest("Unhandled form state, numberOfDestinationCountries already at minimum value");
                }
                countries.remove(countries.size() - 1);
                dao.saveDestinationCountryList(countries);
                return ok(destinationCountry.render(form, response.getCountries(), countries.size()));
              }
              return badRequest("Unhandled value of removeLastDestination: \"" + addAnotherDestination + "\"");
            }

            List<String> destinationCountries = form.get().destinationCountry;
            for (int i = 0; i < destinationCountries.size(); i++) {
              if (destinationCountries.get(i) == null || destinationCountries.get(i).isEmpty()) {
                form.reject("destinationCountry[" + i + ']', "You must enter a destination or territory");
                continue;
              }
              int countryCount = 0;
              for (int j = 0; j < destinationCountries.size(); j++) {
                if (destinationCountries.get(i).equals(destinationCountries.get(j))) {
                  countryCount++;
                }
              }
              if (countryCount > 1) {
                form.reject("destinationCountry[" + i + ']', "You cannot a country more than once, please change or remove one");
              }
            }
            // Check again for errors raised during manual validation
            if(form.hasErrors()){
              return ok(destinationCountry.render(form, response.getCountries(), countries.size()));
            }
            // TODO server side validation of destinationCountry value
            dao.saveDestinationCountryList(destinationCountries);
            return ogelQuestionsController.renderForm();
          }
          else {
            return errorController.renderForm("An issue occurred while processing your request, please try again later.");
          }
        }, ec.current());
  }

  public static class DestinationCountryForm {

    public List<String> destinationCountry;

    public String addAnotherDestination;

    public String removeLastDestination;

  }

}
