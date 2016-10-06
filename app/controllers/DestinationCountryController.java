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
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.CompletionStage;
import java.util.function.Function;
import java.util.stream.IntStream;

public class DestinationCountryController extends Controller {

  private final JourneyManager journeyManager;
  private final FormFactory formFactory;
  private final PermissionsFinderDao permissionsFinderDao;
  private final HttpExecutionContext httpExecutionContext;
  private final CountryServiceClient countryServiceClient;

  public static final int MIN_NUMBER_OF_THROUGH_COUNTRIES = 1;

  public static final int MAX_NUMBER_OF_THROUGH_COUNTRIES = 4;

  @Inject
  public DestinationCountryController(JourneyManager journeyManager,
                                      FormFactory formFactory,
                                      PermissionsFinderDao permissionsFinderDao,
                                      HttpExecutionContext httpExecutionContext,
                                      CountryServiceClient countryServiceClient) {
    this.journeyManager = journeyManager;
    this.formFactory = formFactory;
    this.permissionsFinderDao = permissionsFinderDao;
    this.httpExecutionContext = httpExecutionContext;
    this.countryServiceClient = countryServiceClient;
  }

  public CompletionStage<Result> renderForm() {
    return countryServiceClient.getCountries(httpExecutionContext)
        .thenApplyAsync(response -> {
          if (response.getStatus() == CountryServiceClient.Status.SUCCESS && !response.getCountries().isEmpty()) {

            List<String> throughDestinationCountries = permissionsFinderDao.getThroughDestinationCountries();
            DestinationCountryForm templateForm = new DestinationCountryForm();

            if (throughDestinationCountries.isEmpty()) {
              throughDestinationCountries = Collections.singletonList("");
              permissionsFinderDao.saveThroughDestinationCountries(throughDestinationCountries);
            }

            templateForm.finalDestinationCountry = permissionsFinderDao.getFinalDestinationCountry();
            templateForm.throughDestinationCountries = throughDestinationCountries;

            Optional<Boolean> itemThroughMultipleCountries = permissionsFinderDao.getItemThroughMultipleCountries();
            templateForm.itemThroughMultipleCountries = itemThroughMultipleCountries.isPresent()
                ? Boolean.toString(itemThroughMultipleCountries.get())
                : "";

            return ok(destinationCountry.render(formFactory.form(DestinationCountryForm.class).fill(templateForm), response.getCountries(), throughDestinationCountries.size()));
          }
          else {
            return badRequest("An issue occurred while processing your request, please try again later.");
          }
        }, httpExecutionContext.current());
  }

