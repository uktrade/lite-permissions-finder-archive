package controllers.licencefinder;

import static java.util.concurrent.CompletableFuture.completedFuture;

import com.google.inject.Inject;
import components.auth.SamlAuthorizer;
import components.common.auth.SpireSAML2Client;
import components.common.cache.CountryProvider;
import components.persistence.LicenceFinderDao;
import exceptions.FormStateException;
import org.apache.commons.lang.StringUtils;
import org.pac4j.play.java.Secure;
import play.data.Form;
import play.data.FormFactory;
import play.data.validation.Constraints;
import play.data.validation.ValidationError;
import play.mvc.Controller;
import play.mvc.Result;
import uk.gov.bis.lite.countryservice.api.CountryView;
import utils.CountryUtils;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;

import javax.inject.Named;

@Secure(clients = SpireSAML2Client.CLIENT_NAME, authorizers = SamlAuthorizer.AUTHORIZER_NAME)
public class DestinationController extends Controller {

  private final FormFactory formFactory;
  private final LicenceFinderDao licenceFinderDao;
  private final CountryProvider countryProvider;
  private final views.html.licencefinder.destination destination;

  public static final String DESTINATION_QUESTION = "Where is the final destination of your items?";
  public static final String DESTINATION_MULTIPLE_QUESTION = "Will your items be received by anyone in a different country or territory, such as a consignee, before reaching their final destination?";

  public static final String DESTINATION_COUNTRY = "destinationCountry";
  public static final String FIRST_CONSIGNEE_COUNTRY = "firstConsigneeCountry";
  public static final String MULTIPLE_COUNTRIES = "multipleCountries";
  private static final String UNITED_KINGDOM = "CTRY0";

  @Inject
  public DestinationController(FormFactory formFactory, LicenceFinderDao licenceFinderDao,
                               @Named("countryProviderExport") CountryProvider countryProvider,
                               views.html.licencefinder.destination destination) {
    this.formFactory = formFactory;
    this.licenceFinderDao = licenceFinderDao;
    this.countryProvider = countryProvider;
    this.destination = destination;
  }

  /**
   * renderDestinationForm
   */
  public CompletionStage<Result> renderDestinationForm(String sessionId) {
    DestinationForm form = new DestinationForm();
    form.destinationCountry = licenceFinderDao.getDestinationCountry(sessionId);
    form.firstConsigneeCountry = licenceFinderDao.getFirstConsigneeCountry(sessionId);
    licenceFinderDao.getMultipleCountries(sessionId).ifPresent(aBoolean -> form.multipleCountries = aBoolean);
    return completedFuture(ok(destination.render(formFactory.form(DestinationForm.class).fill(form), getCountries(), getFieldOrder(), sessionId)));
  }

  /**
   * handleDestinationSubmit
   */
  public CompletionStage<Result> handleDestinationSubmit(String sessionId) {

    Form<DestinationForm> destinationForm = formFactory.form(DestinationForm.class).bindFromRequest();

    DestinationForm form = destinationForm.get();
    List<CountryView> countries = getCountries();

    if (form.multipleCountries != null && form.multipleCountries && StringUtils.isEmpty(form.firstConsigneeCountry)) {
      destinationForm.reject(FIRST_CONSIGNEE_COUNTRY, "Enter a country or territory");
    }

    if (destinationForm.hasErrors()) {
      return completedFuture(ok(destination.render(destinationForm, countries, getFieldOrder(), sessionId)));
    }

    if (countries.stream().noneMatch(country -> country.getCountryRef().equals(form.destinationCountry))) {
      throw new FormStateException("Invalid value for " + DESTINATION_COUNTRY + " \"" + form.destinationCountry + "\"");
    }
    String firstCountry = form.firstConsigneeCountry;

    if (form.multipleCountries && !StringUtils.isBlank(firstCountry) &&
        countries.stream().noneMatch(country -> country.getCountryRef().equals(firstCountry))) {
      throw new FormStateException("Invalid value for " + FIRST_CONSIGNEE_COUNTRY + " \"" + firstCountry + "\"");
    }

    licenceFinderDao.saveFirstConsigneeCountry(sessionId, firstCountry);
    licenceFinderDao.saveMultipleCountries(sessionId, form.multipleCountries);
    licenceFinderDao.saveDestinationCountry(sessionId, form.destinationCountry);

    return CompletableFuture.completedFuture(redirect(routes.QuestionsController.renderQuestionsForm(sessionId)));
  }

  private List<CountryView> getCountries() {
    return CountryUtils.getFilteredCountries(CountryUtils.getSortedCountries(countryProvider.getCountries()),
        Collections.singletonList(UNITED_KINGDOM), true);
  }

  private List<String> getFieldOrder() {
    List<String> fields = new ArrayList<>();
    fields.add(DESTINATION_COUNTRY);
    fields.add(MULTIPLE_COUNTRIES);
    fields.add(FIRST_CONSIGNEE_COUNTRY);
    return fields;
  }

  @Constraints.Validate
  public static final class DestinationForm implements Constraints.Validatable<ValidationError> {

    @Constraints.Required(message = "Select your destination country.")
    public String destinationCountry;

    @Constraints.Required(message = "Answer whether your items will be received by anyone in a different country or territory.")
    public Boolean multipleCountries;

    public String firstConsigneeCountry;

    public ValidationError validate() {
      if (multipleCountries != null && multipleCountries && StringUtils.isEmpty(firstConsigneeCountry)) {
        // Could not get to work, got binding error here - validation code is in controller method for now
        // TODO work out why this was happening
        //return new ValidationError("firstConsigneeCountry", "Enter this text");
      }
      return null;
    }
  }
}

