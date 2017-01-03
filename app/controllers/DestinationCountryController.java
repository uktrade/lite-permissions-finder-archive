package controllers;

import static java.util.concurrent.CompletableFuture.completedFuture;

import com.google.inject.Inject;
import components.common.cache.CountryProvider;
import components.common.journey.JourneyManager;
import components.persistence.PermissionsFinderDao;
import exceptions.FormStateException;
import journey.Events;
import models.common.Country;
import play.data.Form;
import play.data.FormFactory;
import play.mvc.Controller;
import play.mvc.Result;
import views.html.destinationCountry;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.CompletionStage;
import java.util.stream.IntStream;

import javax.inject.Named;

public class DestinationCountryController extends Controller {

  private final JourneyManager journeyManager;
  private final FormFactory formFactory;
  private final PermissionsFinderDao permissionsFinderDao;
  private final CountryProvider countryProviderExport;

  public static final int MIN_NUMBER_OF_THROUGH_COUNTRIES = 1;
  public static final int MAX_NUMBER_OF_THROUGH_COUNTRIES = 4;
  public static final String FINAL_DESTINATION_COUNTRY_FIELD_NAME = "finalDestinationCountry";
  public static final String THROUGH_DESTINATION_COUNTRIES_FIELD_NAME = "throughDestinationCountries";
  public static final String ITEM_THROUGH_MULTIPLE_COUNTRIES_FIELD_NAME = "itemThroughMultipleCountries";
  public static final String ADD_ANOTHER_THROUGH_DESTINATION = "addAnotherThroughDestination";

  @Inject
  public DestinationCountryController(JourneyManager journeyManager,
                                      FormFactory formFactory,
                                      PermissionsFinderDao permissionsFinderDao,
                                      @Named("countryProviderExport") CountryProvider countryProviderExport) {
    this.journeyManager = journeyManager;
    this.formFactory = formFactory;
    this.permissionsFinderDao = permissionsFinderDao;
    this.countryProviderExport = countryProviderExport;
  }

  public Result renderForm() {

    List<String> throughDestinationCountries = permissionsFinderDao.getThroughDestinationCountries();
    DestinationCountryForm templateForm = new DestinationCountryForm();

    if (throughDestinationCountries.isEmpty()) {
      throughDestinationCountries = Collections.singletonList("");
      permissionsFinderDao.saveThroughDestinationCountries(throughDestinationCountries);
    }

    templateForm.finalDestinationCountry = permissionsFinderDao.getFinalDestinationCountry();
    templateForm.throughDestinationCountries = throughDestinationCountries;

    List<Country> countries = new ArrayList<>(countryProviderExport.getCountries());

    Optional<Boolean> itemThroughMultipleCountries = permissionsFinderDao.getItemThroughMultipleCountries();
    templateForm.itemThroughMultipleCountries = itemThroughMultipleCountries.isPresent()
        ? Boolean.toString(itemThroughMultipleCountries.get())
        : "";
    return ok(destinationCountry.render(formFactory.form(DestinationCountryForm.class).fill(templateForm), countries, throughDestinationCountries.size(), getFieldOrder(templateForm)));
  }

