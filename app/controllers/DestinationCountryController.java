package controllers;

import static java.util.concurrent.CompletableFuture.completedFuture;

import com.google.inject.Inject;
import components.auth.SamlAuthorizer;
import components.common.auth.SpireSAML2Client;
import components.common.cache.CountryProvider;
import components.common.journey.JourneyManager;
import components.persistence.PermissionsFinderDao;
import exceptions.FormStateException;
import journey.Events;
import org.pac4j.play.java.Secure;
import play.data.Form;
import play.data.FormFactory;
import play.mvc.Controller;
import play.mvc.Result;
import uk.gov.bis.lite.countryservice.api.CountryView;
import utils.CountryUtils;
import views.html.destinationCountry;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.CompletionStage;

import javax.inject.Named;

@Secure(clients = SpireSAML2Client.CLIENT_NAME, authorizers = SamlAuthorizer.AUTHORIZER_NAME)
public class DestinationCountryController extends Controller {

  private final JourneyManager journeyManager;
  private final FormFactory formFactory;
  private final PermissionsFinderDao dao;
  private final CountryProvider countryProvider;

  public static final int MIN_NUMBER_OF_THROUGH_COUNTRIES = 1;
  public static final int MAX_NUMBER_OF_THROUGH_COUNTRIES = 4;
  public static final String FINAL_DESTINATION_COUNTRY_FIELD_NAME = "finalDestinationCountry";
  public static final String THROUGH_DESTINATION_COUNTRIES_FIELD_NAME = "throughDestinationCountries";
  public static final String MULTIPLE_COUNTRIES = "multipleCountries";
  public static final String ADD_ANOTHER_THROUGH_DESTINATION = "addAnotherThroughDestination";
  public static final String UNITED_KINGDOM_COUNTRY_REF = "CTRY0";

  @Inject
  public DestinationCountryController(JourneyManager journeyManager,
                                      FormFactory formFactory,
                                      PermissionsFinderDao dao,
                                      @Named("countryProviderExport") CountryProvider countryProvider) {
    this.journeyManager = journeyManager;
    this.formFactory = formFactory;
    this.dao = dao;
    this.countryProvider = countryProvider;
  }

  /**
   * renderForm
   */
  public Result renderForm() {

    List<String> throughDestinationCountries = dao.getThroughDestinationCountries();
    DestinationCountryForm form = new DestinationCountryForm();

    if (throughDestinationCountries.isEmpty()) {
      throughDestinationCountries = Collections.singletonList("");
      dao.saveThroughDestinationCountries(throughDestinationCountries);
    }

    form.finalDestinationCountry = dao.getFinalDestinationCountry();
    form.throughDestinationCountries = throughDestinationCountries;

    List<CountryView> countries = getCountries();

    dao.getMultipleCountries().ifPresent(aBoolean -> form.multipleCountries = aBoolean);

    return ok(destinationCountry.render(formFactory.form(DestinationCountryForm.class).fill(form), countries,
        throughDestinationCountries.size(), getFieldOrder(form)));
  }

  /**
   * handleSubmit
   */
  public CompletionStage<Result> handleSubmit() {

    Form<DestinationCountryForm> destinationCountryForm = formFactory.form(DestinationCountryForm.class).bindFromRequest();

    DestinationCountryForm form = destinationCountryForm.get();
    List<CountryView> countries = getCountries();

    if (form.addAnotherThroughDestination != null) {
      if ("true".equals(form.addAnotherThroughDestination)) {
        if (form.throughDestinationCountries.size() >= MAX_NUMBER_OF_THROUGH_COUNTRIES) {
          throw new FormStateException("Unhandled form state, number of " + THROUGH_DESTINATION_COUNTRIES_FIELD_NAME + " already at maximum value");
        }
        form.throughDestinationCountries.add("");
        dao.saveThroughDestinationCountries(form.throughDestinationCountries);
        return completedFuture(ok(destinationCountry.render(destinationCountryForm.fill(form), countries, form.throughDestinationCountries.size(), getFieldOrder(form))));
      }
      throw new FormStateException("Unhandled value of " + ADD_ANOTHER_THROUGH_DESTINATION + ": \"" + form.addAnotherThroughDestination + "\"");
    } else if (form.removeThroughDestination != null) {
      int removeIndex = Integer.parseInt(form.removeThroughDestination);
      if (form.throughDestinationCountries.size() <= MIN_NUMBER_OF_THROUGH_COUNTRIES) {
        throw new FormStateException("Unhandled form state, number of " + THROUGH_DESTINATION_COUNTRIES_FIELD_NAME + " already at minimum value");
      }
      form.throughDestinationCountries.remove(removeIndex);
      dao.saveThroughDestinationCountries(form.throughDestinationCountries);
      return completedFuture(ok(destinationCountry.render(destinationCountryForm.fill(form), countries, form.throughDestinationCountries.size(), getFieldOrder(form))));
    }


    if (form.finalDestinationCountry == null || form.finalDestinationCountry.isEmpty()) {
      destinationCountryForm.reject(FINAL_DESTINATION_COUNTRY_FIELD_NAME, "Enter a country or territory");
    }

    if (form.multipleCountries == null) {
      destinationCountryForm.reject(MULTIPLE_COUNTRIES, "Please answer whether your items will be received by anyone in a different country or territory");
    }

    // Check again for errors raised during manual validation
    if (destinationCountryForm.hasErrors()) {
      return completedFuture(ok(destinationCountry.render(destinationCountryForm, countries, form.throughDestinationCountries.size(), getFieldOrder(form))));
    }

    if (countries.stream().noneMatch(country -> country.getCountryRef().equals(form.finalDestinationCountry))) {
      throw new FormStateException("Invalid value for " + FINAL_DESTINATION_COUNTRY_FIELD_NAME + " \"" + form.finalDestinationCountry + "\"");
    }

    dao.saveThroughDestinationCountries(form.throughDestinationCountries);
    dao.saveFinalDestinationCountry(form.finalDestinationCountry);
    dao.saveMultipleCountries(form.multipleCountries);
    return journeyManager.performTransition(Events.DESTINATION_COUNTRIES_SELECTED);
  }

  private List<String> getFieldOrder(DestinationCountryForm destinationCountryForm) {
    List<String> fieldOrder = new ArrayList<>();
    fieldOrder.add(FINAL_DESTINATION_COUNTRY_FIELD_NAME);
    fieldOrder.add(MULTIPLE_COUNTRIES);
    for (int i = 0; i < destinationCountryForm.throughDestinationCountries.size(); i++) {
      fieldOrder.add(throughDestinationCountriesIndexedFieldName(i));
    }
    return fieldOrder;
  }

  private String throughDestinationCountriesIndexedFieldName(int index) {
    return THROUGH_DESTINATION_COUNTRIES_FIELD_NAME + "[" + index + "]";
  }

  private List<CountryView> getCountries() {
    List<CountryView> sortedCountries = CountryUtils.getSortedCountries(countryProvider.getCountries());
    List<String> countryRefs = Collections.singletonList(UNITED_KINGDOM_COUNTRY_REF);
    return CountryUtils.getFilteredCountries(sortedCountries, countryRefs, true);
  }

  public static class DestinationCountryForm {

    public String finalDestinationCountry;

    public List<String> throughDestinationCountries;

    public Boolean multipleCountries;

    public String addAnotherThroughDestination;

    public String removeThroughDestination;

  }

}
