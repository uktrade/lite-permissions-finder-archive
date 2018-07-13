package controllers.licencefinder;

import static java.util.concurrent.CompletableFuture.completedFuture;

import com.google.inject.Inject;
import components.common.auth.SamlAuthorizer;
import components.common.auth.SpireSAML2Client;
import components.common.cache.CountryProvider;
import components.persistence.LicenceFinderDao;
import controllers.guard.LicenceFinderAwaitGuardAction;
import controllers.guard.LicenceFinderUserGuardAction;
import exceptions.UnknownParameterException;
import org.apache.commons.lang.StringUtils;
import org.pac4j.play.java.Secure;
import play.data.Form;
import play.data.FormFactory;
import play.data.validation.Constraints;
import play.data.validation.ValidationError;
import play.mvc.Controller;
import play.mvc.Result;
import play.mvc.With;
import uk.gov.bis.lite.countryservice.api.CountryView;
import utils.CountryUtils;

import java.util.Collections;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;

import javax.inject.Named;

@Secure(clients = SpireSAML2Client.CLIENT_NAME, authorizers = SamlAuthorizer.AUTHORIZER_NAME)
@With({LicenceFinderUserGuardAction.class, LicenceFinderAwaitGuardAction.class})
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

  public CompletionStage<Result> renderDestinationForm(String sessionId) {
    DestinationForm form = new DestinationForm();
    form.destinationCountry = licenceFinderDao.getDestinationCountry(sessionId).orElse(null);
    form.firstConsigneeCountry = licenceFinderDao.getFirstConsigneeCountry(sessionId).orElse(null);
    licenceFinderDao.getMultipleCountries(sessionId).ifPresent(multipleCountries -> form.multipleCountries = multipleCountries);
    return completedFuture(ok(destination.render(formFactory.form(DestinationForm.class).fill(form), getCountries(), sessionId)));
  }

  public CompletionStage<Result> handleDestinationSubmit(String sessionId) {
    Form<DestinationForm> destinationForm = formFactory.form(DestinationForm.class).bindFromRequest();
    List<CountryView> countries = getCountries();
    if (destinationForm.hasErrors()) {
      return completedFuture(ok(destination.render(destinationForm, countries, sessionId)));
    } else {
      DestinationForm form = destinationForm.get();
      boolean multipleCountries = form.multipleCountries;
      String firstConsigneeCountry = form.firstConsigneeCountry;
      String destinationCountry = form.destinationCountry;

      if (multipleCountries && StringUtils.isEmpty(firstConsigneeCountry)) {
        return completedFuture(ok(destination.render(destinationForm.withError(FIRST_CONSIGNEE_COUNTRY, "Enter a country or territory"),
            countries, sessionId)));
      } else if (!isValidCountry(countries, destinationCountry)) {
        throw UnknownParameterException.unknownCountry(destinationCountry);
      } else if (multipleCountries && !isValidCountry(countries, firstConsigneeCountry)) {
        throw UnknownParameterException.unknownCountry(firstConsigneeCountry);
      } else {
        licenceFinderDao.saveDestinationCountry(sessionId, destinationCountry);
        licenceFinderDao.saveMultipleCountries(sessionId, multipleCountries);
        if (multipleCountries) {
          licenceFinderDao.saveFirstConsigneeCountry(sessionId, firstConsigneeCountry);
        }
        return CompletableFuture.completedFuture(redirect(routes.QuestionsController.renderQuestionsForm(sessionId)));
      }
    }
  }

  private List<CountryView> getCountries() {
    return CountryUtils.getFilteredCountries(CountryUtils.getSortedCountries(countryProvider.getCountries()),
        Collections.singletonList(UNITED_KINGDOM), true);
  }

  private boolean isValidCountry(List<CountryView> countries, String countryRef) {
    return countries.stream()
        .anyMatch(country -> country.getCountryRef().equals(countryRef));
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
        return new ValidationError("firstConsigneeCountry", "Enter the first consignee's country or territory");
      } else {
        return null;
      }
    }
  }

}