  public CompletionStage<Result> handleSubmit() {

    Form <DestinationCountryForm> form = formFactory.form(DestinationCountryForm.class).bindFromRequest();

    DestinationCountryForm boundForm = form.get();
    List<Country> countries = new ArrayList<>(countryProviderExport.getCountries());

    if (boundForm.addAnotherThroughDestination != null) {
      if ("true".equals(boundForm.addAnotherThroughDestination)) {
        if (boundForm.throughDestinationCountries.size() >= MAX_NUMBER_OF_THROUGH_COUNTRIES) {
          throw new FormStateException("Unhandled form state, number of " + THROUGH_DESTINATION_COUNTRIES_FIELD_NAME +
              " already at maximum value");
        }
        boundForm.throughDestinationCountries.add("");
        permissionsFinderDao.saveThroughDestinationCountries(boundForm.throughDestinationCountries);
        return completedFuture(ok(destinationCountry.render(form.fill(boundForm), countries,
            boundForm.throughDestinationCountries.size(), getFieldOrder(boundForm))));
      }
      throw new FormStateException("Unhandled value of " + ADD_ANOTHER_THROUGH_DESTINATION + ": \"" +
          boundForm.addAnotherThroughDestination + "\"");
    }
    else if (boundForm.removeThroughDestination != null) {
      int removeIndex = Integer.parseInt(boundForm.removeThroughDestination);
      if (boundForm.throughDestinationCountries.size() <= MIN_NUMBER_OF_THROUGH_COUNTRIES) {
        throw new FormStateException("Unhandled form state, number of " + THROUGH_DESTINATION_COUNTRIES_FIELD_NAME +
            " already at minimum value");
      }
      boundForm.throughDestinationCountries.remove(removeIndex);
      permissionsFinderDao.saveThroughDestinationCountries(boundForm.throughDestinationCountries);
      return completedFuture(ok(destinationCountry.render(form.fill(boundForm), countries,
          boundForm.throughDestinationCountries.size(), getFieldOrder(boundForm))));
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
              form.reject(throughDestinationCountriesIndexedFieldName(i), "You must enter a destination or territory");
            }  // Set.add() returns false if the item was already in the set
            else if (!allCountries.add(country) || country.equals(boundForm.finalDestinationCountry)) {
              form.reject(throughDestinationCountriesIndexedFieldName(i), "You cannot have duplicate destination, " +
                  "country or territories. Please change or remove one");
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
      form.reject(ITEM_THROUGH_MULTIPLE_COUNTRIES_FIELD_NAME, "You must answer this question");
    }
    else {
      throw new FormStateException("Unknown value for " + ITEM_THROUGH_MULTIPLE_COUNTRIES_FIELD_NAME + " \""
          + boundForm.itemThroughMultipleCountries + "\"");
    }

    if (boundForm.finalDestinationCountry == null || boundForm.finalDestinationCountry.isEmpty()) {
      form.reject(FINAL_DESTINATION_COUNTRY_FIELD_NAME, "You must enter a destination or territory");
    }

    // Check again for errors raised during manual validation
    if (form.hasErrors()) {
      return completedFuture(ok(destinationCountry.render(form, countries,
          boundForm.throughDestinationCountries.size(), getFieldOrder(boundForm))));
    }

    if (Boolean.parseBoolean(boundForm.itemThroughMultipleCountries)
        && boundForm.throughDestinationCountries.stream()
        .anyMatch(throughCountry ->  countries.stream()
            .noneMatch(country -> country.getCountryRef().equals(throughCountry)))){
      throw new FormStateException("Invalid value for a " + THROUGH_DESTINATION_COUNTRIES_FIELD_NAME + " item");
    }

    if (countries.stream().noneMatch(country -> country.getCountryRef().equals(boundForm.finalDestinationCountry))) {
      throw new FormStateException("Invalid value for " + FINAL_DESTINATION_COUNTRY_FIELD_NAME + " \""
          + boundForm.finalDestinationCountry + "\"");
    }

    permissionsFinderDao.saveThroughDestinationCountries(boundForm.throughDestinationCountries);
    permissionsFinderDao.saveFinalDestinationCountry(boundForm.finalDestinationCountry);
    return journeyManager.performTransition(Events.DESTINATION_COUNTRIES_SELECTED);
  }

  private List<String> getFieldOrder(DestinationCountryForm destinationCountryForm) {
    List<String> fieldOrder = new ArrayList<>();
    fieldOrder.add(FINAL_DESTINATION_COUNTRY_FIELD_NAME);
    fieldOrder.add(ITEM_THROUGH_MULTIPLE_COUNTRIES_FIELD_NAME);
    for (int i = 0; i < destinationCountryForm.throughDestinationCountries.size(); i++) {
      fieldOrder.add(throughDestinationCountriesIndexedFieldName(i));
    }
    return fieldOrder;
  }

  private String throughDestinationCountriesIndexedFieldName(int index) {
    return THROUGH_DESTINATION_COUNTRIES_FIELD_NAME + "[" + index + "]";
  }

  public static class DestinationCountryForm {

    public String finalDestinationCountry;

    public List<String> throughDestinationCountries;

    public String itemThroughMultipleCountries;

    public String addAnotherThroughDestination;

    public String removeThroughDestination;

  }

}
