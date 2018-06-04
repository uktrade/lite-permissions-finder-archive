package controllers.licencefinder;

import static java.util.concurrent.CompletableFuture.completedFuture;

import com.google.inject.Inject;
import components.auth.SamlAuthorizer;
import components.common.auth.SpireSAML2Client;
import components.common.cache.CountryProvider;
import components.common.state.ContextParamManager;
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
import views.html.licencefinder.destination;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.CompletionStage;

import javax.inject.Named;

@Secure(clients = SpireSAML2Client.CLIENT_NAME, authorizers = SamlAuthorizer.AUTHORIZER_NAME)
public class DestinationController extends Controller {

  private final FormFactory formFactory;
  private final LicenceFinderDao dao;
  private final CountryProvider countryProvider;
  private final ContextParamManager contextParam;

  public static final String DESTINATION_QUESTION = "Where is the final destination of your items?";
  public static final String DESTINATION_MULTIPLE_QUESTION = "Will your items be received by anyone in a different country or territory, such as a consignee, before reaching their final destination?";

  public static final String DESTINATION_COUNTRY = "destinationCountry";
  public static final String FIRST_CONSIGNEE_COUNTRY = "firstConsigneeCountry";
  public static final String MULTIPLE_COUNTRIES = "multipleCountries";
  private static final String UNITED_KINGDOM = "CTRY0";

  @Inject
  public DestinationController(FormFactory formFactory, LicenceFinderDao dao,
                               @Named("countryProviderExport") CountryProvider countryProvider,
                               ContextParamManager contextParam) {
    this.formFactory = formFactory;
    this.dao = dao;
    this.countryProvider = countryProvider;
    this.contextParam = contextParam;
  }


  /************************************************************************************************
   * 'Destination' page
   *******************************************************************************************/
  public CompletionStage<Result> renderDestinationForm() {
    DestinationForm form = new DestinationForm();
    form.destinationCountry = dao.getDestinationCountry();
    form.firstConsigneeCountry = dao.getFirstConsigneeCountry();
    dao.getMultipleCountries().ifPresent(aBoolean -> form.multipleCountries = aBoolean);
    return completedFuture(ok(destination.render(formFactory.form(DestinationForm.class).fill(form), getCountries(), getFieldOrder())));
  }

  public CompletionStage<Result> handleDestinationSubmit() {

    Form<DestinationForm> destinationForm = formFactory.form(DestinationForm.class).bindFromRequest();

    DestinationForm form = destinationForm.get();
    List<CountryView> countries = getCountries();


    if (form.multipleCountries != null && form.multipleCountries) {
      if (form.firstConsigneeCountry == null || form.firstConsigneeCountry.isEmpty()) {
        destinationForm.reject(FIRST_CONSIGNEE_COUNTRY, "Enter a country or territory");
      }
    }

    if (destinationForm.hasErrors()) {
      return completedFuture(ok(destination.render(destinationForm, countries, getFieldOrder())));
    }


    if (countries.stream().noneMatch(country -> country.getCountryRef().equals(form.destinationCountry))) {
      throw new FormStateException("Invalid value for " + DESTINATION_COUNTRY + " \"" + form.destinationCountry + "\"");
    }
    String firstCountry = form.firstConsigneeCountry;

    if (form.multipleCountries && !StringUtils.isBlank(firstCountry) &&
        countries.stream().noneMatch(country -> country.getCountryRef().equals(firstCountry))) {
      throw new FormStateException("Invalid value for " + FIRST_CONSIGNEE_COUNTRY + " \"" + firstCountry + "\"");
    }

    dao.saveFirstConsigneeCountry(firstCountry);
    dao.saveMultipleCountries(form.multipleCountries);
    dao.saveDestinationCountry(form.destinationCountry);

    return contextParam.addParamsAndRedirect(routes.QuestionsController.renderQuestionsForm());
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

      if (multipleCountries != null && multipleCountries) {
        if (firstConsigneeCountry == null || firstConsigneeCountry.isEmpty()) {

          // Could not get to work, got binding error here - validation code is in controller method for now
          // TODO work out why this was happening
          //return new ValidationError("firstConsigneeCountry", "Enter this text");
        }
      }

      return null;
    }

  }

}