  public CompletionStage<Result> handleSubmit() {
    return countryServiceClient.getCountries(httpExecutionContext)
        .thenApplyAsync(response -> {
          if (response.getStatus() == CountryServiceClient.Status.SUCCESS && !response.getCountries().isEmpty()) {

            Form <DestinationCountryForm> form = formFactory.form(DestinationCountryForm.class).bindFromRequest();

            DestinationCountryForm boundForm = form.get();

            if (boundForm.addAnotherThroughDestination != null) {
              if ("true".equals(boundForm.addAnotherThroughDestination)) {
                if (boundForm.throughDestinationCountries.size() >= MAX_NUMBER_OF_THROUGH_COUNTRIES) {
                  return completedFuture(badRequest("Unhandled form state, number of through destination countries already at maximum value"));
                }
                boundForm.throughDestinationCountries.add("");
                permissionsFinderDao.saveThroughDestinationCountries(boundForm.throughDestinationCountries);
                return completedFuture(ok(destinationCountry.render(form.fill(boundForm), response.getCountries(), boundForm.throughDestinationCountries.size())));
              }
              return completedFuture(badRequest("Unhandled value of addAnotherDestination: \"" + boundForm.addAnotherThroughDestination + "\""));
            }
            else if (boundForm.removeThroughDestination != null) {
              int removeIndex = Integer.parseInt(boundForm.removeThroughDestination);
              if (boundForm.throughDestinationCountries.size() <= MIN_NUMBER_OF_THROUGH_COUNTRIES) {
                return completedFuture(badRequest("Unhandled form state, numberOfDestinationCountries already at minimum value"));
              }
              boundForm.throughDestinationCountries.remove(removeIndex);
              permissionsFinderDao.saveThroughDestinationCountries(boundForm.throughDestinationCountries);
              return completedFuture(ok(destinationCountry.render(form.fill(boundForm), response.getCountries(), boundForm.throughDestinationCountries.size())));
            }

            /*
             * itemThroughMultipleCountries == true -> Validate the (through) country list
             * itemThroughMultipleCountries == false -> Reset the country list
             * itemThroughMultipleCountries == null/empty -> Raise a form error
             * Otherwise raise an exception
             */
            if ("true".equals(boundForm.itemThroughMultipleCountries)) {
              Set<String> allCountries = new HashSet<>();
              IntStream.range(0, boundForm.throughDestinationCountries.size())
                  .boxed()
                  .forEach(i -> {
                    String country = boundForm.throughDestinationCountries.get(i);
                    if (country == null || country.isEmpty()) {
                      form.reject("throughDestinationCountry[" + i + ']', "You must enter a destination or territory");
                    }  // Set.add() returns false if the item was already in the set
                    else if (!allCountries.add(country) || country.equals(boundForm.finalDestinationCountry)) {
                      form.reject("throughDestinationCountry[" + i + ']', "You cannot have duplicate destination, country or territories. Please change or remove one");
                    }
                  });

              permissionsFinderDao.saveThroughDestinationCountries(boundForm.throughDestinationCountries);
              permissionsFinderDao.saveItemThroughMultipleCountries(true);

            }
            else if ("false".equals(boundForm.itemThroughMultipleCountries)) {
              permissionsFinderDao.saveItemThroughMultipleCountries(false);
              boundForm.throughDestinationCountries = Collections.singletonList("");
              permissionsFinderDao.saveThroughDestinationCountries(boundForm.throughDestinationCountries);
            }
            else if (boundForm.itemThroughMultipleCountries == null || boundForm.itemThroughMultipleCountries.isEmpty()) {
              form.reject("itemThroughMultipleCountries", "You must answer this question");
            }
            else {
              return completedFuture(badRequest("Unknown value for itemThroughMultipleCountries \"" + boundForm.itemThroughMultipleCountries + "\""));
            }

            if (boundForm.finalDestinationCountry == null || boundForm.finalDestinationCountry.isEmpty()) {
              form.reject("finalDestinationCountry", "You must enter a destination or territory");
            }

            // Check again for errors raised during manual validation
            if (form.hasErrors()) {
              return completedFuture(ok(destinationCountry.render(form, response.getCountries(), boundForm.throughDestinationCountries.size())));
            }

            if (Boolean.parseBoolean(boundForm.itemThroughMultipleCountries)
                && boundForm.throughDestinationCountries.stream()
                .anyMatch(throughCountry ->  response.getCountries().stream()
                    .noneMatch(country -> country.getCountryRef().equals(throughCountry)))){
              return completedFuture((badRequest("Invalid value for a throughDestinationCountries item")));
            }

            if (response.getCountries().stream().noneMatch(country -> country.getCountryRef().equals(boundForm.finalDestinationCountry))) {
              return completedFuture((badRequest("Invalid value for finalDestinationCountry \"" + boundForm.finalDestinationCountry + "\"")));
            }

            permissionsFinderDao.saveThroughDestinationCountries(boundForm.throughDestinationCountries);
            permissionsFinderDao.saveFinalDestinationCountry(boundForm.finalDestinationCountry);
            return journeyManager.performTransition(Events.DESTINATION_COUNTRIES_SELECTED);
          }
          else {
            return completedFuture(badRequest("An issue occurred while processing your request, please try again later."));
          }
        }, httpExecutionContext.current()).thenCompose(Function.identity());
  }

  public static class DestinationCountryForm {

    public String finalDestinationCountry;

    public List<String> throughDestinationCountries;

    public String itemThroughMultipleCountries;

    public String addAnotherThroughDestination;

    public String removeThroughDestination;

  }

}
