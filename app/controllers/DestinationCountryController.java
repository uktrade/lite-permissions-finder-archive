package controllers;

import static java.util.concurrent.CompletableFuture.completedFuture;

import com.google.inject.Inject;
import components.common.client.CountryServiceClient;
import components.common.journey.JourneyManager;
import components.persistence.PermissionsFinderDao;
import journey.Events;
import play.data.Form;
import play.data.FormFactory;
import play.libs.concurrent.HttpExecutionContext;
import play.mvc.Controller;
import play.mvc.Result;
import views.html.destinationCountry;

import java.util.Collections;
import java.util.List;
import java.util.concurrent.CompletionStage;
import java.util.function.Function;

public class DestinationCountryController extends Controller {

  private final JourneyManager jm;
  private final FormFactory formFactory;
  private final PermissionsFinderDao permissionsFinderDao;
  private final HttpExecutionContext ec;
  private final CountryServiceClient countryServiceClient;

  public static final int MIN_NUMBER_OF_COUNTRIES = 1;

  public static final int MAX_NUMBER_OF_COUNTRIES = 5;

  @Inject
  public DestinationCountryController(JourneyManager jm,
                                      FormFactory formFactory,
                                      PermissionsFinderDao permissionsFinderDao,
                                      HttpExecutionContext ec,
                                      CountryServiceClient countryServiceClient) {
    this.jm = jm;
    this.formFactory = formFactory;
    this.permissionsFinderDao = permissionsFinderDao;
    this.ec = ec;
    this.countryServiceClient = countryServiceClient;
  }

  public CompletionStage<Result> renderForm() {
    return countryServiceClient.getCountries()
        .thenApplyAsync(response -> {
          if (response.getStatus() == CountryServiceClient.Status.SUCCESS && !response.getCountries().isEmpty()) {
            List<String> countries = permissionsFinderDao.getDestinationCountryList();
            DestinationCountryForm templateForm = new DestinationCountryForm();
            if (countries.isEmpty()) {
              countries = Collections.singletonList("");
              permissionsFinderDao.saveDestinationCountryList(countries);
            }
            templateForm.destinationCountry = countries;
            return ok(destinationCountry.render(formFactory.form(DestinationCountryForm.class).fill(templateForm), response.getCountries(), countries.size()));
          }
          else {
            return badRequest("An issue occurred while processing your request, please try again later.");
          }
        }, ec.current());
  }

  public CompletionStage<Result> handleSubmit() {
    return countryServiceClient.getCountries()
        .thenApplyAsync(response -> {
          Form <DestinationCountryForm> form = formFactory.form(DestinationCountryForm.class).bindFromRequest();
          if (response.getStatus() == CountryServiceClient.Status.SUCCESS && !response.getCountries().isEmpty()){
            List<String> countries = permissionsFinderDao.getDestinationCountryList();
            if(form.hasErrors()){
              return completedFuture(ok(destinationCountry.render(form, response.getCountries(), countries.size())));
            }

            DestinationCountryForm boundForm = form.get();

            if (boundForm.addAnotherDestination != null) {
              if ("true".equals(boundForm.addAnotherDestination)) {
                if (countries.size() >= MAX_NUMBER_OF_COUNTRIES) {
                  return completedFuture(badRequest("Unhandled form state, numberOfDestinationCountries already at maximum value"));
                }
                boundForm.destinationCountry.add("");
                permissionsFinderDao.saveDestinationCountryList(boundForm.destinationCountry);
                return completedFuture(ok(destinationCountry.render(form, response.getCountries(), boundForm.destinationCountry.size())));
              }
              return completedFuture(badRequest("Unhandled value of addAnotherDestination: \"" + boundForm.addAnotherDestination + "\""));
            }

            if (boundForm.removeLastDestination != null) {
              if ("true".equals(boundForm.removeLastDestination)) {
                if (countries.size() <= MIN_NUMBER_OF_COUNTRIES) {
                  return completedFuture((badRequest("Unhandled form state, numberOfDestinationCountries already at minimum value")));
                }
                boundForm.destinationCountry = boundForm.destinationCountry.subList(0, Math.min(MAX_NUMBER_OF_COUNTRIES, boundForm.destinationCountry.size() -1));
                permissionsFinderDao.saveDestinationCountryList(boundForm.destinationCountry);
                return completedFuture(ok(destinationCountry.render(form, response.getCountries(), boundForm.destinationCountry.size())));
              }
              return completedFuture(badRequest("Unhandled value of removeLastDestination: \"" + boundForm.addAnotherDestination + "\""));
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
              return completedFuture(ok(destinationCountry.render(form, response.getCountries(), countries.size())));
            }

            // TODO server side validation of destinationCountry value
            permissionsFinderDao.saveDestinationCountryList(destinationCountries);
            return jm.performTransition(Events.DESTINATION_COUNTRIES_SELECTED);
          }
          else {
            return completedFuture(badRequest("An issue occurred while processing your request, please try again later."));
          }
        }, ec.current()).thenCompose(Function.identity());
  }

  public static class DestinationCountryForm {

    public List<String> destinationCountry;

    public String addAnotherDestination;

    public String removeLastDestination;

  }

}
